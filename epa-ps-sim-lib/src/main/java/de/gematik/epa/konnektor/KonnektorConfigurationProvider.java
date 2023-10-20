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

package de.gematik.epa.konnektor;

import de.gematik.epa.config.KonnektorConfiguration;
import de.gematik.epa.config.KonnektorConnectionConfiguration;
import de.gematik.epa.config.KonnektorConnectionConfigurationDTO;
import de.gematik.epa.dto.request.KonnektorConfigurationRequestDTO;
import de.gematik.epa.konnektor.config.KonnektorConfigurationMutable;
import de.gematik.epa.utils.internal.Synchronizer;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;

/**
 * Provider of all relevant configuration data for the connected Konnektor.<br>
 * Intended as single point of truth and to enable runtime configuration change.
 */
@Accessors(fluent = true)
@Getter
public class KonnektorConfigurationProvider {

  @Delegate(types = KonnektorConfiguration.class)
  private final KonnektorConfigurationMutable konnektorConfiguration;

  private final Synchronizer configurationChangeSynchronizer = new Synchronizer();

  public KonnektorConfigurationProvider(KonnektorConfigurationMutable konnektorConfiguration) {
    this.konnektorConfiguration = konnektorConfiguration;
  }

  public KonnektorConfiguration updateKonnektorConfigurations(
      @NonNull KonnektorConfigurationRequestDTO newKonnektorConfiguration) {
    if (newKonnektorConfiguration.asUpdate() == Boolean.TRUE) {
      mergeKonnektorConfigurations(newKonnektorConfiguration);
    } else {
      this.konnektorConfiguration.replace(newKonnektorConfiguration);
    }

    return this.konnektorConfiguration;
  }

  private void mergeKonnektorConfigurations(
      KonnektorConfigurationRequestDTO newKonnektorConfiguration) {
    Optional.ofNullable(newKonnektorConfiguration.connection())
        .ifPresent(this::updateKonnektorConnectionKonfiguration);
    Optional.ofNullable(newKonnektorConfiguration.context())
        .ifPresent(this.konnektorConfiguration::context);
  }

  public KonnektorConnectionConfiguration updateKonnektorConnectionKonfiguration(
      @NonNull KonnektorConnectionConfigurationDTO newConnectionConfiguration) {
    if (newConnectionConfiguration.asUpdate() == Boolean.TRUE) {
      this.konnektorConfiguration.connection().update(newConnectionConfiguration);
    } else {
      this.konnektorConfiguration.connection().replace(newConnectionConfiguration);
    }

    return this.konnektorConfiguration.connection();
  }
}
