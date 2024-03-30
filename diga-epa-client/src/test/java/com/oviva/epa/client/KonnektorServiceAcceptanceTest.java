package com.oviva.epa.client;

import static com.oviva.epa.client.Export.EXPORT_XML;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

import com.oviva.epa.client.konn.KonnektorConnectionFactoryBuilder;
import com.oviva.epa.client.model.Card;
import com.oviva.epa.client.model.PinStatus;
import com.oviva.epa.client.model.RecordIdentifier;
import de.gematik.epa.conversion.internal.enumerated.ClassCode;
import de.gematik.epa.conversion.internal.enumerated.ConfidentialityCode;
import de.gematik.epa.conversion.internal.enumerated.EventCode;
import de.gematik.epa.conversion.internal.enumerated.FormatCode;
import de.gematik.epa.conversion.internal.enumerated.HealthcareFacilityCode;
import de.gematik.epa.conversion.internal.enumerated.PracticeSettingCode;
import de.gematik.epa.conversion.internal.enumerated.TypeCode;
import de.gematik.epa.ihe.model.Author;
import de.gematik.epa.ihe.model.document.Document;
import de.gematik.epa.ihe.model.document.DocumentMetadata;
import de.gematik.epa.ihe.model.simple.AuthorInstitution;
import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
// e2e test to write sample document to our test environment
class KonnektorServiceAcceptanceTest {

  // adapt according to what is authorized for your SMC-B testcard,
  // when in doubt ask your provider (e.g. RISE)
  private static final String KVNR = "X110467329";
  private static final String TI_KONNEKTOR_URI = "https://10.156.145.103:443";
  private static final String PROXY_ADDRESS = "127.0.0.1";
  private static final String KEYSTORE_FILE = "keys/vKon_Client_172.026.002.035.p12";
  private static final String KEYSTORE_PASSWORD = "0000";
  private static final String WORKPLACE_ID = "a";
  private static final String CLIENT_SYSTEM_ID = "c";
  private static final String MANDANT_ID = "m";
  private static final String USER_ID = "admin";
  private static final int PROXY_PORT = 3128;

  private KonnektorService konnektorService;

  @BeforeEach
  void testGetCardsInfo() throws Exception {
    this.konnektorService = buildService();
    var cards = konnektorService.getCardsInfo();

    // if this fails, your connection to the connector is faulty
    assertThat(cards.size(), equalTo(1));

    var card = cards.get(0);
    assertThat(card.type(), equalTo(Card.CardType.SMC_B));
    assertThat(
        card.holderName(),
        equalTo("DiGA-Hersteller und Anbieter Prof. Dr. Tina Gräfin CesaTEST-ONLY"));

    var pinStatus = konnektorService.verifySmcPin(card.handle());

    // if the status is VERIFIABLE, it means the PIN must be reinserted again
    assertThat(pinStatus, equalTo(PinStatus.VERIFIED));
  }

  @Test
  void getAuthorizationList() {

    // IMPORTANT: This is strictly rate-limited to once a day!
    var authorizations = konnektorService.getAuthorizationList();

    // check whether our test KVNR is among them
    assertTrue(authorizations.stream().anyMatch(a -> a.recordIdentifier().kvnr().equals(KVNR)));
  }

  @Test
  void getAuthorizationState() {

    // 1) get home community
    var hcid = konnektorService.getHomeCommunityID(KVNR);
    var recordIdentifier = new RecordIdentifier(KVNR, hcid);

    // 2) get the authorization state
    var authorizedApplications = konnektorService.getAuthorizationState(recordIdentifier);

    // 3) check whether we're authorized for the ePA
    assertTrue(authorizedApplications.stream().anyMatch(a -> "ePA".equals(a.name())));
  }

  @Test
  void writeDocument() {

    var documentId = UUID.randomUUID();

    // when

    // 1) get home community
    var hcid = konnektorService.getHomeCommunityID(KVNR);
    var recordIdentifier = new RecordIdentifier(KVNR, hcid);

    // 2) read author/telematik ID from SMC-B
    var authorInstitution =
        konnektorService.getAuthorInstitutions().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("no SMC-B found"));
    var document = buildDocumentPayload(documentId, authorInstitution, EXPORT_XML.getBytes());

