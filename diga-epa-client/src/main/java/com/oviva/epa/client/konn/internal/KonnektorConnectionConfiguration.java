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

package com.oviva.epa.client.konn.internal;

import java.net.URI;
import java.util.List;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

public record KonnektorConnectionConfiguration(

    /** the URI to the Konnektor APIs (Fachmodule) */
    URI uri,
    TlsConfig tlsConfig,
    ProxyAddressConfig proxyAddress,
    BasicAuthenticationConfig basicAuthentication) {

  public record TlsConfig(
      List<KeyManager> keyManagers,
      List<TrustManager> trustManagers,

      // Liste der zulässigen Ciphersuiten",
      // example: "['TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384',
      // 'TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256', 'TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384',
      // 'TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256', 'TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA',
      // 'TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA']")
      List<String> ciphersuites) {}

  public record ProxyAddressConfig(String address, Integer port, boolean enabled) {}

  public record BasicAuthenticationConfig(String username, String password, boolean enabled) {}
}
