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

package de.gematik.epa.konnektor.client;

import static de.gematik.epa.unit.util.TestDataFactory.KVNR;
import static de.gematik.epa.unit.util.TestDataFactory.getCardsEgkResponse;
import static de.gematik.epa.unit.util.TestDataFactory.readVSDResponse;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.epa.unit.util.ResourceLoader;
import de.gematik.epa.unit.util.TestBase;
import de.gematik.epa.unit.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import telematik.ws.conn.vsds.vsdservice.xsd.v5_2.ReadVSD;

class VSDServiceClientTest extends TestBase {

  private VSDServiceClient tstObj;

  @BeforeEach
  void beforeEach() {
    TestDataFactory.initKonnektorTestConfiguration(konnektorInterfaceAssembly());
    tstObj =
        new VSDServiceClient(
            konnektorContextProvider(), konnektorInterfaceAssembly(), TestDataFactory.KVNR);
  }

  @Test
  void transformRequestTest() {
    Mockito.when(konnektorInterfaceAssembly().eventService().getCards(Mockito.any()))
        .thenReturn(getCardsEgkResponse(KVNR));
    var readVSDRequest = ResourceLoader.readVSDRequest();
    var konReadVSDRequest =
        assertDoesNotThrow(() -> tstObj.transformRequest(readVSDRequest.kvnr()));
    System.out.println(konReadVSDRequest);
    assertNotNull(konReadVSDRequest);
    assertTrue(konReadVSDRequest.isPerformOnlineCheck());
    assertTrue(konReadVSDRequest.isReadOnlineReceipt());
    assertNotNull(konReadVSDRequest.getEhcHandle());
    assertNotNull(konReadVSDRequest.getHpcHandle());
    assertEquals("EGK456", konReadVSDRequest.getEhcHandle());
  }

  @Test
  void transformResponseTest() {
    var konReadVSDResponse = TestDataFactory.readVSDResponse();
    var readVSDResponse = assertDoesNotThrow(() -> tstObj.transformResponse(konReadVSDResponse));

    assertNotNull(readVSDResponse);
    assertTrue(readVSDResponse.success());
    assertEquals(1, readVSDResponse.resultOfOnlineCheckEGK());
  }

  @Test
  void readVSDTest() {
    Mockito.when(konnektorInterfaceAssembly().vsdService().readVSD(Mockito.any()))
        .thenReturn(readVSDResponse());
    var readVSDRequest = new ReadVSD();
    var response = assertDoesNotThrow(() -> tstObj.readVSD(readVSDRequest));
    assertNotNull(response);
  }
}
