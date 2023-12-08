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

import static org.apache.cxf.message.Message.REQUESTOR_ROLE;

import de.gematik.epa.config.AddressConfig;
import de.gematik.epa.config.BasicAuthenticationConfig;
import de.gematik.epa.config.Context;
import de.gematik.epa.config.DefaultdataInterface;
import de.gematik.epa.config.FileInfo;
import de.gematik.epa.config.ProxyAddressConfig;
import de.gematik.epa.config.TlsConfig;
import de.gematik.epa.context.ContextHeaderAdapter;
import de.gematik.epa.data.AuthorInstitutionConfiguration;
import de.gematik.epa.data.AuthorPerson;
import de.gematik.epa.data.SubmissionSetAuthorConfiguration;
import de.gematik.epa.dto.request.SignDocumentRequest.SignatureType;
import de.gematik.epa.ihe.model.simple.AuthorInstitution;
import de.gematik.epa.konnektor.KonnektorContextProvider;
import de.gematik.epa.konnektor.KonnektorInterfaceAssembly;
import de.gematik.epa.konnektor.KonnektorUtils;
import de.gematik.epa.konnektor.config.KonnektorConfigurationMutable;
import de.gematik.epa.konnektor.config.KonnektorConnectionConfigurationMutable;
import de.gematik.epa.konnektor.cxf.KonnektorInterfacesCxfImpl;
import de.gematik.epa.konnektor.cxf.interceptors.HomeCommunityBlockOutInterceptor;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.GregorianCalendar;
import java.util.List;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import oasis.names.tc.dss._1_0.core.schema.Base64Data;
import oasis.names.tc.dss._1_0.core.schema.Base64Signature;
import oasis.names.tc.dss._1_0.core.schema.SignatureObject;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessageInfo.Type;
import org.apache.cxf.service.model.ServiceInfo;
import org.mockito.Mockito;
import telematik.ws.conn.cardservice.wsdl.v8_1.CardServicePortType;
import telematik.ws.conn.cardservice.xsd.v8_1.CardInfoType;
import telematik.ws.conn.cardservice.xsd.v8_1.CardInfoType.CardVersion;
import telematik.ws.conn.cardservice.xsd.v8_1.Cards;
import telematik.ws.conn.cardservice.xsd.v8_1.GetPinStatusResponse;
import telematik.ws.conn.cardservice.xsd.v8_1.PinStatusEnum;
import telematik.ws.conn.cardservicecommon.xsd.v2_0.CardTypeType;
import telematik.ws.conn.cardservicecommon.xsd.v2_0.PinResponseType;
import telematik.ws.conn.cardservicecommon.xsd.v2_0.PinResultEnum;
import telematik.ws.conn.certificateservice.wsdl.v6_0.CertificateServicePortType;
import telematik.ws.conn.certificateservice.xsd.v6_0.ReadCardCertificateResponse;
import telematik.ws.conn.certificateservicecommon.xsd.v2_0.CertRefEnum;
import telematik.ws.conn.certificateservicecommon.xsd.v2_0.X509DataInfoListType;
import telematik.ws.conn.certificateservicecommon.xsd.v2_0.X509DataInfoListType.X509DataInfo.X509Data.X509IssuerSerial;
import telematik.ws.conn.connectorcommon.xsd.v5_0.Status;
import telematik.ws.conn.connectorcontext.xsd.v2_0.ContextType;
import telematik.ws.conn.eventservice.wsdl.v6_1.EventServicePortType;
import telematik.ws.conn.eventservice.xsd.v6_1.GetCardsResponse;
import telematik.ws.conn.phrs.phrmanagementservice.wsdl.v2_5.PHRManagementServicePortType;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.AuthorizedApplicationType;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.GetAuthorizationStateResponse;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.GetHomeCommunityID;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.GetHomeCommunityIDResponse;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.RequestFacilityAuthorizationResponse;
import telematik.ws.conn.phrs.phrservice.wsdl.v2_0.PHRServicePortType;
import telematik.ws.conn.signatureservice.wsdl.v7_5.SignatureServicePortType;
import telematik.ws.conn.signatureservice.xsd.v7_5.DocumentType;
import telematik.ws.conn.signatureservice.xsd.v7_5.SignDocumentResponse;
import telematik.ws.conn.signatureservice.xsd.v7_5.SignResponse;
import telematik.ws.conn.signatureservice.xsd.v7_5.SignResponse.OptionalOutputs;
import telematik.ws.conn.vsds.vsdservice.wsdl.v5_2.VSDServicePortType;
import telematik.ws.conn.vsds.vsdservice.xsd.v5_2.ReadVSDResponse;
import telematik.ws.conn.vsds.vsdservice.xsd.v5_2.VSDStatusType;
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
  public static final String STATUS_RESULT_WARNING = KonnektorUtils.STATUS_WARNING;
  public static final String REGISTRY_RESPONSE_STATUS_SUCCESS =
      "urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Success";
  public static final String SMB_AUT_TELEMATIK_ID = "5-SMC-B-Testkarte-883110000117894";

  public static KonnektorConfigurationMutable createKonnektorConfiguration() {
    return new KonnektorConfigurationMutableForTest()
        .connection(createKonnektorConnectionConfiguration())
        .context(createKonnektorContext());
  }

  public static Context createKonnektorContext() {
    return new Context(MANDANT_ID, CLIENTSYSTEM_ID, WORKPLACE_ID, USER_ID);
  }

  public static KonnektorConnectionConfigurationMutable createKonnektorConnectionConfiguration() {
    return new KonnektorConnectionConfigurationMutableForTest()
        .address(createAddress())
        .tlsConfig(createTlsConfig())
        .proxyAddress(createProxyAddressConfig())
        .basicAuthentication(createBasicAuthenticationData());
  }

  public static AddressConfig createAddress() {
    return new AddressConfig(
        "localhost",
        Integer.parseInt(AddressConfig.DEFAULT_PORT),
        KonnektorInterfacesCxfImpl.HTTPS_PROTOCOL,
        "services");
  }

  public static TlsConfig createTlsConfig() {
    return new TlsConfig(
        new FileInfo(ResourceLoader.TEST_P12),
        "test1234",
        "PKCS12",
        List.of("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", "TLS_DHE_RSA_WITH_AES_256_CBC_SHA"));
  }

  public static ProxyAddressConfig createProxyAddressConfig() {
    return new ProxyAddressConfig(
        "localhost", Integer.parseInt(ProxyAddressConfig.DEFAULT_PORT), true);
  }

  public static BasicAuthenticationConfig createBasicAuthenticationData() {
    return new BasicAuthenticationConfig("root", "root", true);
  }

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

  public static Status getStatusWarning() {
    var status = new Status();
    status.setResult(STATUS_RESULT_WARNING);

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

  public static ReadVSDResponse readVSDResponse() {
    final String allgemeineVersicherungsdaten =
        "H4sIAAAAAAAAAM1S30/CMBD+V5a9s+tmZpgpJQjGEASM6DS+LHU7toXtZtaChr/ejhCzEYKvvrS57+77kV758LssrB3WKq9oYLsOsy2kuEpySgf2dLXs9ft+0HN9eyj4yzgaFUWKJeaEYcOJM6y3lKpEaqS3+YNl1EgN7EzrzxuAL+WYaanzjZMgrCXsVFI2B+x8x7Ot8WQehXdPq+lyMbANYtwF/xXWWLeqxkbF2VbvBb/FNCcSHvMYc70rDkeAzyplguhaYtqQO+UGiYyIcFngMxZcBxzO9rusQiIlWJsXQTE5YbR7fCFLFM+otHU/C3urkMMB4aOPGuOMDpP/MB5cyAen9dllvG+V1Puc1pXqFMans0yStRZ+S+QItTnRMY3Rjw781+WjYC6H5uZwaRJOraETDLrfCv7+y+IHk1/iLhwDAAA=";
    final String geschuetzteVersichertendaten =
        "H4sIAAAAAAAAAIVP22rCQBD9lbDvZmIhoDJZKSoS8AINFfFFlmTMBpONZCdpydd3QwsqLfTlzMyZy5mD88+q9DpqbFGbSIz9QHhk0jorTB6JONmPJpNwOhqHYi7xfXFek011S9wzHYalVFPDZDLl4LjdeO6asZHQzLcZwIf1c6oUF1c/I7go6GxWDQBd6L8Ib7Hcng+rtyTe7yLhGKcu8dT2Spetya1lxa2VmHzHAOEnQ/hjiEq6ctE5L9woykniKzU9l+5HOUW4F3hS2qjH3jPhZH7fgv/Nyy+lvE/gTQEAAA==";
    final String persoenlicheVersichertendaten =
        "H4sIAAAAAAAAAI1SwW7aQBD9lZXvYTEFEarxRkmIUqQAUaxCbmhjD7bT9Wy1u4aW322/IeeMU0Kh7aGXtd/MvHkzegMX32ojNuh8ZSmJ4k43EkiZzSsqkmiSzs/Ozwejs3gQCR805dpYwiT6jj66UPD5enXPVItkqqzERduGvy4gV/LzOL0T1+PpanHzkE7msyQadHqtAmuST6IyhK8fpdz6ToG1DtWXTo5yreXG53X7yA3XRwqO+roTRKvJWD3Gcbc/7I+GXZB/5uBtPFJwi0+NC56namoVjwZx3OsOQZ6EYWEd6RpVutUUnlFMf7hKE6GYVZk1jHWLxT0Gp1lrXw0znZVvf0v0Hk2w6wDyEGRpn5UGszKoZat4QEyskfyu8Trs1K37ua6oJR4FIWUpbkqXuWub80LWB4NV2OnSqN6HYb8P8iQGcxdUmpUvtNUmR6FrcdVQ4Z/QFSDbJNyxkQqWtiTPJKPZLXTsOaoxyH+FQf6i7KdRD03ptQH5juGTbjw1dc3+9EAeIbgk3tdVazZkv9MNn5amQvDgfycPPX9vLN89lKd3IP/j+tQr5eQJHuACAAA=";
    final String pruefungsnachweis =
        "H4sIAAAAAAAACh2NQQ+CIBhA/wrjrh/gatoAD+XBQ+qydW1MyFyKLpjWv0+7vu29x9PP0KPZvF03WoFpSDAythl1Z1uB87oM4niXBBQj55XVqh+tEfhrHE4lrwp0PJ3vt+xS52Xxtzd/LVon8NP76QCwuLA1g/LdK9QGHgpmpweY7ALzdpP8WktGWEQSRsk+YizisCKeScohkxyqQv4AzmAvGKYAAAA=";

    var readVSDResponse = new ReadVSDResponse();
    readVSDResponse.setVSDStatus(new VSDStatusType().withStatus("1"));
    readVSDResponse.setAllgemeineVersicherungsdaten(
        allgemeineVersicherungsdaten.getBytes(StandardCharsets.UTF_8));
    readVSDResponse.setGeschuetzteVersichertendaten(
        geschuetzteVersichertendaten.getBytes(StandardCharsets.UTF_8));
    readVSDResponse.setPersoenlicheVersichertendaten(
        persoenlicheVersichertendaten.getBytes(StandardCharsets.UTF_8));
    readVSDResponse.setPruefungsnachweis(Base64.getDecoder().decode(pruefungsnachweis.getBytes()));
    return readVSDResponse;
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

  public static GetPinStatusResponse getPinStatusResponse(PinStatusEnum pinStatus) {
    BigInteger leftTries = new BigInteger("3");
    return new GetPinStatusResponse()
        .withStatus(getStatusOk())
        .withPinStatus(pinStatus)
        .withLeftTries(leftTries);
  }

  public static PinResponseType verifyPin(Status status, PinResultEnum pinResult) {
    BigInteger leftTries = new BigInteger("3");
    return new PinResponseType()
        .withStatus(status)
        .withPinResult(pinResult)
        .withLeftTries(leftTries);
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

  public static SignDocumentResponse getSignDocumentResponse() {
    return new SignDocumentResponse()
        .withSignResponse(
            new SignResponse()
                .withRequestID("reth456")
                .withStatus(getStatusOk())
                .withSignatureObject(
                    new SignatureObject()
                        .withBase64Signature(
                            new Base64Signature()
                                .withType(SignatureType.CMS.uri().toString())
                                .withValue("I am a Signature".getBytes(StandardCharsets.UTF_8))))
                .withOptionalOutputs(
                    new OptionalOutputs()
                        .withDocumentWithSignature(
                            new DocumentType().withBase64Data(new Base64Data()))));
  }

  public static SoapMessage createCxfSoapMessage() {
    var soapMessage = new SoapMessage(Soap12.getInstance());
    var interfaceInfo =
        new InterfaceInfo(
            new ServiceInfo(), new QName(HomeCommunityBlockOutInterceptor.PHR_SERVICE_PORT_NAME));
    var operationInfo =
        interfaceInfo.addOperation(
            new QName(HomeCommunityBlockOutInterceptor.PROV_AND_REG_OPERATION_NAME));
    var msgInfo = new MessageInfo(operationInfo, Type.OUTPUT, operationInfo.getName());
    soapMessage.put(MessageInfo.class, msgInfo);
    soapMessage.put(REQUESTOR_ROLE, Boolean.TRUE);
    return soapMessage;
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
        "Prof. Dr. Freiherr",
        "1.2.276.0.76.4.16");
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
        .signatureService(Mockito.mock(SignatureServicePortType.class))
        .vsdService(Mockito.mock(VSDServicePortType.class));
  }

  @SneakyThrows
  public static void initKonnektorTestConfiguration(
      KonnektorInterfaceAssembly konnektorInterfaceAssembly) {
    var phrManagementServiceMock = konnektorInterfaceAssembly.phrManagementService();
    Mockito.when(phrManagementServiceMock.getHomeCommunityID(Mockito.any(GetHomeCommunityID.class)))
        .thenReturn(TestDataFactory.getHomeCommunityIDResponse());
  }

  @SneakyThrows
  public static void setupMocksForSmbInformationProvider(
      KonnektorInterfaceAssembly konnektorInterfaceAssembly) {
    var eventServiceMock = konnektorInterfaceAssembly.eventService();
    var certificateServiceMock = konnektorInterfaceAssembly.certificateService();
    var getCardsSmbResponse = getCardsSmbResponse();

    Mockito.when(eventServiceMock.getCards(Mockito.any())).thenReturn(getCardsSmbResponse);

    Mockito.when(certificateServiceMock.readCardCertificate(Mockito.any()))
        .thenReturn(readCardCertificateResponse());
  }

  @Data
  @Accessors(fluent = true)
  static class KonnektorConnectionConfigurationMutableForTest
      implements KonnektorConnectionConfigurationMutable {
    private AddressConfig address;
    private BasicAuthenticationConfig basicAuthentication;
    private ProxyAddressConfig proxyAddress;
    private TlsConfig tlsConfig;
  }

  @Data
  @Accessors(fluent = true)
  static class KonnektorConfigurationMutableForTest implements KonnektorConfigurationMutable {
    private KonnektorConnectionConfigurationMutable connection;
    private Context context;
  }
}
