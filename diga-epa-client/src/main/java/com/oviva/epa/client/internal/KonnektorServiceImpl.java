package com.oviva.epa.client.internal;

import com.oviva.epa.client.*;
import com.oviva.epa.client.internal.svc.CardServiceClient;
import com.oviva.epa.client.internal.svc.CertificateServiceClient;
import com.oviva.epa.client.internal.svc.EventServiceClient;
import com.oviva.epa.client.internal.svc.PhrManagementServiceClient;
import com.oviva.epa.client.internal.svc.model.KonnektorContext;
import com.oviva.epa.client.internal.svc.phr.PhrServiceClient;
import com.oviva.epa.client.internal.svc.phr.SmbInformationServiceClient;
import com.oviva.epa.client.internal.svc.phr.model.RecordIdentifierAdapter;
import com.oviva.epa.client.konn.KonnektorConnection;
import com.oviva.epa.client.model.*;
import de.gematik.epa.LibIheXdsMain;
import de.gematik.epa.ihe.model.document.Document;
import de.gematik.epa.ihe.model.document.DocumentMetadata;
import de.gematik.epa.ihe.model.document.ReplaceDocument;
import de.gematik.epa.ihe.model.request.DocumentReplaceRequest;
import de.gematik.epa.ihe.model.request.DocumentSubmissionRequest;
import de.gematik.epa.ihe.model.simple.AuthorInstitution;
import de.gematik.epa.ihe.model.simple.SubmissionSetMetadata;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.time.LocalDateTime;
import java.util.*;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryErrorList;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;
import telematik.ws.conn.cardservicecommon.xsd.v2_0.CardTypeType;
import telematik.ws.conn.exception.FaultMessageException;

public class KonnektorServiceImpl implements KonnektorService {

  private static final String STATUS_SUCCESS =
      "urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Success";
  private final EventServiceClient eventServiceClient;
  private final CardServiceClient cardServiceClient;
  private final PhrManagementServiceClient phrManagementServiceClient;
  private final PhrServiceClient phrServiceClient;

  private final SmbInformationServiceClient smbInformationServiceClient;

  public KonnektorServiceImpl(KonnektorConnection connection, KonnektorContext konnektorContext) {

    eventServiceClient = new EventServiceClient(connection.eventService(), konnektorContext);

    cardServiceClient =
        new CardServiceClient(connection.cardService(), konnektorContext, eventServiceClient);

    phrServiceClient = new PhrServiceClient(connection.phrService(), konnektorContext);

    phrManagementServiceClient =
        new PhrManagementServiceClient(connection.phrManagementService(), konnektorContext);

    var certificateServiceClient =
        new CertificateServiceClient(connection.certificateService(), konnektorContext);

    smbInformationServiceClient =
        new SmbInformationServiceClient(eventServiceClient, certificateServiceClient);
  }

  @NonNull
  @Override
  public List<AuthorInstitution> getAuthorInstitutions() {
    return smbInformationServiceClient.getAuthorInstitutions();
  }

  @NonNull
  @Override
  public List<Card> getCardsInfo() {
    // don't be fooled by the name, this returns a list of cards...
    return eventServiceClient.getSmbInfo().getCards().getCard().stream()
        .map(c -> new Card(c.getCardHandle(), c.getCardHolderName(), mapCardType(c.getCardType())))
        .toList();
  }

  private Card.CardType mapCardType(CardTypeType t) {
    return switch (t) {
      case SMC_B -> Card.CardType.SMC_B;
      case SMC_KT -> Card.CardType.SMC_KT;
      default -> Card.CardType.UNKNOWN;
    };
  }

  @Override
  public @NonNull PinStatus verifySmcPin(@NonNull String cardHandle) {
    var response = cardServiceClient.getPinStatusResponse(cardHandle, "PIN.SMC");
    return PinStatus.valueOf(response.getPinStatus().name());
  }

  @Override
  public @NonNull WriteDocumentResponse writeDocument(
      @NonNull RecordIdentifier recordIdentifier, @NonNull Document document) {

    var phrRecordIdentifier =
        new com.oviva.epa.client.internal.svc.phr.model.RecordIdentifier(
            recordIdentifier.kvnr(), recordIdentifier.homeCommunityId());

    var metadata = getSubmissionSetMetadata(document.documentMetadata());
    var docSubmissionRequest =
        new DocumentSubmissionRequest(
            new RecordIdentifierAdapter(phrRecordIdentifier), List.of(document), metadata);

    var req = LibIheXdsMain.convertDocumentSubmissionRequest(docSubmissionRequest);

    var res =
        phrServiceClient.documentRepositoryProvideAndRegisterDocumentSetB(phrRecordIdentifier, req);

    validateResponse(res);

    return new WriteDocumentResponse(res.getRequestId());
  }

  @Override
  public @NonNull WriteDocumentResponse replaceDocument(
      @NonNull RecordIdentifier recordIdentifier,
      @NonNull Document document,
      @NonNull UUID documentToReplaceId) {

    var phrRecordIdentifier =
        new com.oviva.epa.client.internal.svc.phr.model.RecordIdentifier(
            recordIdentifier.kvnr(), recordIdentifier.homeCommunityId());

    var metadata = getSubmissionSetMetadata(document.documentMetadata());

    var replaceDocument =
        new ReplaceDocument(
            document.documentData(),
            document.documentMetadata(),
            // this ID must be a valid object ID in IHE
            uuidToUrn(documentToReplaceId).orElse(null));

    var req =
        new DocumentReplaceRequest(
            new RecordIdentifierAdapter(phrRecordIdentifier), List.of(replaceDocument), metadata);

    var provideAndRegisterRequest = LibIheXdsMain.convertDocumentReplaceRequest(req);

    var res =
        phrServiceClient.documentRepositoryProvideAndRegisterDocumentSetB(
            phrRecordIdentifier, provideAndRegisterRequest);

    validateResponse(res);

    return new WriteDocumentResponse(res.getRequestId());
  }

  private void validateResponse(RegistryResponseType res) {

    if (STATUS_SUCCESS.equals(res.getStatus())) {
      return;
    }
    var errors =
        Optional.ofNullable(res.getRegistryErrorList())
            .map(RegistryErrorList::getRegistryError)
            .stream()
            .flatMap(Collection::stream)
            .map(
                e ->
                    new WriteDocumentException.Error(
                        e.getValue(),
                        e.getCodeContext(),
                        e.getErrorCode(),
                        e.getSeverity(),
                        e.getLocation()))
            .toList();
    throw new WriteDocumentException(
        "writing document failed, status='%s'".formatted(res.getStatus()), errors);
  }

  @Override
  public @NonNull String getHomeCommunityID(@NonNull String kvnr) {
    try {
      return phrManagementServiceClient.getHomeCommunityID(kvnr);
    } catch (FaultMessageException e) {
      throw new KonnektorException(e.getMessage(), e);
    }
  }

  private SubmissionSetMetadata getSubmissionSetMetadata(DocumentMetadata metadata) {

    var author =
        metadata.author().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("no author"));

    return new SubmissionSetMetadata(List.of(author), null, LocalDateTime.now(), null, null, null);
  }

  /**
   * Convert the UUID to a valid object identity in the context of IHE <a
   * href="https://wiki.ihe.net/index.php/Creating_Unique_IDs_-_OID_and_UUID">Creating Unique IDs -
   * OID and UUID</a>
   */
  private Optional<String> uuidToUrn(@Nullable UUID id) {
    return Optional.ofNullable(id).map("urn:uuid:%s"::formatted);
  }
}
