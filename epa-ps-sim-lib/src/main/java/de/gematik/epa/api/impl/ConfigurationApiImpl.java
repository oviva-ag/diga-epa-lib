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

import de.gematik.epa.api.ConfigurationApi;
import de.gematik.epa.dto.request.KonnektorConfigurationRequestDTO;
import de.gematik.epa.dto.response.ResponseDTO;
import de.gematik.epa.konnektor.KonnektorConfigurationProvider;
import de.gematik.epa.konnektor.KonnektorContextProvider;
import de.gematik.epa.konnektor.KonnektorUtils;
import de.gematik.epa.konnektor.client.CardServiceClient;
import de.gematik.epa.konnektor.cxf.KonnektorInterfacesCxfImpl;
import jakarta.ws.rs.InternalServerErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ConfigurationApiImpl implements ConfigurationApi {

  private final KonnektorConfigurationProvider configurationProvider;

  private final KonnektorInterfacesCxfImpl konnektorInterfacesCxf;

  private final KonnektorContextProvider contextProvider;

  @Override
  public ResponseDTO configureKonnektor(KonnektorConfigurationRequestDTO request) {
    try {
      log.info("Running operation configureKonnektor");
      configurationProvider
          .configurationChangeSynchronizer()
          .runBlocking(
              () -> {
                configurationProvider.updateKonnektorConfigurations(request);
                konnektorInterfacesCxf.update(configurationProvider.connection());
                unlockSmb();
              });
      return new ResponseDTO(true, "Konnektor configuration update completed");
    } catch (Exception e) {
      log.error("Operation configureKonnektor failed with an exception", e);
      return KonnektorUtils.fromThrowable(e);
    }
  }

  protected void unlockSmb() {
    try (CardServiceClient cardServiceClient =
        new CardServiceClient(contextProvider, konnektorInterfacesCxf)) {
      cardServiceClient.verifySmb();
    } catch (Exception e) {
      throw new InternalServerErrorException("Operation unlockSmb failed with an exception", e);
    }
  }
}
