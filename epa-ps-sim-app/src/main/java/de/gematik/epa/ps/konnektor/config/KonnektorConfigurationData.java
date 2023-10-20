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

package de.gematik.epa.ps.konnektor.config;

import de.gematik.epa.config.Context;
import de.gematik.epa.konnektor.config.KonnektorConfigurationMutable;
import de.gematik.epa.konnektor.config.KonnektorConnectionConfigurationMutable;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Data
@Accessors(fluent = true)
@RequiredArgsConstructor
@ConfigurationProperties("konnektor")
public class KonnektorConfigurationData implements KonnektorConfigurationMutable {

  private KonnektorConnectionConfigurationData connection;

  private Context context;

  @ConstructorBinding
  public KonnektorConfigurationData(
      KonnektorConnectionConfigurationData connection, Context context) {
    this.connection = connection;
    this.context = context;
  }

  @Override
  public KonnektorConfigurationData connection(
      KonnektorConnectionConfigurationMutable konnektorConnectionConfiguration) {
    if (connection == null) connection = new KonnektorConnectionConfigurationData();
    connection.replace(konnektorConnectionConfiguration);
    return this;
  }
}