    // 3) write the FHIR/MIO document
    assertDoesNotThrow(() -> konnektorService.writeDocument(recordIdentifier, document));
  }

  @Test
  void replaceDocument() {

    var documentId = UUID.randomUUID();

    // when

    // 1) get home community
    var hcid = konnektorService.getHomeCommunityID(KVNR);
    var recordIdentifier = new RecordIdentifier(KVNR, hcid);

    // 2) read author/telematik ID from SMC-B
    var authorInstitution =
        konnektorService.getAuthorInstitutions().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("no SMC-B found"));

    var document = buildDocumentPayload(documentId, authorInstitution, EXPORT_XML.getBytes());

    // 3) write the FHIR/MIO document
    assertDoesNotThrow(() -> konnektorService.writeDocument(recordIdentifier, document));

    // 4) replace the document
    var newDocumentId = UUID.randomUUID();
    var newDocument = buildDocumentPayload(newDocumentId, authorInstitution, EXPORT_XML.getBytes());
    assertDoesNotThrow(
        () -> konnektorService.replaceDocument(recordIdentifier, newDocument, documentId));
  }

  /** establish a connection to the TI Konnektor */
  private KonnektorService buildService() throws Exception {

    // these are the TLS client credentials as received from the Konnektor provider (e.g. RISE)
    var keys = loadKeys();
    var uri = URI.create(TI_KONNEKTOR_URI);

    var cf =
        KonnektorConnectionFactoryBuilder.newBuilder()
            .clientKeys(keys)
            .konnektorUri(uri)
            .proxyServer(PROXY_ADDRESS, PROXY_PORT)
            .trustAllServers() // currently we don't validate the server's certificate
            .build();

    var conn = cf.connect();

    return KonnektorServiceBuilder.newBuilder()
        .connection(conn)
        .workplaceId(WORKPLACE_ID)
        .clientSystemId(CLIENT_SYSTEM_ID)
        .mandantId(MANDANT_ID)
        .userId(USER_ID)
        .build();
  }

  private Document buildDocumentPayload(
      UUID id, AuthorInstitution authorInstitution, byte[] contents) {
    var repositoryUniqueId = UUID.randomUUID().toString();

    // IMPORTANT: Without the urn prefix we can't replace it later
    var documentUuid = "urn:uuid:" + id;

    return new Document(
        contents,
        new DocumentMetadata(
            List.of(
                new Author(
                    null,
                    "Tester",
                    "DIGA-Test",
                    "",
                    "",
                    "Dr",
                    // Der identifier in AuthorInstitution muss eine gültige TelematikId sein, so
                    // wie sie z. B. auf der SMC-B-Karte enthalten ist
                    List.of(authorInstitution),
                    List.of("11^^^&amp;1.3.6.1.4.1.19376.3.276.1.5.13&amp;ISO"),
                    List.of(),
                    List.of())),
            "AVAILABLE",
            List.of(ConfidentialityCode.NORMAL.getValue()),
            ClassCode.DURCHFUEHRUNGSPROTOKOLL.getValue(),
            "DiGA MIO-Beispiel eines Dokument von Referenzimplementierung geschickt (Simple Roundtrip)",
            LocalDateTime.now().minusHours(3),
            documentUuid,
            List.of(EventCode.PATIENTEN_MITGEBRACHT.getValue()),
            FormatCode.DIGA.getValue(),
            "",
            HealthcareFacilityCode.PATIENT_AUSSERHALB_BETREUUNG.getValue(),
            "de-DE",
            "",
            "application/fhir+xml",
            PracticeSettingCode.PATIENT_AUSSERHALB_BETREUUNG.getValue(),
            List.of(),
            LocalDateTime.now().minusWeeks(2),
            LocalDateTime.now().minusWeeks(1),
            contents.length,
            "Gesundheitsmonitoring",
            TypeCode.PATIENTENEIGENE_DOKUMENTE.getValue(),
            documentUuid,
            "monitoring.xml",
            repositoryUniqueId,
            "",
            KVNR),
        null);
  }

  private List<KeyManager> loadKeys() throws Exception {
    var ks = loadKeyStore();

    final KeyManagerFactory keyFactory =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keyFactory.init(ks, KEYSTORE_PASSWORD.toCharArray());
    return Arrays.asList(keyFactory.getKeyManagers());
  }

  private KeyStore loadKeyStore()
      throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {

    var is = IOUtils.resourceToURL(KEYSTORE_FILE, this.getClass().getClassLoader()).openStream();

    var keyStore = KeyStore.getInstance("PKCS12");

    keyStore.load(is, KEYSTORE_PASSWORD.toCharArray());

    return keyStore;
  }
}
