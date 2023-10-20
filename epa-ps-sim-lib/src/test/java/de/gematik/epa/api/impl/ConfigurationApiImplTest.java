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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.epa.config.KonnektorConnectionConfigurationDTO;
import de.gematik.epa.dto.request.KonnektorConfigurationRequestDTO;
import de.gematik.epa.konnektor.KonnektorConfigurationProvider;
import de.gematik.epa.konnektor.KonnektorContextProvider;
import de.gematik.epa.konnektor.cxf.KonnektorInterfacesCxfImpl;
import de.gematik.epa.unit.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import telematik.ws.conn.cardservice.wsdl.v8_1.CardServicePortType;
import telematik.ws.conn.cardservice.xsd.v8_1.PinStatusEnum;
import telematik.ws.conn.eventservice.wsdl.v6_1.EventServicePortType;

class ConfigurationApiImplTest {

  private KonnektorConfigurationRequestDTO requestDTO;
  private KonnektorConfigurationProvider cfgProvider;
  private KonnektorInterfacesCxfImpl konnektorInterfaces;
  private KonnektorContextProvider konnektorContextProvider;

  @BeforeEach
  void initialize() {
    var tstData = TestDataFactory.createKonnektorConfiguration();
    cfgProvider = new KonnektorConfigurationProvider(tstData);
    konnektorInterfaces = Mockito.mock(KonnektorInterfacesCxfImpl.class);
    var eventService = Mockito.mock(EventServicePortType.class);
    var cardService = Mockito.mock(CardServicePortType.class);
    Mockito.when(konnektorInterfaces.eventService()).thenReturn(eventService);
    Mockito.when(konnektorInterfaces.cardService()).thenReturn(cardService);
    Mockito.when(eventService.getCards(Mockito.any()))
        .thenReturn(TestDataFactory.getCardsSmbResponse());
    Mockito.when(cardService.getPinStatus(Mockito.any()))
        .thenReturn(TestDataFactory.getPinStatusResponse(PinStatusEnum.VERIFIED));

    konnektorContextProvider = new KonnektorContextProvider(cfgProvider, konnektorInterfaces);

    requestDTO =
        new KonnektorConfigurationRequestDTO(
            new KonnektorConnectionConfigurationDTO(
                tstData.connection().address(),
                tstData.connection().tlsConfig(),
                tstData.connection().proxyAddress(),
                tstData.connection().basicAuthentication(),
                false),
            tstData.context(),
            false);
  }

  @Test
  void configureKonnektorTest() {
    Mockito.when(konnektorInterfaces.update(requestDTO.connection()))
        .thenReturn(konnektorInterfaces);

    var tstObj =
        new ConfigurationApiImpl(cfgProvider, konnektorInterfaces, konnektorContextProvider);

    var result = assertDoesNotThrow(() -> tstObj.configureKonnektor(requestDTO));

    assertNotNull(result);
    assertTrue(result.success());
  }

  @Test
  void configureKonnektorExceptionTest() {
    var exceptionMsg = "I am the expected exception";
    Mockito.when(konnektorInterfaces.update(Mockito.any()))
        .thenThrow(new IllegalArgumentException(exceptionMsg));

    var tstObj =
        new ConfigurationApiImpl(cfgProvider, konnektorInterfaces, konnektorContextProvider);

    var result = assertDoesNotThrow(() -> tstObj.configureKonnektor(requestDTO));

    assertNotNull(result);
    assertFalse(result.success());
    assertNotNull(result.statusMessage());
    assertTrue(result.statusMessage().contains(exceptionMsg));
  }

  @Test
  void unlockSmbTest() {
    Mockito.when(konnektorInterfaces.update(requestDTO.connection()))
        .thenReturn(konnektorInterfaces);
    var tstObj =
        new ConfigurationApiImpl(cfgProvider, konnektorInterfaces, konnektorContextProvider);
    assertDoesNotThrow(tstObj::unlockSmb);
  }
}
