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

package de.gematik.epa.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.epa.config.AddressConfig;
import de.gematik.epa.config.BasicAuthenticationConfig;
import de.gematik.epa.config.Context;
import de.gematik.epa.config.FileInfo;
import de.gematik.epa.config.KonnektorConnectionConfigurationDTO;
import de.gematik.epa.config.ProxyAddressConfig;
import de.gematik.epa.config.TlsConfig;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.junit.jupiter.api.Test;

class KonnektorConfigurationRequestDTOTest {

  @Test
  void konnektorConfigurationRequestDTOTest() {
    var address = new AddressConfig("localhost", 80, "http", "path");
    var proxyAddress = new ProxyAddressConfig("remotehost", 3128, false);
    var basicAuthentication = new BasicAuthenticationConfig("root", "root", false);
    var tlsConfig = new TlsConfig(new FileInfo("keystore.p12"), "00", "PKCS12", List.of());

    var connectionCfg =
        new KonnektorConnectionConfigurationDTO(
            address, tlsConfig, proxyAddress, basicAuthentication, true);

    var context = new Context("Mandant", "Clientsystem", "Workplace", null);

    var konCfg =
        assertDoesNotThrow(
            () -> new KonnektorConfigurationRequestDTO(connectionCfg, context, true));

    var addressUrl = assertDoesNotThrow(() -> konCfg.connection().address().createUrl());

    var otherAddress =
        new AddressConfig(
            addressUrl.getHost(),
            addressUrl.getPort(),
            addressUrl.getProtocol(),
            addressUrl.getPath());

    try {
      assertEquals(address.createUrl(), otherAddress.createUrl());
    } catch (MalformedURLException mue) {
      fail(mue);
    }
  }

  @Test
  void testGetUrlWithPathNotNull() throws MalformedURLException {
    AddressConfig address = new AddressConfig("localhost", 80, "http", "kon16");
    URL url = address.createUrl();
    assertNotNull(url);
    assertEquals("http://localhost:80/kon16", url.toString());
  }

  @Test
  void testGetUrlWithPathNull() throws MalformedURLException {
    AddressConfig address = new AddressConfig("localhost", 80, "http", null);
    URL url = address.createUrl();
    assertNotNull(url);
    assertEquals("http://localhost:80/", url.toString());
  }
}
