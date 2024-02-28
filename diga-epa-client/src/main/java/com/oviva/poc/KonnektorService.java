package com.oviva.poc;

import com.oviva.poc.config.DefaultdataProvider;
import com.oviva.poc.config.MyDefaultdataInterface;
import com.oviva.poc.konn.KonnektorConnection;
import com.oviva.poc.svc.CardServiceClient;
import com.oviva.poc.svc.EventServiceClient;
import com.oviva.poc.svc.PhrManagementServiceClient;
import com.oviva.poc.svc.model.KonnektorContext;
import com.oviva.poc.svc.phr.PhrServiceClient;
import com.oviva.poc.svc.phr.model.RecordIdentifier;
import com.oviva.poc.svc.phr.model.RecordIdentifierAdapter;
import de.gematik.epa.LibIheXdsMain;
import de.gematik.epa.ihe.model.document.Document;
import de.gematik.epa.ihe.model.document.DocumentInterface;
import de.gematik.epa.ihe.model.request.DocumentSubmissionRequest;
import de.gematik.epa.ihe.model.simple.SubmissionSetMetadata;
import java.time.LocalDateTime;
import java.util.List;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;
import telematik.ws.conn.cardservice.xsd.v8_1.CardInfoType;
import telematik.ws.conn.cardservice.xsd.v8_1.PinStatusEnum;

public class KonnektorService {

  private EventServiceClient eventServiceClient;
  private CardServiceClient cardServiceClient;
  private PhrManagementServiceClient phrManagementServiceClient;
  private PhrServiceClient phrServiceClient;

  public KonnektorService(KonnektorConnection connection, KonnektorContext konnektorContext) {

    this.eventServiceClient = new EventServiceClient(connection.eventService(), konnektorContext);

    this.cardServiceClient =
        new CardServiceClient(connection.cardService(), konnektorContext, eventServiceClient);

    this.phrServiceClient = new PhrServiceClient(connection.phrService(), konnektorContext);

    this.phrManagementServiceClient =
        new PhrManagementServiceClient(connection.phrManagementService(), konnektorContext);
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

    // TODO pull out into resonable config
    var defaultdataProvider = new DefaultdataProvider(new MyDefaultdataInterface());

    var documents = List.of(document);

    var docSubmissionRequest =
        new DocumentSubmissionRequest(
            new RecordIdentifierAdapter(recordIdentifier),
            documents,
            getSubmissionSetMetadata(documents, defaultdataProvider));

    var provideAndRegisterRequest =
        LibIheXdsMain.convertDocumentSubmissionRequest(docSubmissionRequest);

    return phrServiceClient.documentRepositoryProvideAndRegisterDocumentSetB(
        recordIdentifier, provideAndRegisterRequest);
  }

  public String getHomeCommunityID(String kvnr) {
    return phrManagementServiceClient.getHomeCommunityID(kvnr);
  }

  private SubmissionSetMetadata getSubmissionSetMetadata(
      List<? extends DocumentInterface> documents, DefaultdataProvider defaultdataProvider) {
    return new SubmissionSetMetadata(
        List.of(defaultdataProvider.getSubmissionSetAuthorFromDocuments(documents)),
        null,
        LocalDateTime.now(),
        null,
        null,
        null);
  }
}
