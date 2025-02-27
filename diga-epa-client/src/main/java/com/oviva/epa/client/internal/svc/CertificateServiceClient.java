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

package com.oviva.epa.client.internal.svc;

import com.oviva.epa.client.internal.svc.model.KonnektorContext;
import com.oviva.epa.client.internal.svc.utils.CertificateUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import telematik.ws.conn.certificateservice.wsdl.v6_0.CertificateServicePortType;
import telematik.ws.conn.certificateservice.xsd.v6_0.CryptType;
import telematik.ws.conn.certificateservice.xsd.v6_0.ObjectFactory;
import telematik.ws.conn.certificateservice.xsd.v6_0.ReadCardCertificate;
import telematik.ws.conn.certificateservice.xsd.v6_0.ReadCardCertificateResponse;
import telematik.ws.conn.certificateservicecommon.xsd.v2_0.CertRefEnum;
import telematik.ws.conn.certificateservicecommon.xsd.v2_0.X509DataInfoListType;
import telematik.ws.conn.certificateservicecommon.xsd.v2_0.X509DataInfoListType.X509DataInfo;
import telematik.ws.conn.certificateservicecommon.xsd.v2_0.X509DataInfoListType.X509DataInfo.X509Data;

public class CertificateServiceClient {

  private final CertificateServicePortType certificateService;

  private final KonnektorContext context;

  public CertificateServiceClient(
      CertificateServicePortType certificateService, KonnektorContext context) {
    this.certificateService = certificateService;
    this.context = context;
  }

  public String getTelematikIdForCard(@NonNull String cardHandle) {
    var cert = readCertificateForRef(cardHandle, CertRefEnum.C_AUT, CryptType.ECC);
    return CertificateUtils.getTelematikIdFromCertificate(cert);
  }

  public X509Certificate readRsaAuthenticationCertificateForCard(@NonNull String cardHandle) {
    return readCertificateForRef(cardHandle, CertRefEnum.C_AUT, CryptType.RSA);
  }

  public X509Certificate readEccAuthenticationCertificateForCard(@NonNull String cardHandle) {
    return readCertificateForRef(cardHandle, CertRefEnum.C_AUT, CryptType.ECC);
  }

  /**
   * @param certRef
   *     <ul>
   *       <li>C.AUT (Authentisierungszertifikat, HBAx, SM-B)
   *       <li>C.ENC (Verschlüsselungszertifikat, HBAx, SM-B)
   *       <li>C.SIG (nicht-qualifiziertes Signaturzertifikat, SM-B)
   *       <li>C.QES (qualifiziertes Signaturzertifikat HBAx)
   *     </ul>
   *     <a href="https://gemspec.gematik.de/docs/gemILF/gemILF_PS/gemILF_PS_V2.24.0/#4.4.4.2">See
   *     also</a>
   */
  private X509Certificate readCertificateForRef(
      @NonNull String cardHandle, @NonNull CertRefEnum certRef, CryptType cryptType) {

    final var cardCertRequest = buildReadCardCertificateRequest(cardHandle, certRef, cryptType);
    var cardCertResponse = readCardCertificate(cardCertRequest);

    var certificate =
        Optional.ofNullable(cardCertResponse)
            .map(ReadCardCertificateResponse::getX509DataInfoList)
            .map(X509DataInfoListType::getX509DataInfo)
            .stream()
            .flatMap(Collection::stream)
            .map(X509DataInfo::getX509Data)
            .map(X509Data::getX509Certificate)
            .findFirst()
            .orElseThrow(
                () ->
                    new NoSuchElementException(
                        "No %s certificate in readCardCertificate response for card: %s"
                            .formatted(certRef, cardHandle)));

    return Objects.requireNonNull(
        CertificateUtils.toX509Certificate(certificate),
        "%s certificate data could not be decoded as X509 certificate".formatted(certRef));
  }

  private ReadCardCertificateResponse readCardCertificate(@NonNull ReadCardCertificate request) {
    return certificateService.readCardCertificate(request);
  }

  private ReadCardCertificate buildReadCardCertificateRequest(
      String cardHandle, CertRefEnum certRef, CryptType cryptType) {
    final var certRefList = new ObjectFactory().createReadCardCertificateCertRefList();
    certRefList.getCertRef().add(certRef);
    final var readCardCertificateRequest = new ObjectFactory().createReadCardCertificate();
    readCardCertificateRequest.setCardHandle(cardHandle);
    readCardCertificateRequest.setCertRefList(certRefList);
    readCardCertificateRequest.setContext(context.toContext());
    readCardCertificateRequest.setCrypt(cryptType);
    return readCardCertificateRequest;
  }
}
