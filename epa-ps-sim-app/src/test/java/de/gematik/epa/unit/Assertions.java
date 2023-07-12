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

package de.gematik.epa.unit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.epa.ps.konnektor.config.BasicAuthenticationConfig;
import de.gematik.epa.ps.konnektor.config.ProxyAddressConfig;
import de.gematik.epa.ps.konnektor.config.TlsConfig;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

@UtilityClass
public class Assertions {

  public static void assertTlsConfig(
      @NonNull TlsConfig expectedValues, TLSClientParameters actualValues) {
    assertNotNull(actualValues);

    assertTrue(actualValues.getKeyManagers().length > 0);
    assertTrue(actualValues.getTrustManagers().length > 0);
    assertNotNull(actualValues.getCipherSuites());
    assertArrayEquals(
        expectedValues.ciphersuites().toArray(new String[0]),
        actualValues.getCipherSuites().toArray(new String[0]));
  }

  public static void assertAuthorization(
      @NonNull BasicAuthenticationConfig expectedValues, AuthorizationPolicy actualValues) {
    assertNotNull(actualValues);

    assertTrue(actualValues.isSetAuthorizationType());
    assertEquals(expectedValues.username(), actualValues.getUserName());
    assertEquals(expectedValues.password(), actualValues.getPassword());
  }

  public static void assertProxy(
      @NonNull ProxyAddressConfig expectedValues, HTTPClientPolicy actualValues) {
    assertNotNull(actualValues);

    assertEquals(expectedValues.address(), actualValues.getProxyServer());
    assertEquals(expectedValues.port(), actualValues.getProxyServerPort());
  }
}
