package com.oviva.epa.client.internal;

import com.oviva.epa.client.*;
import com.oviva.epa.client.internal.svc.*;
import com.oviva.epa.client.internal.svc.model.KonnektorContext;
import com.oviva.epa.client.internal.svc.phr.PhrServiceClient;
import com.oviva.epa.client.internal.svc.phr.SmbInformationServiceClient;
import com.oviva.epa.client.internal.svc.phr.model.RecordIdentifierAdapter;
import com.oviva.epa.client.internal.svc.utils.Digest;
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
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import javax.xml.datatype.XMLGregorianCalendar;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryErrorList;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telematik.ws.conn.cardservicecommon.xsd.v2_0.CardTypeType;
import telematik.ws.conn.connectorcommon.xsd.v5_0.ResultEnum;
import telematik.ws.conn.connectorcommon.xsd.v5_0.Status;
import telematik.ws.conn.exception.FaultMessageException;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.GetAuthorizationListResponse;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.GetAuthorizationStateResponse;

public class KonnektorServiceImpl implements KonnektorService {

  private static Logger log = LoggerFactory.getLogger(KonnektorServiceImpl.class);
  private static final String CERT_ALG_BP = "SHA256withECDSA";
  private static final String REGISTRY_STATUS_SUCCESS =
      "urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Success";
  private final String userAgent; // A_22470-05
  private final EventServiceClient eventServiceClient;
  private final CardServiceClient cardServiceClient;
  private final PhrManagementServiceClient phrManagementServiceClient;
  private final PhrServiceClient phrServiceClient;
  private final CertificateServiceClient certificateServiceClient;
  private final AuthSignatureServiceClient authSignatureServiceClient;

  private final SmbInformationServiceClient smbInformationServiceClient;

  public KonnektorServiceImpl(
      String userAgent, KonnektorConnection connection, KonnektorContext konnektorContext) {
    this.userAgent = userAgent;

    eventServiceClient = new EventServiceClient(connection.eventService(), konnektorContext);

    cardServiceClient =
        new CardServiceClient(connection.cardService(), konnektorContext, eventServiceClient);

    phrServiceClient = new PhrServiceClient(connection.phrService(), konnektorContext);

    phrManagementServiceClient =
        new PhrManagementServiceClient(
            connection.phrManagementService(), konnektorContext, userAgent);

    certificateServiceClient =
        new CertificateServiceClient(connection.certificateService(), konnektorContext);

    smbInformationServiceClient =
        new SmbInformationServiceClient(eventServiceClient, certificateServiceClient);

    authSignatureServiceClient =
        new AuthSignatureServiceClient(connection.authSignatureService(), konnektorContext);
  }

  @NonNull
  @Override
  public List<AuthorInstitution> getAuthorInstitutions() {
    return smbInformationServiceClient.getAuthorInstitutions();
  }

  @NonNull
  @Override
  public List<Card> getCardsInfo() {
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

  @NonNull
  @Override
  public X509Certificate readAuthenticationCertificateForCard(@NonNull String cardHandle) {
    return certificateServiceClient.readAuthenticationCertificateForCard(cardHandle);
  }

  @NonNull
  @Override
  public byte[] authSign(@NonNull String cardHandle, byte[] bytesToSign) {
    // https://gemspec.gematik.de/docs/gemSpec/gemSpec_Kon/gemSpec_Kon_V5.24.0/#4.1.13.1.1
    // TODO?

    var cert = certificateServiceClient.readAuthenticationCertificateForCard(cardHandle);
    System.out.println(cert);
    var alg = cert.getPublicKey().getAlgorithm();
    // TODO: check cert, should be brainpool curve!

    var hash = Digest.sha256(bytesToSign);
    return authSignatureServiceClient.signAuthHash(cardHandle, hash);
  }

  @Override
  @NonNull
  public List<AuthorizedApplication> getAuthorizationState(
      @NonNull RecordIdentifier recordIdentifier) {

    var res =
        phrManagementServiceClient.getAuthorizationState(
            recordIdentifier.kvnr(), recordIdentifier.homeCommunityId());
    validateAuthorizationStateResponse(res);

    return res.getAuthorizationStatusList().getAuthorizedApplication().stream()
        .map(e -> new AuthorizedApplication(e.getApplicationName(), parseDate(e.getValidTo())))
        .toList();
  }

  private void validateAuthorizationStateResponse(GetAuthorizationStateResponse res) {
    var result = parseResult(res.getStatus());
    if (ResultEnum.OK.equals(result)) {
      return;
    }

    if (ResultEnum.WARNING.equals(result)) {
      log.atDebug().addKeyValue("response", res::toString).log("warning response from Konnektor");
      return;
    }

    throw new KonnektorException(
        "bad GetAuthorizationStateResponse: " + res.getStatus().toString());
  }

  @Override
  @NonNull
  public List<AuthorizationEntry> getAuthorizationList() {
    var res = phrManagementServiceClient.getAuthorizationList();

    validateAuthorizationListResponse(res);

    return res.getAuthorizationList().getAuthorizationEntry().stream()
        .map(
            e ->
                new AuthorizationEntry(
                    new RecordIdentifier(
                        e.getRecordIdentifier().getInsurantId().getExtension(),
                        e.getRecordIdentifier().getHomeCommunityId()),
                    parseDate(e.getValidTo())))
        .toList();
  }

  private LocalDate parseDate(XMLGregorianCalendar encoded) {
    return LocalDate.of(encoded.getYear(), encoded.getMonth(), encoded.getDay());
  }

  private void validateAuthorizationListResponse(GetAuthorizationListResponse res) {
    var result = parseResult(res.getStatus());
    if (ResultEnum.OK.equals(result)) {
      return;
    }

    throw new KonnektorException("bad GetAuthorizationListResponse: " + res.getStatus().toString());
  }

  private ResultEnum parseResult(Status status) {
    var str = status.getResult().toUpperCase(Locale.ROOT);
    try {
      // upper case before parsing, the Result allows 'Warning' while the enum does not
      return ResultEnum.valueOf(str);
    } catch (IllegalArgumentException e) {
      // let's not fail on an unknown enum, response might still be useful
      return ResultEnum.WARNING;
    }
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

    if (REGISTRY_STATUS_SUCCESS.equals(res.getStatus())) {
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
