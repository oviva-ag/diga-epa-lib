/*
 * Copyright 2023 gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.epa.unit.util;

import de.gematik.epa.config.DefaultdataInterface;
import de.gematik.epa.config.DefaultdataProvider;
import de.gematik.epa.context.ContextHeaderAdapter;
import de.gematik.epa.data.AuthorInstitutionConfiguration;
import de.gematik.epa.data.AuthorPerson;
import de.gematik.epa.data.Context;
import de.gematik.epa.data.SubmissionSetAuthorConfiguration;
import de.gematik.epa.ihe.model.simple.AuthorInstitution;
import de.gematik.epa.konnektor.KonnektorContextProvider;
import de.gematik.epa.konnektor.KonnektorInterfaceAssembly;
import de.gematik.epa.konnektor.KonnektorInterfaceProvider;
import de.gematik.epa.konnektor.KonnektorUtils;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;
import java.io.IOException;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeFactory;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;
import org.mockito.Mockito;
import telematik.ws.conn.cardservice.wsdl.v8_1.CardServicePortType;
import telematik.ws.conn.cardservice.xsd.v8_1.CardInfoType;
import telematik.ws.conn.cardservice.xsd.v8_1.CardInfoType.CardVersion;
import telematik.ws.conn.cardservice.xsd.v8_1.Cards;
import telematik.ws.conn.cardservicecommon.xsd.v2_0.CardTypeType;
import telematik.ws.conn.certificateservice.wsdl.v6_0.CertificateServicePortType;
import telematik.ws.conn.certificateservice.xsd.v6_0.ReadCardCertificateResponse;
import telematik.ws.conn.certificateservicecommon.xsd.v2_0.CertRefEnum;
import telematik.ws.conn.certificateservicecommon.xsd.v2_0.X509DataInfoListType;
import telematik.ws.conn.certificateservicecommon.xsd.v2_0.X509DataInfoListType.X509DataInfo.X509Data.X509IssuerSerial;
import telematik.ws.conn.connectorcommon.xsd.v5_0.Status;
import telematik.ws.conn.connectorcontext.xsd.v2_0.ContextType;
import telematik.ws.conn.eventservice.wsdl.v6_1.EventServicePortType;
import telematik.ws.conn.eventservice.xsd.v6_1.GetCardsResponse;
import telematik.ws.conn.phrs.phrmanagementservice.wsdl.v2_0.PHRManagementServicePortType;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_0.AuthorizedApplicationType;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_0.GetAuthorizationStateResponse;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_0.GetHomeCommunityID;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_0.GetHomeCommunityIDResponse;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_0.RequestFacilityAuthorizationResponse;
import telematik.ws.conn.phrs.phrservice.wsdl.v2_0.PHRServicePortType;
import telematik.ws.conn.signatureservice.wsdl.v7_5.SignatureServicePortType;
import telematik.ws.fd.phr.phrcommon.xsd.v1_1.InsurantIdType;
import telematik.ws.fd.phr.phrcommon.xsd.v1_1.RecordIdentifierType;
import telematik.ws.tel.error.telematikerror.xsd.v2_0.Error;
import telematik.ws.tel.error.telematikerror.xsd.v2_0.ObjectFactory;

@UtilityClass
public class TestDataFactory {

  public static final String MANDANT_ID = "Test_Mandant";
  public static final String CLIENTSYSTEM_ID = "Test_Clientsytem";
  public static final String WORKPLACE_ID = "Test_Workplace";
  public static final String USER_ID = "Test_User";
  public static final String KVNR = "X110435031";
  public static final String ROOT = KonnektorContextProvider.defaultRootId();
  public static final String HOME_COMMUNITY_ID = "1.2.3.4.5.67";
  public static final String STATUS_RESULT_OK = KonnektorUtils.STATUS_OK;
  public static final String REGISTRY_RESPONSE_STATUS_SUCCESS =
      "urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Success";
  public static final String SMB_AUT_TELEMATIK_ID = "5-SMC-B-Testkarte-883110000117894";

  public static ContextHeaderAdapter contextHeader() {
    var contextHeader = new ContextHeaderAdapter();
    contextHeader.setContext(contextType());
    contextHeader.setRecordIdentifier(recordIdentifier());

    return contextHeader;
  }

  public static ContextType contextType() {
    var context = new ContextType();
    context.setMandantId(MANDANT_ID);
    context.setClientSystemId(CLIENTSYSTEM_ID);
    context.setWorkplaceId(WORKPLACE_ID);
    context.setUserId(USER_ID);

    return context;
  }

  public static RecordIdentifierType recordIdentifier() {
    var recordIdentifier = new RecordIdentifierType();
    recordIdentifier.setHomeCommunityId(HOME_COMMUNITY_ID);
    recordIdentifier.setInsurantId(insurantId());

    return recordIdentifier;
  }

  public static InsurantIdType insurantId() {
    var insurantId = new InsurantIdType();
    insurantId.setExtension(KVNR);
    insurantId.setRoot(ROOT);

    return insurantId;
  }

  public static Status getStatusOk() {
    var status = new Status();
    status.setResult(STATUS_RESULT_OK);

    return status;
  }

  public static Error getTelematikError() {

    final var objectFactory = new ObjectFactory();

    final var traceDetail = objectFactory.createErrorTraceDetail();
    traceDetail.setValue("This error is very very devastating in every detail");

    final var trace = objectFactory.createErrorTrace();
    trace.setLogReference("Test-Log-Ref");
    trace.setErrorType("TECHNICAL");
    trace.setEventID("Test-Event-Id");
    trace.setInstance("Unit-Test-Instance");
    trace.setCompType("Unit-Test");
    trace.setSeverity("ERROR");
    trace.setCode(BigInteger.valueOf(7208L));
    trace.setErrorText("Very very devastating error");
    trace.setDetail(traceDetail);

    final var error = objectFactory.createError();
    error.setTimestamp(
        DatatypeFactory.newDefaultInstance().newXMLGregorianCalendar(new GregorianCalendar()));
    error.setMessageID("Test-Msg-Id");
    error.getTrace().add(trace);

    return error;
  }

  public static CardInfoType cardInfoSmb() {
    var cardInfo = new CardInfoType();
    cardInfo.setCardType(CardTypeType.SMC_B);
    cardInfo.setCardVersion(new CardVersion());
    cardInfo.setCardHandle("SMB123");
    cardInfo.setCardHolderName("Arztpraxis Unit Test Olé");
    cardInfo.setIccsn("80271282350235250218");
    cardInfo.setCtId("CT1");
    cardInfo.setSlotId(BigInteger.valueOf(1));

    return cardInfo;
  }

  public static CardInfoType cardInfoEgk(String kvnr) {
    var cardInfo = new CardInfoType();
    cardInfo.setCardType(CardTypeType.EGK);
    cardInfo.setCardVersion(new CardVersion());
    cardInfo.setCardHandle("EGK456");
    cardInfo.setCardHolderName("Elfriede Barbara Gudrun Müller");
    cardInfo.setIccsn("80271282320235252170");
    cardInfo.setCtId("CT1");
    cardInfo.setSlotId(BigInteger.valueOf(2));
    cardInfo.setKvnr(kvnr);

    return cardInfo;
  }

  public static RegistryResponseType registryResponseSuccess() {
    var registryResponse = new RegistryResponseType();

    registryResponse.setStatus(REGISTRY_RESPONSE_STATUS_SUCCESS);

    return registryResponse;
  }

  public static RetrieveDocumentSetResponseType retrieveDocumentSetResponse() {
    var retrieveResponse = new RetrieveDocumentSetResponseType();

    retrieveResponse.setRegistryResponse(registryResponseSuccess());

    var retrievedDocument = new RetrieveDocumentSetResponseType.DocumentResponse();
    var putDoc = ResourceLoader.putDocumentWithFolderMetadataRequest().documentSets().get(0);
    retrievedDocument.setDocumentUniqueId(
        ResourceLoader.retrieveDocumentsRequest().documentUniqueIds().get(0));
    retrievedDocument.setDocument(putDoc.documentData().value());
    retrievedDocument.setMimeType(putDoc.documentMetadata().mimeType());
    retrievedDocument.setHomeCommunityId(HOME_COMMUNITY_ID);
    retrievedDocument.setRepositoryUniqueId(HOME_COMMUNITY_ID);

    retrieveResponse.getDocumentResponse().add(retrievedDocument);

    return retrieveResponse;
  }

  public static AdhocQueryResponse getSuccessResponse() {
    var result = new AdhocQueryResponse();
    result.setStatus("urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Success");
    return result;
  }

  public static GetCardsResponse getCardsSmbResponse() {
    var getCardsResponse = new GetCardsResponse();

    getCardsResponse.setStatus(getStatusOk());
    getCardsResponse.setCards(new Cards());
    getCardsResponse.getCards().getCard().add(cardInfoSmb());

    return getCardsResponse;
  }

  public static GetCardsResponse getCardsEgkResponse(String kvnr) {
    var getCardsResponse = new GetCardsResponse();

    getCardsResponse.setStatus(getStatusOk());
    getCardsResponse.setCards(new Cards());
    getCardsResponse.getCards().getCard().add(cardInfoEgk(kvnr));

    return getCardsResponse;
  }

  public static ReadCardCertificateResponse readCardCertificateResponse() throws IOException {
    var readCardCertificateResponse = new ReadCardCertificateResponse();

    readCardCertificateResponse.setStatus(getStatusOk());
    readCardCertificateResponse.setX509DataInfoList(new X509DataInfoListType());

    var x509DataInfo = new X509DataInfoListType.X509DataInfo();
    x509DataInfo.setCertRef(CertRefEnum.C_AUT);

    var x509Data = new X509DataInfoListType.X509DataInfo.X509Data();
    x509Data.setX509Certificate(ResourceLoader.autCertificateAsByteArray());
    x509Data.setX509SubjectName("Unfallkrankenhaus am SeeTEST-ONLY");
    x509Data.setX509IssuerSerial(new X509IssuerSerial());

    x509DataInfo.setX509Data(x509Data);
    readCardCertificateResponse.getX509DataInfoList().getX509DataInfo().add(x509DataInfo);

    return readCardCertificateResponse;
  }

  public static GetHomeCommunityIDResponse getHomeCommunityIDResponse() {
    var getHomeCommunityIDResponse = new GetHomeCommunityIDResponse();
    getHomeCommunityIDResponse.setHomeCommunityID(HOME_COMMUNITY_ID);
    getHomeCommunityIDResponse.setStatus(getStatusOk());

    return getHomeCommunityIDResponse;
  }

  public static RequestFacilityAuthorizationResponse requestFacilityAuthorizationResponse() {
    var requestFacilityAuthorizationResponse = new RequestFacilityAuthorizationResponse();

    requestFacilityAuthorizationResponse.setStatus(getStatusOk());

    return requestFacilityAuthorizationResponse;
  }

  public static GetAuthorizationStateResponse getAuthorizationStateResponse() {
    var authorizationStateResponse = new GetAuthorizationStateResponse();
    authorizationStateResponse.setStatus(getStatusOk());

    var authorization = new AuthorizedApplicationType();
    authorization.setApplicationName("ePA");
    authorization.setValidTo(
        DatatypeFactory.newDefaultInstance().newXMLGregorianCalendar(new GregorianCalendar()));

    authorizationStateResponse.setAuthorizationStatusList(
        new GetAuthorizationStateResponse.AuthorizationStatusList());
    authorizationStateResponse
        .getAuthorizationStatusList()
        .getAuthorizedApplication()
        .add(authorization);
    return authorizationStateResponse;
  }

  public static AuthorInstitutionConfiguration authorInstitutionConfiguration(
      boolean retrieveFromSmb) {
    return new AuthorInstitutionConfiguration(
        retrieveFromSmb, new AuthorInstitution("Arztpraxis", SMB_AUT_TELEMATIK_ID));
  }

  public static AuthorPerson authorPerson() {
    return new AuthorPerson(
        SMB_AUT_TELEMATIK_ID,
        "Müller-Lüdenscheidt",
        "Manfred",
        "Soldier Boy",
        "von und zu",
        "Prof. Dr. Freiherr");
  }

  public static SubmissionSetAuthorConfiguration submissionSetAuthorConfiguration(
      boolean useFirstDocumentAuthor, boolean retrieveFromSmb) {
    return new SubmissionSetAuthorConfiguration(
        useFirstDocumentAuthor,
        authorPerson(),
        authorInstitutionConfiguration(retrieveFromSmb),
        "11^^^&1.3.6.1.4.1.19376.3.276.1.5.13&ISO");
  }

  public static DefaultdataInterface defaultdata(
      boolean useFirstDocumentAuthor, boolean retrieveFromSmb) {
    return () -> submissionSetAuthorConfiguration(useFirstDocumentAuthor, retrieveFromSmb);
  }

  public static KonnektorInterfaceAssembly konnektorInterfaceAssemblyMock() {
    return new KonnektorInterfaceAssemblyMock()
        .phrService(Mockito.mock(PHRServicePortType.class))
        .phrManagementService(Mockito.mock(PHRManagementServicePortType.class))
        .eventService(Mockito.mock(EventServicePortType.class))
        .cardService(Mockito.mock(CardServicePortType.class))
        .certificateService(Mockito.mock(CertificateServicePortType.class))
        .signatureService(Mockito.mock(SignatureServicePortType.class));
  }

  @SneakyThrows
  public static void initKonnektorTestConfiguration() {
    KonnektorContextProvider.defaultInstance()
        .konnektorContext(new Context(MANDANT_ID, CLIENTSYSTEM_ID, WORKPLACE_ID, USER_ID));

    var konnektorInterfaceAssemblyMock = konnektorInterfaceAssemblyMock();
    KonnektorInterfaceProvider.defaultInstance()
        .setKonnektorInterfaceAssembly(konnektorInterfaceAssemblyMock);

    DefaultdataProvider.defaultInstance().defaultdata(defaultdata(true, false));

    var phrManagementServiceMock = konnektorInterfaceAssemblyMock.phrManagementService();
    Mockito.when(phrManagementServiceMock.getHomeCommunityID(Mockito.any(GetHomeCommunityID.class)))
        .thenReturn(TestDataFactory.getHomeCommunityIDResponse());
  }

  @SneakyThrows
  public static void setupMocksForSmbInformationProvider() {
    var eventServiceMock =
        KonnektorInterfaceProvider.defaultInstance().getKonnektorInterfaceAssembly().eventService();
    var certificateServiceMock =
        KonnektorInterfaceProvider.defaultInstance()
            .getKonnektorInterfaceAssembly()
            .certificateService();
    var getCardsSmbResponse = getCardsSmbResponse();

    Mockito.when(eventServiceMock.getCards(Mockito.any())).thenReturn(getCardsSmbResponse);

    Mockito.when(certificateServiceMock.readCardCertificate(Mockito.any()))
        .thenReturn(readCardCertificateResponse());
  }
}
