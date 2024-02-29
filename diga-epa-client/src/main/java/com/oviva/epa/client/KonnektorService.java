package com.oviva.epa.client;

import com.oviva.epa.client.konn.KonnektorConnection;
import com.oviva.epa.client.svc.CardServiceClient;
import com.oviva.epa.client.svc.CertificateServiceClient;
import com.oviva.epa.client.svc.EventServiceClient;
import com.oviva.epa.client.svc.PhrManagementServiceClient;
import com.oviva.epa.client.svc.model.KonnektorContext;
import com.oviva.epa.client.svc.phr.PhrServiceClient;
import com.oviva.epa.client.svc.phr.SmbInformationServiceClient;
import com.oviva.epa.client.svc.phr.model.RecordIdentifier;
import com.oviva.epa.client.svc.phr.model.RecordIdentifierAdapter;
import de.gematik.epa.LibIheXdsMain;
import de.gematik.epa.ihe.model.document.Document;
import de.gematik.epa.ihe.model.request.DocumentSubmissionRequest;
import de.gematik.epa.ihe.model.simple.AuthorInstitution;
import de.gematik.epa.ihe.model.simple.SubmissionSetMetadata;
import java.time.LocalDateTime;
import java.util.List;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;
import telematik.ws.conn.cardservice.xsd.v8_1.CardInfoType;
import telematik.ws.conn.cardservice.xsd.v8_1.PinStatusEnum;

public class KonnektorService {

  private final EventServiceClient eventServiceClient;
  private final CardServiceClient cardServiceClient;
  private final PhrManagementServiceClient phrManagementServiceClient;
  private final PhrServiceClient phrServiceClient;

  private final SmbInformationServiceClient smbInformationServiceClient;

  public KonnektorService(KonnektorConnection connection, KonnektorContext konnektorContext) {

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

  public List<AuthorInstitution> getAuthorInstitutions() {
    return smbInformationServiceClient.getAuthorInstitutions();
  }

  // TODO: map CardInfoType to a VO
  public List<CardInfoType> getCardsInfo() {
    // don't be fooled by the name, this returns a list of cards...
    return eventServiceClient.getSmbInfo().getCards().getCard();
  }

  // TODO: map PinStatusEnum to a VO
  public PinStatusEnum verifyPin(String cardHandle) {
    var response = cardServiceClient.getPinStatusResponse(cardHandle, "PIN.SMC");
    return response.getPinStatus();
  }

  // TODO: map RegistryResponseType to a VO
  public RegistryResponseType writeDocument(RecordIdentifier recordIdentifier, Document document) {

    var docSubmissionRequest =
        new DocumentSubmissionRequest(
            new RecordIdentifierAdapter(recordIdentifier),
            List.of(document),
            getSubmissionSetMetadata(document));

    var provideAndRegisterRequest =
        LibIheXdsMain.convertDocumentSubmissionRequest(docSubmissionRequest);

    return phrServiceClient.documentRepositoryProvideAndRegisterDocumentSetB(
        recordIdentifier, provideAndRegisterRequest);
  }

  public String getHomeCommunityID(String kvnr) {
    return phrManagementServiceClient.getHomeCommunityID(kvnr);
  }

  private SubmissionSetMetadata getSubmissionSetMetadata(Document document) {

    var author =
        document.documentMetadata().author().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("no author"));

    return new SubmissionSetMetadata(List.of(author), null, LocalDateTime.now(), null, null, null);
  }
}
