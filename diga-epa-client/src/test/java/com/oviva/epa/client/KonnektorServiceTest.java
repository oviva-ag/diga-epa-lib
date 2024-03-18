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
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Disabled
class KonnektorServiceTest {

  private static final Logger log = LoggerFactory.getLogger(KonnektorServiceTest.class);

  // adapt according to what is authorized for your SMC-B testcard,
  // when in doubt ask your provider (e.g. RISE)
  private static final String KVNR = "X110467329";

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
  void writeDocument() {

    String documentId = UUID.randomUUID().toString();

    // when

    // 1) get home community
    var hcid = konnektorService.getHomeCommunityID(KVNR);
    var recordIdentifier = new RecordIdentifier(KVNR, hcid);

    // 2) read author/telematik ID from SMC-B
    var authorInstitution =
        konnektorService.getAuthorInstitutions().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("no SMC-B found"));
    var document = buildDocumentPayload(KVNR, documentId, authorInstitution, EXPORT_XML.getBytes());

    // 3) write the FHIR/MIO document
    assertDoesNotThrow(() -> konnektorService.writeDocument(recordIdentifier, document));
  }

  /** establish a connection to the TI Konnektor */
  private KonnektorService buildService() throws Exception {

    // these are the TLS client credentials as received from the Konnektor provider (e.g. RISE)
    var keys = loadKeys();
    var uri = URI.create("https://localhost:4443");

    var cf =
        KonnektorConnectionFactoryBuilder.newBuilder()
            .clientKeys(keys)
            .konnektorUri(uri)
            .trustAllServers() // currently we don't validate the server's certificate
            .build();

    var conn = cf.connect();

    return KonnektorServiceBuilder.newBuilder()
        .connection(conn)
        .workplaceId("a")
        .clientSystemId("c")
        .mandantId("m")
        .userId("admin")
        .build();
  }

  private Document buildDocumentPayload(
      String kvnr, String id, AuthorInstitution authorInstitution, byte[] contents) {
    var entryUUID = UUID.randomUUID().toString();
    var repositoryUniqueId = UUID.randomUUID().toString();

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
            LocalDateTime.now().minus(3, ChronoUnit.HOURS),
            entryUUID,
            List.of(EventCode.PATIENTEN_MITGEBRACHT.getValue()),
            FormatCode.DIGA.getValue(),
            "",
            HealthcareFacilityCode.PATIENT_AUSSERHALB_BETREUUNG.getValue(),
            "de-DE",
            "",
            "application/fhir+xml",
            PracticeSettingCode.PATIENT_AUSSERHALB_BETREUUNG.getValue(),
            List.of(),
            LocalDateTime.now().minus(2, ChronoUnit.WEEKS),
            LocalDateTime.now().minus(1, ChronoUnit.WEEKS),
            contents.length,
            "Gesundheitsmonitoring",
            TypeCode.PATIENTENEIGENE_DOKUMENTE.getValue(),
            id,
            "monitoring.xml",
            repositoryUniqueId,
            "",
            kvnr),
        null);
  }

  private List<KeyManager> loadKeys() throws Exception {

    var password = "0000";
    var keyFile = "keys/vKon_Client_172.026.002.035.p12";

    var ks = loadKeyStore(keyFile, password);

    final KeyManagerFactory keyFactory =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keyFactory.init(ks, password.toCharArray());
    return Arrays.asList(keyFactory.getKeyManagers());
  }

  private KeyStore loadKeyStore(String path, String password)
      throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {

    var is = IOUtils.resourceToURL(path, this.getClass().getClassLoader()).openStream();

    var keyStore = KeyStore.getInstance("PKCS12");

    keyStore.load(is, password.toCharArray());

    return keyStore;
  }
}
