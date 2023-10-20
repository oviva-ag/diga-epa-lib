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

import de.gematik.epa.api.ReadVSDApi;
import de.gematik.epa.dto.request.ReadVSDRequest;
import de.gematik.epa.dto.response.ReadVSDResponseDTO;
import de.gematik.epa.konnektor.KonnektorContextProvider;
import de.gematik.epa.konnektor.KonnektorInterfaceAssembly;
import de.gematik.epa.konnektor.KonnektorUtils;
import de.gematik.epa.konnektor.client.VSDServiceClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Accessors(fluent = true)
@RequiredArgsConstructor
@Getter
@Slf4j
public class ReadVSDApiImpl implements ReadVSDApi {
  private final KonnektorContextProvider contextProvider;
  private final KonnektorInterfaceAssembly konnektorInterfaceAssembly;

  @Override
  public ReadVSDResponseDTO readVSD(ReadVSDRequest request) {
    log.info("Running operation readVSD");
    try (var vsdServiceClient =
        new VSDServiceClient(contextProvider, konnektorInterfaceAssembly, request.kvnr())) {
      var konRequest = vsdServiceClient.transformRequest(request.kvnr());
      var konResponse = vsdServiceClient.readVSD(konRequest);

      return vsdServiceClient.transformResponse(konResponse);
    } catch (Exception e) {
      log.error("Operation ReadVSD failed with an exception", e);
      return new ReadVSDResponseDTO(KonnektorUtils.fromThrowable(e));
    }
  }
}
