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

package de.gematik.epa.konnektor.client;

import de.gematik.epa.konnektor.KonnektorContextProvider;
import de.gematik.epa.konnektor.KonnektorInterfaceAssembly;
import de.gematik.epa.konnektor.SmbInformationProvider;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import telematik.ws.conn.phrs.phrmanagementservice.wsdl.v2_5.PHRManagementServicePortType;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.GetAuthorizationState;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.GetAuthorizationStateResponse;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.RequestFacilityAuthorization;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.RequestFacilityAuthorizationResponse;
import telematik.ws.conn.phrs.phrservice.xsd.v2_0.ContextHeader;

@Getter
@Accessors(fluent = true)
public class PhrManagementClient extends KonnektorServiceClient {

  private PHRManagementServicePortType phrManagementService;

  private ContextHeader contextHeader;

  private final String kvnr;

  @Getter(lazy = true)
  private final SmbInformationProvider smbInformationProvider =
      new SmbInformationProvider(konnektorContextProvider, konnektorInterfaceAssembly);

  @Getter(lazy = true)
  private final EventServiceClient eventServiceClient =
      smbInformationProvider().eventServiceClient();

  public PhrManagementClient(
      KonnektorContextProvider konnektorContextProvider,
      KonnektorInterfaceAssembly konnektorInterfaceAssembly,
      String kvnr) {
    super(konnektorContextProvider, konnektorInterfaceAssembly);
    this.kvnr = kvnr;
    runInitializationSynchronized();
  }

  public RequestFacilityAuthorizationResponse requestFacilityAuthorization(
      @NonNull RequestFacilityAuthorization request) {
    return phrManagementService.requestFacilityAuthorization(request);
  }

  public GetAuthorizationStateResponse requestGetAuthorizationState(
      @NonNull GetAuthorizationState request) {
    return phrManagementService.getAuthorizationState(request);
  }

  @Override
  protected void initialize() {
    contextHeader = konnektorContextProvider.createContextHeader(kvnr);
    phrManagementService = konnektorInterfaceAssembly.phrManagementService();
  }
}
