package com.oviva.epa.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.oviva.epa.client.konn.KonnektorConnectionFactoryBuilder;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("e2e")
class KonnektorServiceAcceptanceTest {

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

  private KonnektorService konnektorService;

  @BeforeEach
  void testGetCardsInfo() throws Exception {
    this.konnektorService = buildService();
  }

  @Test
  void listSmcbCards() {

    var cards = konnektorService.listSmcbCards();

    assertEquals(1, cards.size());
    assertEquals("9-SMC-B-Testkarte-883110000145356", cards.get(0).telematikId());
  }

  @Test
  void authSignRsaPss() {

    var cards = konnektorService.listSmcbCards();
    assumeTrue(cards.size() == 1);
    var card = cards.get(0);

    assertNotNull(card.authRsaCertificate());

    var data = "Hello!".getBytes(StandardCharsets.UTF_8);

    // when
    var signed = konnektorService.authSignRsaPss(card.handle(), data);

    // then
    assertNotNull(signed);
  }

  @Test
  void authSignEcc() {

    var cards = konnektorService.listSmcbCards();
    assumeTrue(cards.size() == 1);
    var card = cards.get(0);

    assertNotNull(card.authEccCertificate());

    var data = "Hello!".getBytes(StandardCharsets.UTF_8);

    // when
    var signed = konnektorService.authSignEcdsa(card.handle(), data);

    // then
    assertNotNull(signed);
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
