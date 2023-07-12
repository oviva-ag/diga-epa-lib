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

import static de.gematik.epa.unit.util.TestDataFactory.getAuthorizationStateResponse;
import static de.gematik.epa.unit.util.TestDataFactory.getTelematikError;
import static de.gematik.epa.unit.util.TestDataFactory.requestFacilityAuthorizationResponse;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.epa.konnektor.KonnektorUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import telematik.ws.conn.phrs.phrmanagementservice.wsdl.v2_0.FaultMessage;
import telematik.ws.conn.phrs.phrmanagementservice.wsdl.v2_0.PHRManagementServicePortType;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_0.GetAuthorizationState;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_0.RequestFacilityAuthorization;

class PhrManagementClientTest {

  private PhrManagementClient tstObj;

  @SneakyThrows
  @BeforeEach
  void initialize() {
    tstObj = new PhrManagementClient(Mockito.mock(PHRManagementServicePortType.class));

    Mockito.when(tstObj.phrManagementService().requestFacilityAuthorization(Mockito.any()))
        .thenReturn(requestFacilityAuthorizationResponse());
    Mockito.when(tstObj.phrManagementService().getAuthorizationState(Mockito.any()))
        .thenReturn(getAuthorizationStateResponse());
  }

  @Test
  void requestFacilityAuthorizationTest() {
    var testdata = new RequestFacilityAuthorization();

    var result = assertDoesNotThrow(() -> tstObj.requestFacilityAuthorization(testdata));

    assertNotNull(result);
    assertEquals(KonnektorUtils.STATUS_OK, result.getStatus().getResult());
  }

  @SneakyThrows
  @Test
  void requestFacilityAuthorizationFaultTest() {
    var testdata = new RequestFacilityAuthorization();
    Mockito.when(tstObj.phrManagementService().requestFacilityAuthorization(Mockito.any()))
        .thenThrow(new FaultMessage("No verified SM-B found", getTelematikError()));

    var result =
        assertThrows(FaultMessage.class, () -> tstObj.requestFacilityAuthorization(testdata));

    assertNotNull(result);
    assertNotNull(result.getFaultInfo());
  }

  @Test
  void requestGetAuthorizationStateTest() {
    var testdata = new GetAuthorizationState();
    var result = assertDoesNotThrow(() -> tstObj.requestGetAuthorizationState(testdata));
    assertNotNull(result);
    assertEquals(KonnektorUtils.STATUS_OK, result.getStatus().getResult());
    assertNotNull(result.getAuthorizationStatusList());
    assertNotNull(result.getAuthorizationStatusList().getAuthorizedApplication());
    assertNotNull(
        result.getAuthorizationStatusList().getAuthorizedApplication().get(0).getApplicationName());
    assertEquals(
        "ePA",
        result.getAuthorizationStatusList().getAuthorizedApplication().get(0).getApplicationName());
    assertNotNull(
        result.getAuthorizationStatusList().getAuthorizedApplication().get(0).getValidTo());
  }

  @SneakyThrows
  @Test
  void requestGetAuthorizationStateFaultTest() {
    var testdata = new GetAuthorizationState();
    Mockito.when(tstObj.phrManagementService().getAuthorizationState(Mockito.any()))
        .thenThrow(new FaultMessage("No verified SM-B found", getTelematikError()));

    var result =
        assertThrows(FaultMessage.class, () -> tstObj.requestGetAuthorizationState(testdata));

    assertNotNull(result);
    assertNotNull(result.getFaultInfo());
  }
}
