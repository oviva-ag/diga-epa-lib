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

package de.gematik.epa.ps.konnektor.impl;

import static de.gematik.epa.unit.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.epa.konnektor.KonnektorInterfaceProvider;
import de.gematik.epa.unit.AppTestDataFactory;
import de.gematik.epa.unit.TestKonnektorInterfaceImplGenerator;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import telematik.ws.conn.SdsApi;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestKonnektorInterfaceImplGenerator.class})
class KonnektorInterfaceImplGeneratorTest {

  @Autowired TestKonnektorInterfaceImplGenerator testKonnektorInterfaceImplGenerator;

  @Test
  void provideInterfaceAssemblyTest() {
    var interfaceAssembly =
        assertDoesNotThrow(
            () -> KonnektorInterfaceProvider.defaultInstance().getKonnektorInterfaceAssembly());

    assertNotNull(interfaceAssembly);
    assertNotNull(interfaceAssembly.phrService());
    assertNotNull(interfaceAssembly.phrManagementService());
    assertNotNull(interfaceAssembly.eventService());
    assertNotNull(interfaceAssembly.cardService());
    assertNotNull(interfaceAssembly.certificateService());
    assertNotNull(interfaceAssembly.signatureService());
  }

  @Test
  void sdsApiTest() {
    var config = AppTestDataFactory.createKonnektorConfiguration();
    config =
        config.withConnection(
            config
                .connection()
                .withTlsconfig(
                    testKonnektorInterfaceImplGenerator
                        .konnektorConfiguration
                        .connection()
                        .tlsconfig()));

    var konnektorInterfaceImplGenerator =
        new KonnektorInterfaceImplGenerator(config, new DefaultResourceLoader());

    var sdsApi = assertDoesNotThrow(konnektorInterfaceImplGenerator::sdsApi);

    assertNotNull(sdsApi);

    var sdsHttpConduit = WebClient.getConfig(sdsApi).getHttpConduit();

    assertNotNull(sdsHttpConduit);

    assertTlsConfig(config.connection().tlsconfig(), sdsHttpConduit.getTlsClientParameters());

    assertAuthorization(
        config.connection().basicauthentication(), sdsHttpConduit.getAuthorization());

    assertProxy(config.connection().proxyaddress(), sdsHttpConduit.getClient());
  }

  @Test
  void getJaxWSClientProxyTest() {
    var config = AppTestDataFactory.createKonnektorConfiguration();
    config =
        config.withConnection(
            config
                .connection()
                .withTlsconfig(
                    testKonnektorInterfaceImplGenerator
                        .konnektorConfiguration
                        .connection()
                        .tlsconfig()));

    var resourceLoader = new DefaultResourceLoader();

    var konnektorInterfaceImplGenerator =
        new KonnektorInterfaceImplGenerator(config, resourceLoader) {
          @Override
          protected SdsApi sdsApi() {
            return TestKonnektorInterfaceImplGenerator.mockSdsApi(resourceLoader);
          }
        };

    var phrServiceClientProxy = assertDoesNotThrow(konnektorInterfaceImplGenerator::phrService);

    assertNotNull(phrServiceClientProxy);

    var phrHttpConduit = (HTTPConduit) ClientProxy.getClient(phrServiceClientProxy).getConduit();

    assertNotNull(phrHttpConduit);

    assertTlsConfig(config.connection().tlsconfig(), phrHttpConduit.getTlsClientParameters());

    assertAuthorization(
        config.connection().basicauthentication(), phrHttpConduit.getAuthorization());

    assertProxy(config.connection().proxyaddress(), phrHttpConduit.getClient());
  }

  @Test
  void constructorTest() {
    var result =
        assertDoesNotThrow(
            () ->
                new KonnektorInterfaceImplGenerator(
                    AppTestDataFactory.createKonnektorConfiguration(),
                    new DefaultResourceLoader()));

    assertNotNull(result.konnektorConfiguration());
    assertNotNull(result.resourceLoader());
    assertNotNull(result.konnektorContextProvider());
    assertNotNull(result.konnektorInterfaceProvider());
  }
}
