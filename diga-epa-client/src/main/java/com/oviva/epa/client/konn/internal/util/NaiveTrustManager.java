package com.oviva.epa.client.konn.internal.util;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

public class NaiveTrustManager implements X509TrustManager {

  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType)
      throws CertificateException {

    // we're naive, let's just trust everything
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType)
      throws CertificateException {

    // we're naive, let's just trust everything
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    return new X509Certificate[0];
  }
}
