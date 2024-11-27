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
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
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

  private static final String FAKE_KVNR = "A123456780";

  // A_22470-05
  // https://gemspec.gematik.de/docs/gemSpec/gemSpec_Aktensystem_ePAfueralle/latest/#2.7
  private static final String USER_AGENT = "TEST/0.0.1";
  private static final String TI_KONNEKTOR_URI = "https://10.156.145.103:443";
  private static final String PROXY_ADDRESS = "127.0.0.1";
  private static final String KEYSTORE_FILE = "keys/vKon_Client_172.026.002.035.p12";
  private static final String KEYSTORE_PASSWORD = "0000";
  private static final String WORKPLACE_ID = "a";
  private static final String CLIENT_SYSTEM_ID = "c";
  private static final String MANDANT_ID = "m";
  private static final String USER_ID = "admin";
  private static final int PROXY_PORT = 3128;

  private static final String MIME_FHIR_XML = "application/fhir+xml";
  private static final String MIME_PDF = "application/pdf";

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
  void authSign() {

    var cards = konnektorService.getCardsInfo();
    assertThat(cards.size(), equalTo(1));
    var card = cards.get(0);

    var signed =
        konnektorService.authSign(card.handle(), "Hello!".getBytes(StandardCharsets.UTF_8));

    assertEquals("expected", Base64.getEncoder().withoutPadding().encodeToString(signed));
  }

  @Test
  @Disabled("rate limited")
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
  void getAuthorizationState_notAuthorized() {

    // 1) get some valid home community
    var hcid = konnektorService.getHomeCommunityID(KVNR);

    // 2) create a record identifier we don't have access to
    var recordIdentifier = new RecordIdentifier(FAKE_KVNR, hcid);

    // 3) get the authorization state
    var authorizedApplications = konnektorService.getAuthorizationState(recordIdentifier);

    // 4) check whether we're authorized for the ePA
    assertTrue(authorizedApplications.isEmpty());
  }

  @Test
  void writePdfDocument() {

    var documentId = UUID.randomUUID();

    // 1) get home community
    var hcid = konnektorService.getHomeCommunityID(KVNR);
    var recordIdentifier = new RecordIdentifier(KVNR, hcid);

    // 2) read author/telematik ID from SMC-B
    var authorInstitution =
        konnektorService.getAuthorInstitutions().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("no SMC-B found"));

    // prepare document
    // IMPORTANT: Only PDF/A is allowed!
    // See: A_25233 & A_24864-02
    // https://gemspec.gematik.de/docs/gemSpec/gemSpec_Aktensystem_ePAfueralle/latest/#A_25233
    var document =
        buildDocumentPayload(documentId, authorInstitution, hcid, MIME_PDF, loadExamplePdf());

    // 3) write the PDF/A-1a document
    assertDoesNotThrow(() -> konnektorService.writeDocument(recordIdentifier, document));
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
    var document =
        buildDocumentPayload(
            documentId, authorInstitution, hcid, MIME_FHIR_XML, EXPORT_XML.getBytes());

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

    var document =
        buildDocumentPayload(
            documentId, authorInstitution, hcid, MIME_FHIR_XML, EXPORT_XML.getBytes());

    // 3) write the FHIR/MIO document
    assertDoesNotThrow(() -> konnektorService.writeDocument(recordIdentifier, document));

    // 4) replace the document
    var newDocumentId = UUID.randomUUID();
    var newDocument =
        buildDocumentPayload(
            newDocumentId, authorInstitution, hcid, MIME_FHIR_XML, EXPORT_XML.getBytes());
    assertDoesNotThrow(
        () -> konnektorService.replaceDocument(recordIdentifier, newDocument, documentId));
  }

  private byte[] loadExamplePdf() {
    try (var is = this.getClass().getClassLoader().getResourceAsStream("example_pdfa.pdf")) {
      return is.readAllBytes();
    } catch (IOException e) {
      fail(e);
    }

    return new byte[0];
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
        .userAgent(USER_AGENT)
        .build();
  }

  private Document buildDocumentPayload(
      UUID id,
      AuthorInstitution authorInstitution,
      String homeCommunityId,
      String mimeType,
      byte[] contents) {
    var repositoryUniqueId = homeCommunityId;
    var currentDate = LocalDateTime.now().toString();

    // IMPORTANT: Without the urn prefix we can't replace it later
    var documentUuid = "urn:uuid:" + id;

    return new Document(
        contents,
        new DocumentMetadata(
            List.of(
                // Telematik-ID der DiGA^Name der DiGA (Name der
                // Verordnungseinheit)^Oviva-AG^^^^^^&<OID für DiGAs, wie in professionOID>&ISO
                // https://gemspec.gematik.de/docs/gemSpec/gemSpec_DM_ePA_EU-Pilot/gemSpec_DM_ePA_EU-Pilot_V1.53.1/#2.1.4.3.1
                new Author(
                    authorInstitution.identifier(),
                    "Oviva Direkt für Adipositas",
                    "Oviva AG",
                    "",
                    "",
                    "",
                    // professionOID for DiGA:
                    // https://gemspec.gematik.de/docs/gemSpec/gemSpec_OID/gemSpec_OID_V3.19.0/#3.5.1.3
                    // TODO read this from the SMC-B, see
                    // com.oviva.epa.client.internal.svc.utils.CertificateUtils::getProfessionInfoFromCertificate
                    "1.2.276.0.76.4.282", // OID
                    // Der identifier in AuthorInstitution muss eine gültige TelematikId sein, so
                    // wie sie z. B. auf der SMC-B-Karte enthalten ist
                    List.of(authorInstitution),
                    List.of("12^^^&amp;1.3.6.1.4.1.19376.3.276.1.5.13&amp;ISO"),
                    List.of("25^^^&1.3.6.1.4.1.19376.3.276.1.5.11&ISO"),
                    List.of("^^Internet^telematik-infrastructure@oviva.com"))),
            "AVAILABLE",
            List.of(ConfidentialityCode.NORMAL.getValue()),
            ClassCode.DURCHFUEHRUNGSPROTOKOLL.getValue(),
            "DiGA MIO-Beispiel eines Dokument von Referenzimplementierung geschickt (Simple Roundtrip)",
            LocalDateTime.now().minusHours(3),
            documentUuid,
            List.of(
                EventCode.VIRTUAL_ENCOUNTER.getValue(), EventCode.PATIENTEN_MITGEBRACHT.getValue()),
            FormatCode.DIGA.getValue(),
            "",
            HealthcareFacilityCode.PATIENT_AUSSERHALB_BETREUUNG.getValue(),
            "de-DE",
            "",
            mimeType,
            PracticeSettingCode.PATIENT_AUSSERHALB_BETREUUNG.getValue(),
            List.of(),
            null,
            null,
            contents.length,
            "ePA Export Oviva Direkt for Obesity " + currentDate,
            TypeCode.PATIENTENEIGENE_DOKUMENTE.getValue(),
            documentUuid,
            "Oviva_DiGA_Export_" + currentDate,
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
