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

package de.gematik.epa.api.impl;

import static de.gematik.epa.unit.util.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import de.gematik.epa.dto.request.FolderCode;
import de.gematik.epa.dto.request.GetAuthorizationStateRequest;
import de.gematik.epa.dto.request.PermissionHcpoRequest;
import de.gematik.epa.unit.util.TestBase;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import telematik.ws.conn.phrs.phrmanagementservice.wsdl.v2_5.FaultMessage;

class PermissionApiImplTest extends TestBase {

  private PermissionApiImpl tstObj;

  @BeforeEach
  void initialize() {
    tstObj = new PermissionApiImpl(konnektorContextProvider(), konnektorInterfaceAssembly());
    setupMocksForSmbInformationProvider(konnektorInterfaceAssembly());

    Mockito.when(konnektorInterfaceAssembly().eventService().getCards(any()))
        .thenReturn(getCardsEgkResponse(KVNR));
    Mockito.when(
            konnektorInterfaceAssembly().phrManagementService().requestFacilityAuthorization(any()))
        .thenReturn(requestFacilityAuthorizationResponse());
    Mockito.when(konnektorInterfaceAssembly().phrManagementService().getAuthorizationState(any()))
        .thenReturn(getAuthorizationStateResponse());
    Mockito.when(konnektorInterfaceAssembly().phrManagementService().getHomeCommunityID(any()))
        .thenReturn(getHomeCommunityIDResponse());
  }

  @Test
  void permissionHcpoTest() {
    var testdata =
        new PermissionHcpoRequest(KVNR, Set.of(FolderCode.OTHER_MEDICAL, FolderCode.CHILDSRECORD));

    var result = assertDoesNotThrow(() -> tstObj.permissionHcpo(testdata));

    assertNotNull(result);
    assertTrue(result.success());
    assertNull(result.statusMessage());
  }

  @Test
  void permissionHcpoFaultTest() {
    var testdata =
        new PermissionHcpoRequest(KVNR, Set.of(FolderCode.OTHER_MEDICAL, FolderCode.CHILDSRECORD));
    var fault = new FaultMessage("I am the expected exception", getTelematikError());
    Mockito.when(
            konnektorInterfaceAssembly().phrManagementService().requestFacilityAuthorization(any()))
        .thenThrow(fault);

    var result = assertDoesNotThrow(() -> tstObj.permissionHcpo(testdata));

    assertNotNull(result);
    assertFalse(result.success());
    assertNotNull(result.statusMessage());
    assertTrue(result.statusMessage().contains(fault.getMessage()));
    assertTrue(result.statusMessage().contains(fault.getFaultInfo().toString()));
  }

  @Test
  void getAuthorizationStateTest() {
    var testdata = new GetAuthorizationStateRequest(KVNR);
    var result = assertDoesNotThrow(() -> tstObj.getAuthorizationState(testdata));
    assertNotNull(result);
    assertTrue(result.success());
    assertNull(result.statusMessage());
    assertEquals(1, result.authorizedApplications().size());
    assertEquals("ePA", result.authorizedApplications().get(0).applicationName());
  }

  @Test
  void getAuthorizationStateFaultTest() {
    var testdata = new GetAuthorizationStateRequest(KVNR);
    var fault = new FaultMessage("I am the expected exception", getTelematikError());
    Mockito.when(konnektorInterfaceAssembly().phrManagementService().getAuthorizationState(any()))
        .thenThrow(fault);

    var result = assertDoesNotThrow(() -> tstObj.getAuthorizationState(testdata));

    assertNotNull(result);
    assertFalse(result.success());
    assertNotNull(result.statusMessage());
    assertTrue(result.statusMessage().contains(fault.getMessage()));
    assertTrue(result.statusMessage().contains(fault.getFaultInfo().toString()));
  }
}
