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

package de.gematik.epa.unit.util;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.epa.config.BasicAuthenticationConfig;
import de.gematik.epa.config.ProxyAddressConfig;
import de.gematik.epa.config.TlsConfig;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.opentest4j.AssertionFailedError;
import telematik.ws.conn.connectorcontext.xsd.v2_0.ContextType;
import telematik.ws.conn.phrs.phrservice.xsd.v2_0.ContextHeader;
import telematik.ws.fd.phr.phrcommon.xsd.v1_1.InsurantIdType;
import telematik.ws.fd.phr.phrcommon.xsd.v1_1.RecordIdentifierType;

@UtilityClass
public class Assertions {

  public static void assertEquals(@NonNull ContextHeader expected, @NonNull ContextHeader actual) {
    if (actual.equals(expected)) return;

    try {
      assertEquals(expected.getContext(), actual.getContext());
      assertEquals(expected.getRecordIdentifier(), actual.getRecordIdentifier());
    } catch (AssertionError ae) {
      throw new AssertionFailedError("ContextHeader are not the same", expected, actual, ae);
    }
  }

  public static void assertEquals(@NonNull ContextType expected, @NonNull ContextType actual) {
    if (actual.equals(expected)
        || (actual.getMandantId().equals(expected.getMandantId())
            && actual.getClientSystemId().equals(expected.getClientSystemId())
            && actual.getWorkplaceId().equals(expected.getWorkplaceId())
            && actual.getUserId().equals(expected.getUserId()))) return;

    throw new AssertionFailedError("Contexts are not the same", expected, actual);
  }

  public static void assertEquals(
      @NonNull RecordIdentifierType expected, @NonNull RecordIdentifierType actual) {
    if (actual.equals(expected)) return;

    if (!actual.getHomeCommunityId().equals(expected.getHomeCommunityId()))
      throw new AssertionFailedError(
          "HomeCommunityIds are not the same",
          expected.getHomeCommunityId(),
          actual.getHomeCommunityId());

    try {
      assertEquals(expected.getInsurantId(), actual.getInsurantId());
    } catch (AssertionError ae) {
      throw new AssertionFailedError("RecordIdentifier are not the same", expected, actual, ae);
    }
  }

  public static void assertEquals(
      @NonNull InsurantIdType expected, @NonNull InsurantIdType actual) {
    if (actual.equals(expected)
        || (actual.getExtension().equals(expected.getExtension())
            && actual.getRoot().equals(expected.getRoot()))) return;

    throw new AssertionFailedError("InsurantIds are not the same", expected, actual);
  }

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
    org.junit.jupiter.api.Assertions.assertEquals(
        expectedValues.username(), actualValues.getUserName());
    org.junit.jupiter.api.Assertions.assertEquals(
        expectedValues.password(), actualValues.getPassword());
  }

  public static void assertProxy(
      @NonNull ProxyAddressConfig expectedValues, HTTPClientPolicy actualValues) {
    assertNotNull(actualValues);

    org.junit.jupiter.api.Assertions.assertEquals(
        expectedValues.address(), actualValues.getProxyServer());
    org.junit.jupiter.api.Assertions.assertEquals(
        expectedValues.port(), actualValues.getProxyServerPort());
  }
}
