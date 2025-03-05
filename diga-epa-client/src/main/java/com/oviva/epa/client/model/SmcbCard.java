package com.oviva.epa.client.model;

import java.security.cert.X509Certificate;

public record SmcbCard(
    String handle,
    String telematikId,
    String holderName,
    X509Certificate authRsaCertificate,
    X509Certificate authEccCertificate,
    boolean pinVerified) {}
