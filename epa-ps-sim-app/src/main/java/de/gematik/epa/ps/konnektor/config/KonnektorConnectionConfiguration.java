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

import lombok.With;
import org.springframework.boot.context.properties.bind.DefaultValue;
import telematik.ws.conn.SdsApi;
import telematik.ws.conn.servicedirectory.xsd.v3_1.ConnectorServices;

/**
 * Configuration object for the connection to the Konnektor
 *
 * @param address under which the Konnektor services can be reached and particular the {@link
 *     telematik.ws.conn.servicedirectory.xsd.v3_1.ConnectorServices} information can be retrieved.
 *     The path information must not contain the path of the {@link SdsApi#getConnectorSds()}
 *     operation.
 * @param tlsconfig configuration properties for establishing a TLS connection to the Konnektor. If
 *     it is used, is based on the value of the {@link AddressConfig#protocol()} property ("https" -
 *     TLS, "http" - no TLS) for retrieving the ConnectorServices information. For the Konnektor
 *     webservices TLS will also be used if {@link ConnectorServices#isTLSMandatory()} is true.
 * @param proxyaddress to be set, if the Konnektor is behind a proxy. If this is not present or
 *     {@link ProxyAddressConfig#enabled()} is false, no proxy will be set.
 * @param basicauthentication to be set, if the Konnektor requires basic authentication. If this is
 *     not present or {@link BasicAuthenticationConfig#enabled()} is false, no basic authentication
 *     will be used.
 */
@With
public record KonnektorConnectionConfiguration(
    @DefaultValue AddressConfig address,
    @DefaultValue TlsConfig tlsconfig,
    @DefaultValue ProxyAddressConfig proxyaddress,
    @DefaultValue BasicAuthenticationConfig basicauthentication) {}
