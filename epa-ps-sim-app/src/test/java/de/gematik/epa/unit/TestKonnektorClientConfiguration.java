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

package de.gematik.epa.unit;

import static de.gematik.epa.unit.AppTestDataFactory.getCardsSmbResponse;
import static de.gematik.epa.unit.AppTestDataFactory.getPinStatusResponse;

import de.gematik.epa.konnektor.cxf.KonnektorInterfacesCxfImpl;
import de.gematik.epa.ps.konnektor.KonnektorClientConfiguration;
import de.gematik.epa.ps.konnektor.config.KonnektorConfigurationData;
import de.gematik.epa.ps.utils.SpringUtils;
import org.mockito.Mockito;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import telematik.ws.conn.cardservice.wsdl.v8_1.CardServicePortType;
import telematik.ws.conn.cardservice.xsd.v8_1.PinStatusEnum;
import telematik.ws.conn.eventservice.wsdl.v6_1.EventServicePortType;

@TestConfiguration
@ComponentScan("de.gematik.epa.ps")
@Profile("test")
@EnableConfigurationProperties(KonnektorConfigurationData.class)
public class TestKonnektorClientConfiguration extends KonnektorClientConfiguration {

  public TestKonnektorClientConfiguration(
      KonnektorConfigurationData konnektorConfiguration, ResourceLoader resourceLoader) {
    super(konnektorConfiguration, resourceLoader);
  }

  @Override
  protected KonnektorInterfacesCxfImpl createNewKonnektorInterfaceAssembly() {
    return createKonnektorInterfaceCxfImpl();
  }

  protected KonnektorInterfacesCxfImpl createKonnektorInterfaceCxfImpl() {
    KonnektorInterfacesCxfImpl konnektorInterfacesCxf =
        new KonnektorInterfacesCxfImpl(
            filePath ->
                SpringUtils.findReadableResource(resourceLoader, filePath).getInputStream());
    var eventService = Mockito.mock(EventServicePortType.class);
    var cardService = Mockito.mock(CardServicePortType.class);
    konnektorInterfacesCxf.cardService(cardService);
    konnektorInterfacesCxf.eventService(eventService);

    Mockito.when(eventService.getCards(Mockito.any())).thenReturn(getCardsSmbResponse());
    Mockito.when(cardService.getPinStatus(Mockito.any()))
        .thenReturn(getPinStatusResponse(PinStatusEnum.VERIFIED));
    return konnektorInterfacesCxf;
  }
}
