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

import static de.gematik.epa.unit.util.TestDataFactory.KVNR;
import static de.gematik.epa.unit.util.TestDataFactory.getCardsEgkResponse;
import static de.gematik.epa.unit.util.TestDataFactory.setupMocksForSmbInformationProvider;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import de.gematik.epa.dto.request.ReadVSDRequest;
import de.gematik.epa.dto.response.ReadVSDResponseDTO;
import de.gematik.epa.unit.util.ResourceLoader;
import de.gematik.epa.unit.util.TestBase;
import de.gematik.epa.unit.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import telematik.ws.conn.vsds.vsdservice.wsdl.v5_2.VSDServicePortType;
import telematik.ws.conn.vsds.vsdservice.xsd.v5_2.ReadVSD;

class ReadVSDApiImplTest extends TestBase {

  private final ReadVSDApiImpl tstObj =
      new ReadVSDApiImpl(konnektorContextProvider(), konnektorInterfaceAssembly());

  VSDServicePortType vsdServiceMock = konnektorInterfaceAssembly().vsdService();

  @BeforeEach
  void initialize() {
    setupMocksForSmbInformationProvider(konnektorInterfaceAssembly());
    Mockito.when(konnektorInterfaceAssembly().eventService().getCards(any()))
        .thenReturn(getCardsEgkResponse(KVNR));

    Mockito.when(vsdServiceMock.readVSD(Mockito.any(ReadVSD.class)))
        .thenReturn(TestDataFactory.readVSDResponse());
  }

  @Test
  void readVSDTest() {
    ReadVSDRequest request = ResourceLoader.readVSDRequest();
    ReadVSDResponseDTO expectedResponseDTO =
        new ReadVSDResponseDTO(true, "ReadVSD operation was successful", 1);
    ReadVSDResponseDTO actualResponseDTO = assertDoesNotThrow(() -> tstObj.readVSD(request));
    assertEquals(expectedResponseDTO, actualResponseDTO);
  }

  @Test
  void readVSDExceptionTest() {
    var exception = new RuntimeException("I am the expected exception");
    Mockito.when(vsdServiceMock.readVSD(Mockito.any())).thenThrow(exception);
    ReadVSDRequest request = new ReadVSDRequest(KVNR);

    var actualResponseDTO = assertDoesNotThrow(() -> tstObj.readVSD(request));
    assertNotNull(actualResponseDTO);
    assertFalse(actualResponseDTO.success());
    assertTrue(actualResponseDTO.statusMessage().contains(exception.getMessage()));
  }
}
