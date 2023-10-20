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

package de.gematik.epa.unit.util;

import de.gematik.epa.konnektor.KonnektorInterfaceAssembly;
import lombok.Data;
import lombok.experimental.Accessors;
import telematik.ws.conn.cardservice.wsdl.v8_1.CardServicePortType;
import telematik.ws.conn.certificateservice.wsdl.v6_0.CertificateServicePortType;
import telematik.ws.conn.eventservice.wsdl.v6_1.EventServicePortType;
import telematik.ws.conn.phrs.phrmanagementservice.wsdl.v2_5.PHRManagementServicePortType;
import telematik.ws.conn.phrs.phrservice.wsdl.v2_0.PHRServicePortType;
import telematik.ws.conn.signatureservice.wsdl.v7_5.SignatureServicePortType;
import telematik.ws.conn.vsds.vsdservice.wsdl.v5_2.VSDServicePortType;

@Data
@Accessors(fluent = true)
public class KonnektorInterfaceAssemblyMock implements KonnektorInterfaceAssembly {

  private PHRServicePortType phrService;
  private PHRManagementServicePortType phrManagementService;
  private EventServicePortType eventService;
  private CardServicePortType cardService;
  private CertificateServicePortType certificateService;
  private SignatureServicePortType signatureService;
  private VSDServicePortType vsdService;
}
