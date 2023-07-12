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

package de.gematik.epa.api.impl;

import de.gematik.epa.api.PermissionApi;
import de.gematik.epa.dto.request.GetAuthorizationStateRequest;
import de.gematik.epa.dto.request.PermissionHcpoRequest;
import de.gematik.epa.dto.response.GetAuthorizationStateResponseDTO;
import de.gematik.epa.dto.response.ResponseDTO;
import de.gematik.epa.konnektor.KonnektorContextProvider;
import de.gematik.epa.konnektor.KonnektorUtils;
import de.gematik.epa.konnektor.SmbInformationProvider;
import de.gematik.epa.konnektor.client.EventServiceClient;
import de.gematik.epa.konnektor.client.PhrManagementClient;
import de.gematik.epa.konnektor.conversion.PermissionUtils;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_0.GetAuthorizationStateResponse;

@Accessors(fluent = true)
@RequiredArgsConstructor
@Getter
@Slf4j
public class PermissionApiImpl implements PermissionApi {

  private final KonnektorContextProvider contextProvider;

  private final EventServiceClient eventServiceClient;

  private final SmbInformationProvider smbInformationProvider;

  private final PhrManagementClient phrMgmtClient;

  @Override
  public ResponseDTO permissionHcpo(PermissionHcpoRequest request) {
    try {
      log.info("Running operation permissionHcpo");
      var contextHeader =
          Objects.requireNonNull(
              contextProvider.createContextHeader(request.kvnr()),
              "No contextHeader could be created using KVNR "
                  + Objects.toString(request.kvnr(), "null"));
      var egkInfo =
          Objects.requireNonNull(
              eventServiceClient.getEgkInfoToKvnr(request.kvnr()),
              "No egkInfo could be retrieved for KVNR " + Objects.toString(request.kvnr(), "null"));
      var leiInstitutionInfo =
          Objects.requireNonNull(
              smbInformationProvider.getOneAuthorInstitution(),
              "No Leistungserbringer Institution information could be retrieved");

      var konRequest =
          PermissionUtils.createRequestFacilityAuthorizationRequest(
              request, leiInstitutionInfo, egkInfo, contextHeader);

      var konResponse = phrMgmtClient.requestFacilityAuthorization(konRequest);

      return KonnektorUtils.fromStatus(konResponse.getStatus());
    } catch (Exception e) {
      log.error("The operation permissionHcpo failed because an exception was thrown", e);
      return KonnektorUtils.fromThrowable(e);
    }
  }

  @Override
  public GetAuthorizationStateResponseDTO getAuthorizationState(
      GetAuthorizationStateRequest request) {
    try {
      log.info("Running operation getAuthorizationState");
      var contextHeader =
          Objects.requireNonNull(
              contextProvider.createContextHeader(request.kvnr()),
              "No contextHeader could be created using KVNR "
                  + Objects.toString(request.kvnr(), "null"));

      var konRequest = PermissionUtils.createRequestGetAuthorizationStateRequest(contextHeader);
      GetAuthorizationStateResponse konResponse =
          phrMgmtClient.requestGetAuthorizationState(konRequest);
      return KonnektorUtils.getAuthorizationStateResponse(konResponse);
    } catch (Exception e) {
      log.error("The operation GetAuthorizationState failed because an exception was thrown", e);
      return new GetAuthorizationStateResponseDTO(KonnektorUtils.fromThrowable(e));
    }
  }
}
