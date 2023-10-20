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

package de.gematik.epa.konnektor.cxf;

import static de.gematik.epa.unit.util.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.epa.config.AddressConfig;
import de.gematik.epa.config.KonnektorConnectionConfiguration;
import de.gematik.epa.konnektor.cxf.KonnektorInterfacesCxfImpl.FileLoader;
import de.gematik.epa.unit.util.ResourceLoader;
import de.gematik.epa.unit.util.TestDataFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import lombok.experimental.Accessors;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import telematik.ws.conn.SdsApi;

class KonnektorInterfacesCxfImplTest {

  @Test
  void updateTest() {
    var connCfg = TestDataFactory.createKonnektorConnectionConfiguration();
    var tstObj = new KonnektorInterfacesCxfImplForTest();

    var alsoTstObj = assertDoesNotThrow(() -> tstObj.update(connCfg));

    assertEquals(connCfg, alsoTstObj.configuration);
    assertTrue(alsoTstObj.isTlsPreferred());
    assertEquals(ResourceLoader.connectorServices(), alsoTstObj.connectorServices());
    assertClientProxy(alsoTstObj.phrService(), connCfg);
    assertClientProxy(alsoTstObj.phrManagementService(), connCfg);
    assertClientProxy(alsoTstObj.eventService(), connCfg);
    assertClientProxy(alsoTstObj.certificateService(), connCfg);
    assertClientProxy(alsoTstObj.cardService(), connCfg);
    assertClientProxy(alsoTstObj.signatureService(), connCfg);
  }

  @Test
  void sdsApiTest() {
    var connCfg = TestDataFactory.createKonnektorConnectionConfiguration();
    var tstObj = new KonnektorInterfacesCxfImpl(new TestFileLoader());
    tstObj.configuration = connCfg;

    var sdsApi = assertDoesNotThrow(tstObj::sdsApi);

    assertNotNull(sdsApi);

    var sdsHttpConduit = WebClient.getConfig(sdsApi).getHttpConduit();

    assertHttpConduit(sdsHttpConduit, connCfg);
  }

  @Test
  void sdsApiHttpTest() {
    var connCfg = TestDataFactory.createKonnektorConnectionConfiguration();
    connCfg.address(new AddressConfig("localhost", 80, "http", "the/path"));
    var tstObj = new KonnektorInterfacesCxfImpl(new TestFileLoader());
    tstObj.configuration = connCfg;
    tstObj.isTlsPreferred = Boolean.FALSE;

    var sdsApi = assertDoesNotThrow(tstObj::sdsApi);

    assertNotNull(sdsApi);

    var sdsHttpConduit = WebClient.getConfig(sdsApi).getHttpConduit();

    assertNull(sdsHttpConduit.getTlsClientParameters());
  }

  private void assertClientProxy(
      Object clientProxyObject, KonnektorConnectionConfiguration connCfg) {
    assertNotNull(clientProxyObject);

    var httpConduit = (HTTPConduit) ClientProxy.getClient(clientProxyObject).getConduit();

    assertHttpConduit(httpConduit, connCfg);
  }

  private void assertHttpConduit(
      HTTPConduit httpConduit, KonnektorConnectionConfiguration connCfg) {
    assertNotNull(httpConduit);

    assertTlsConfig(connCfg.tlsConfig(), httpConduit.getTlsClientParameters());

    assertAuthorization(connCfg.basicAuthentication(), httpConduit.getAuthorization());

    assertProxy(connCfg.proxyAddress(), httpConduit.getClient());
  }

  @Accessors(fluent = true)
  static class KonnektorInterfacesCxfImplForTest extends KonnektorInterfacesCxfImpl {

    public KonnektorInterfacesCxfImplForTest() {
      super(new TestFileLoader());
    }

    @Override
    protected SdsApi sdsApi() {
      var thisSdsApi = Mockito.mock(SdsApi.class);

      Mockito.when(thisSdsApi.getConnectorSds()).thenReturn(ResourceLoader.connectorServices());

      return thisSdsApi;
    }
  }

  static class TestFileLoader implements FileLoader {

    @Override
    public InputStream process(String filePath) {
      return new ByteArrayInputStream(ResourceLoader.readBytesFromResource(filePath));
    }
  }
}
