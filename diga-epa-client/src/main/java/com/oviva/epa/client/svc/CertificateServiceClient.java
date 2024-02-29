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

package com.oviva.epa.client.svc;

import com.oviva.epa.client.svc.model.KonnektorContext;
import com.oviva.epa.client.svc.utils.CertificateUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import telematik.ws.conn.cardservice.xsd.v8_1.CardInfoType;
import telematik.ws.conn.certificateservice.wsdl.v6_0.CertificateServicePortType;
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

  public ReadCardCertificateResponse readCardCertificate(@NonNull ReadCardCertificate request) {
    return certificateService.readCardCertificate(request);
  }

  public String getTelematikIdToCard(@NonNull CardInfoType card) {
    final var cardCertRequest = buildReadCardCertificateRequest(card, CertRefEnum.C_AUT);

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
                        "No AUT certificate in readCardCertificate response for card: " + card));

    X509Certificate cert =
        Objects.requireNonNull(
            CertificateUtils.toX509Certificate(certificate),
            "AUT certificate data could not be decoded as X509 certificate");

    return CertificateUtils.getTelematikIdFromCertificate(cert);
  }

  private ReadCardCertificate buildReadCardCertificateRequest(
      CardInfoType cardInfo, CertRefEnum certRef) {
    final var certRefList = new ObjectFactory().createReadCardCertificateCertRefList();
    certRefList.getCertRef().add(certRef);
    final var readCardCertificateRequest = new ObjectFactory().createReadCardCertificate();
    readCardCertificateRequest.setCardHandle(cardInfo.getCardHandle());
    readCardCertificateRequest.setCertRefList(certRefList);
    readCardCertificateRequest.setContext(context.toContext());
    return readCardCertificateRequest;
  }
}
