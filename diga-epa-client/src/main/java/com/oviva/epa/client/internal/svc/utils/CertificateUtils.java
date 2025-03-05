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

package com.oviva.epa.client.internal.svc.utils;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Optional;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.isismtt.ISISMTTObjectIdentifiers;
import org.bouncycastle.asn1.isismtt.x509.AdmissionSyntax;
import org.bouncycastle.asn1.isismtt.x509.Admissions;
import org.bouncycastle.asn1.isismtt.x509.ProfessionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CertificateUtils {

  private static final String ADMISSION_IDENTIFIER_ID =
      ISISMTTObjectIdentifiers.id_isismtt_at_admission.getId();
  private static final String CERTIFICATE_TYPE_X509 = "X.509";
  private static final Logger log = LoggerFactory.getLogger(CertificateUtils.class);

  private CertificateUtils() {}

  /**
   * Parse an X509 certificate in encoded form into an {@link X509Certificate}
   *
   * @param encoded the encoded certificate, must not be null
   * @return the certificate as X509Certificate object
   */
  public static X509Certificate toX509Certificate(byte[] encoded) {
    try {
      var certFactory = CertificateFactory.getInstance(CERTIFICATE_TYPE_X509);
      var certStream = new ByteArrayInputStream(encoded);
      return (X509Certificate) certFactory.generateCertificate(certStream);
    } catch (CertificateException e) {
      throw new IllegalArgumentException("failed to parse encoded X.509 certificate", e);
    }
  }

  public static String getTelematikIdFromCertificate(@NonNull final X509Certificate certificate) {
    final var professionInfo = getProfessionInfoFromCertificate(certificate);
    return Optional.ofNullable(professionInfo)
        .map(ProfessionInfo::getRegistrationNumber)
        .orElseGet(
            () -> {
              log.warn("No telematikId could be found in certificate {}", certificate);
              return null;
            });
  }

  // region private

  private static ProfessionInfo getProfessionInfoFromCertificate(
      @NonNull final X509Certificate certificate) {
    final byte[] extensionValue = certificate.getExtensionValue(ADMISSION_IDENTIFIER_ID);
    return getProfessionInfoFromAdmissionExtension(extensionValue);
  }

  private static ProfessionInfo getProfessionInfoFromAdmissionExtension(byte[] extensionValue) {
    return Optional.ofNullable(extensionValue)
        .filter(extVal -> extVal.length > 2)
        .map(ASN1OctetString::getInstance)
        .map(asn1Octet -> ASN1Sequence.getInstance(asn1Octet.getOctets()))
        .map(AdmissionSyntax::getInstance)
        .map(AdmissionSyntax::getContentsOfAdmissions)
        .stream()
        .flatMap(Arrays::stream)
        .map(Admissions::getProfessionInfos)
        .flatMap(Arrays::stream)
        .findFirst()
        .orElse(null);
  }

  // endregion private

}
