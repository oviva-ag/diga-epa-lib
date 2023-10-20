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

import de.gematik.epa.config.AddressConfig;
import de.gematik.epa.config.BasicAuthenticationConfig;
import de.gematik.epa.config.ProxyAddressConfig;
import de.gematik.epa.config.TlsConfig;
import de.gematik.epa.konnektor.config.KonnektorConnectionConfigurationMutable;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Data
@RequiredArgsConstructor
@Accessors(fluent = true)
public class KonnektorConnectionConfigurationData
    implements KonnektorConnectionConfigurationMutable {

  private AddressConfig address;

  private TlsConfig tlsConfig;

  private ProxyAddressConfig proxyAddress;

  private BasicAuthenticationConfig basicAuthentication;

  @ConstructorBinding
  public KonnektorConnectionConfigurationData(
      AddressConfig address,
      TlsConfig tlsConfig,
      ProxyAddressConfig proxyAddress,
      BasicAuthenticationConfig basicAuthentication) {
    this.address = address;
    this.tlsConfig = tlsConfig;
    this.proxyAddress = proxyAddress;
    this.basicAuthentication = basicAuthentication;
  }
}
