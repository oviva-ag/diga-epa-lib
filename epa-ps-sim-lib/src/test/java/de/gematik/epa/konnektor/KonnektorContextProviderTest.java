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

package de.gematik.epa.konnektor;

import static de.gematik.epa.unit.util.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.epa.unit.util.TestBase;
import de.gematik.epa.unit.util.TestDataFactory;
import java.util.MissingResourceException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import telematik.ws.conn.phrs.phrmanagementservice.wsdl.v2_5.FaultMessage;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.GetHomeCommunityID;

class KonnektorContextProviderTest extends TestBase {

  @BeforeEach
  void beforeEach() {
    TestDataFactory.initKonnektorTestConfiguration(konnektorInterfaceAssembly());
  }

  @SneakyThrows
  @Test
  void createContextHeaderTest() {
    var tstObj =
        new KonnektorContextProvider(
            konnektorConfigurationProvider(), konnektorInterfaceAssembly());
    var firstResult = assertDoesNotThrow(() -> tstObj.createContextHeader(TestDataFactory.KVNR));

    assertEquals(TestDataFactory.contextHeader(), firstResult);

    var secondResult = assertDoesNotThrow(() -> tstObj.getContextHeader());

    assertSame(firstResult, secondResult);
  }

  @SneakyThrows
  @Test
  void contextProviderCacheTest() {
    var tstObj =
        new KonnektorContextProvider(
            konnektorConfigurationProvider(), konnektorInterfaceAssembly());

    var firstResult = assertDoesNotThrow(() -> tstObj.createContextHeader(TestDataFactory.KVNR));

    var phrManagementServiceMock = konnektorInterfaceAssembly().phrManagementService();
    Mockito.when(phrManagementServiceMock.getHomeCommunityID(Mockito.any(GetHomeCommunityID.class)))
        .thenThrow(
            new FaultMessage(
                "The PHRManagementService should not have been called a second time for the same KVNR"));

    assertDoesNotThrow(tstObj::removeContextHeader);

    var secondResult = assertDoesNotThrow(() -> tstObj.createContextHeader(TestDataFactory.KVNR));

    assertEquals(firstResult, secondResult);
    assertNotSame(firstResult, secondResult);
  }

  @Test
  void getContextTest() {
    var tstObj =
        new KonnektorContextProvider(
            konnektorConfigurationProvider(), konnektorInterfaceAssembly());
    var context = assertDoesNotThrow(tstObj::getContext);

    assertNotNull(context);
    assertEquals(TestDataFactory.contextHeader().getContext(), context);
  }

  @Test
  void getContextFromHeaderTest() {
    var tstObj =
        new KonnektorContextProvider(
            konnektorConfigurationProvider(), konnektorInterfaceAssembly());
    tstObj.createContextHeader(TestDataFactory.KVNR);

    var context = assertDoesNotThrow(tstObj::getContext);

    assertNotNull(context);
    assertEquals(TestDataFactory.contextType(), context);
  }

  @Test
  void getContextThrowsTest() {
    var konCfgProvider =
        new KonnektorConfigurationProvider(
            TestDataFactory.createKonnektorConfiguration().context(null));
    var konCtxProvider = new KonnektorContextProvider(konCfgProvider, konnektorInterfaceAssembly());

    assertThrows(MissingResourceException.class, konCtxProvider::getContext);
  }

  @Test
  void getRecordIdentifierTest() {
    var tstObj =
        new KonnektorContextProvider(
            konnektorConfigurationProvider(), konnektorInterfaceAssembly());
    tstObj.createContextHeader(TestDataFactory.KVNR);

    var recordIdentifier = tstObj.getRecordIdentifier();

    assertNotNull(recordIdentifier);
    assertEquals(TestDataFactory.recordIdentifier(), recordIdentifier);
  }

  @Test
  void getRecordIdentifierThrowsTest() {
    var konCtxProvider =
        new KonnektorContextProvider(
            konnektorConfigurationProvider(), konnektorInterfaceAssembly());
    konCtxProvider.removeContextHeader();

    assertThrows(MissingResourceException.class, konCtxProvider::getRecordIdentifier);
  }
}
