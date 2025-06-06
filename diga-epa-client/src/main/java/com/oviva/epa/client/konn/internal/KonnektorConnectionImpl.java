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

package com.oviva.epa.client.konn.internal;

import com.oviva.epa.client.konn.KonnektorConnection;
import telematik.ws.conn.authsignatureservice.wsdl.v7_4.AuthSignatureServicePortType;
import telematik.ws.conn.cardservice.wsdl.v8_1.CardServicePortType;
import telematik.ws.conn.certificateservice.wsdl.v6_0.CertificateServicePortType;
import telematik.ws.conn.eventservice.wsdl.v6_1.EventServicePortType;
import telematik.ws.conn.phrs.phrmanagementservice.wsdl.v2_5.PHRManagementServicePortType;
import telematik.ws.conn.phrs.phrservice.wsdl.v2_0.PHRServicePortType;
import telematik.ws.conn.signatureservice.wsdl.v7_5.SignatureServicePortType;
import telematik.ws.conn.vsds.vsdservice.wsdl.v5_2.VSDServicePortType;

public class KonnektorConnectionImpl implements KonnektorConnection {

  private final PHRServicePortType phrService;
  private final PHRManagementServicePortType phrManagementService;
  private final EventServicePortType eventService;
  private final CardServicePortType cardService;
  private final CertificateServicePortType certificateService;
  private final SignatureServicePortType signatureService;
  private final VSDServicePortType vsdService;
  private final AuthSignatureServicePortType authSignatureService;

  public KonnektorConnectionImpl(
      PHRServicePortType phrService,
      PHRManagementServicePortType phrManagementService,
      EventServicePortType eventService,
      CardServicePortType cardService,
      CertificateServicePortType certificateService,
      SignatureServicePortType signatureService,
      VSDServicePortType vsdService,
      AuthSignatureServicePortType authSignatureService) {
    this.phrService = phrService;
    this.phrManagementService = phrManagementService;
    this.eventService = eventService;
    this.cardService = cardService;
    this.certificateService = certificateService;
    this.signatureService = signatureService;
    this.vsdService = vsdService;
    this.authSignatureService = authSignatureService;
  }

  @Override
  public PHRServicePortType phrService() {
    return phrService;
  }

  @Override
  public PHRManagementServicePortType phrManagementService() {
    return phrManagementService;
  }

  @Override
  public EventServicePortType eventService() {
    return eventService;
  }

  @Override
  public CardServicePortType cardService() {
    return cardService;
  }

  @Override
  public CertificateServicePortType certificateService() {
    return certificateService;
  }

  @Override
  public SignatureServicePortType signatureService() {
    return signatureService;
  }

  @Override
  public VSDServicePortType vsdService() {
    return vsdService;
  }

  @Override
  public AuthSignatureServicePortType authSignatureService() {
    return authSignatureService;
  }
}
