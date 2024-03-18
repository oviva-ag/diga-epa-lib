package com.oviva.epa.client.konn;

import com.oviva.epa.client.konn.internal.KonnektorConnectionConfiguration;
import com.oviva.epa.client.konn.internal.KonnektorConnectionConfiguration.BasicAuthenticationConfig;
import com.oviva.epa.client.konn.internal.KonnektorConnectionConfiguration.ProxyAddressConfig;
import com.oviva.epa.client.konn.internal.KonnektorConnectionConfiguration.TlsConfig;
import com.oviva.epa.client.konn.internal.KonnektorConnectionFactoryImpl;
import com.oviva.epa.client.konn.internal.util.NaiveTrustManager;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;

public class KonnektorConnectionFactoryBuilder {

  private static final List<String> DEFAULT_TLS_CIPHERSUITES =
      List.of(
          "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
          "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
          "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
          "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
          "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
          "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA");

  private URI uri = null;
  private TlsConfig tlsConfig = null;
  private ProxyAddressConfig proxyAddress = null;
  private BasicAuthenticationConfig basicAuthentication = null;

  private List<String> ciphersuites = DEFAULT_TLS_CIPHERSUITES;
  private List<KeyManager> keyManagers = null;
  private List<TrustManager> trustManagers = null;

  private KonnektorConnectionFactoryBuilder() {}

  public static KonnektorConnectionFactoryBuilder newBuilder() {
    return new KonnektorConnectionFactoryBuilder();
  }

  private static List<KeyManager> loadFromPkcs12KeyStore(Path path, String password) {

    try (var fis = Files.newInputStream(path)) {
      var keyStore = KeyStore.getInstance("PKCS12");
      keyStore.load(fis, password.toCharArray());

      final KeyManagerFactory keyFactory =
          KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      keyFactory.init(keyStore, password.toCharArray());
      return Arrays.asList(keyFactory.getKeyManagers());
    } catch (IOException | GeneralSecurityException e) {
      throw new IllegalArgumentException("failed to read keystore from %s".formatted(path), e);
    }
  }

  @NonNull
  public KonnektorConnectionFactoryBuilder konnektorUri(@NonNull URI konnektorUri) {
    this.uri = konnektorUri;
    return this;
  }

  @NonNull
  public KonnektorConnectionFactoryBuilder tlsCiphersuites(@NonNull List<String> ciphersuites) {
    this.ciphersuites = ciphersuites;
    return this;
  }

  @NonNull
  public KonnektorConnectionFactoryBuilder basicAuthentication(
      @NonNull String username, @NonNull String password) {
    this.basicAuthentication = new BasicAuthenticationConfig(username, password, true);
    return this;
  }

  @NonNull
  public KonnektorConnectionFactoryBuilder proxyServer(@NonNull String address, int port) {
    this.proxyAddress = new ProxyAddressConfig(address, port, true);
    return this;
  }

  @NonNull
  public KonnektorConnectionFactoryBuilder clientKeys(@NonNull List<KeyManager> keyManagers) {
    this.keyManagers = keyManagers;
    return this;
  }

  @NonNull
  public KonnektorConnectionFactoryBuilder trustManagers(
      @NonNull List<TrustManager> trustManagers) {
    this.trustManagers = trustManagers;
    return this;
  }

  @NonNull
  public KonnektorConnectionFactoryBuilder trustAllServers() {
    this.trustManagers = List.of(new NaiveTrustManager());
    return this;
  }

  @NonNull
  public KonnektorConnectionFactoryBuilder clientKeysFromP12(
      @NonNull Path keyStorePath, String password) {
    this.keyManagers = loadFromPkcs12KeyStore(keyStorePath, password);
    return this;
  }

  public KonnektorConnectionFactory build() {

    if (uri == null) {
      throw new IllegalArgumentException("uri is required");
    }

    if (tlsConfig == null) {
      var kms = Optional.ofNullable(this.keyManagers).orElse(List.of());
      var tms = Optional.ofNullable(this.trustManagers).orElse(List.of());

      tlsConfig = new TlsConfig(kms, tms, ciphersuites);
    }

    var cfg =
        new KonnektorConnectionConfiguration(uri, tlsConfig, proxyAddress, basicAuthentication);
    return new KonnektorConnectionFactoryImpl(cfg);
  }
}
