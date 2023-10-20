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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.epa.unit.util.KonnektorInterfaceAnswer;
import de.gematik.epa.unit.util.TestBase;
import de.gematik.epa.unit.util.TestDataFactory;
import java.util.NoSuchElementException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import telematik.ws.conn.cardservice.xsd.v8_1.Cards;
import telematik.ws.conn.cardservicecommon.xsd.v2_0.CardTypeType;
import telematik.ws.conn.eventservice.wsdl.v6_1.FaultMessage;
import telematik.ws.conn.eventservice.xsd.v6_1.GetCards;
import telematik.ws.conn.eventservice.xsd.v6_1.GetCardsResponse;

class EventServiceClientTest extends TestBase {

  @BeforeEach
  void beforeEach() {
    TestDataFactory.initKonnektorTestConfiguration(konnektorInterfaceAssembly());
  }

  @SneakyThrows
  @Test
  void getSmbInfo() {
    var eventServiceMock = konnektorInterfaceAssembly().eventService();
    var answer =
        new KonnektorInterfaceAnswer<GetCards, GetCardsResponse>()
            .setAnswer(TestDataFactory.getCardsSmbResponse());

    Mockito.when(eventServiceMock.getCards(Mockito.any())).then(answer);

    var result =
        assertDoesNotThrow(
            () ->
                new EventServiceClient(konnektorContextProvider(), konnektorInterfaceAssembly())
                    .getSmbInfo());

    assertNotNull(result);

    var request = answer.getRequest();
    assertNotNull(request);
    assertEquals(CardTypeType.SM_B, request.getCardType());
  }

  @SneakyThrows
  @Test
  void getCardsThrowsTest() {
    var eventServiceMock = konnektorInterfaceAssembly().eventService();

    var faultMessage = new FaultMessage("Some error occurred", TestDataFactory.getTelematikError());

    Mockito.when(eventServiceMock.getCards(Mockito.any())).thenThrow(faultMessage);

    var tstObj = new EventServiceClient(konnektorContextProvider(), konnektorInterfaceAssembly());
    var request = new GetCards();

    var exception = assertThrows(FaultMessage.class, () -> tstObj.getCards(request));

    assertEquals(faultMessage.getClass(), exception.getClass());
    assertEquals(faultMessage.getFaultInfo(), exception.getFaultInfo());
  }

  @SneakyThrows
  @Test
  void getEgkInfoToKvnr() {
    var eventServiceMock = konnektorInterfaceAssembly().eventService();
    var answer =
        new KonnektorInterfaceAnswer<GetCards, GetCardsResponse>()
            .setAnswer(TestDataFactory.getCardsEgkResponse(TestDataFactory.KVNR));

    Mockito.when(eventServiceMock.getCards(Mockito.any())).then(answer);

    var result =
        assertDoesNotThrow(
            () ->
                new EventServiceClient(konnektorContextProvider(), konnektorInterfaceAssembly())
                    .getEgkInfoToKvnr(TestDataFactory.KVNR));

    assertNotNull(result);

    var request = answer.getRequest();
    assertNotNull(request);
    assertEquals(CardTypeType.EGK, request.getCardType());
  }

  @Test
  void getCardHandleTest() {
    var eventServiceMock = konnektorInterfaceAssembly().eventService();
    var getCardsResponse = TestDataFactory.getCardsSmbResponse();
    Mockito.when(eventServiceMock.getCards(Mockito.any())).thenReturn(getCardsResponse);

    var tstObj = new EventServiceClient(konnektorContextProvider(), konnektorInterfaceAssembly());

    var cardHandle = assertDoesNotThrow(() -> tstObj.getCardHandle(CardTypeType.SM_B));

    assertNotNull(cardHandle);
    assertEquals(getCardsResponse.getCards().getCard().get(0).getCardHandle(), cardHandle);
  }

  @Test
  void getCardHandleNoCardTest() {
    var eventServiceMock = konnektorInterfaceAssembly().eventService();
    var getCardsResponse =
        new GetCardsResponse().withStatus(TestDataFactory.getStatusOk()).withCards(new Cards());
    Mockito.when(eventServiceMock.getCards(Mockito.any())).thenReturn(getCardsResponse);

    var tstObj = new EventServiceClient(konnektorContextProvider(), konnektorInterfaceAssembly());

    assertThrows(NoSuchElementException.class, () -> tstObj.getCardHandle(CardTypeType.HB_AX));
  }
}
