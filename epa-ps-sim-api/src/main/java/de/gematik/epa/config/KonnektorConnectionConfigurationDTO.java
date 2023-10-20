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

package de.gematik.epa.config;

import static de.gematik.epa.constants.Documentation.AS_UPDATE_DESCRIPTION;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Configuration object for the connection to the Konnektor
 *
 * @param address under which the Konnektor services can be reached.
 * @param tlsConfig configuration properties for establishing a TLS connection to the Konnektor.
 * @param proxyAddress to be set, if the Konnektor is behind a proxy. If this is not present or
 *     {@link ProxyAddressConfig#enabled()} is false, no proxy will be set.
 * @param basicAuthentication to be set, if the Konnektor requires basic authentication. If this is
 *     not present or {@link BasicAuthenticationConfig#enabled()} is false, no basic authentication
 *     will be used.
 * @param asUpdate Switch, whether the submitted data should update the already configured data
 *     (true) or replace them (false).<br>
 *     Update means, that for an element not set, the already configured data will continue to be
 *     used, sparing the caller the need to resubmit unchanged data.
 */
@Schema(
    description = "Konfigurationsinformationen um die Verbindung zum Konnektor zu konfigurieren")
public record KonnektorConnectionConfigurationDTO(
    @Schema(description = "Daten der Konnektor-Adresse") AddressConfig address,
    @Schema(
            description =
                "Daten zur Herstellung einer TLS Verbindung zwischen epa-ps-sim und Konnektor")
        TlsConfig tlsConfig,
    @Schema(
            description =
                "Adressdaten eines Proxies, der sich ggf. zwischen epa-ps-sim und Konnektor befindet")
        ProxyAddressConfig proxyAddress,
    @Schema(
            description =
                "Authentifizierungsdaten für eine Basic Authentication gegenüber dem Konnektor")
        BasicAuthenticationConfig basicAuthentication,
    @Schema(description = AS_UPDATE_DESCRIPTION) Boolean asUpdate)
    implements KonnektorConnectionConfiguration {}
