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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import de.gematik.epa.konnektor.KonnektorUtils;
import de.gematik.epa.unit.util.KonnektorInterfaceAnswer;
import de.gematik.epa.unit.util.TestBase;
import de.gematik.epa.unit.util.TestDataFactory;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import telematik.ws.conn.cardservice.xsd.v8_1.GetPinStatus;
import telematik.ws.conn.cardservice.xsd.v8_1.GetPinStatusResponse;
import telematik.ws.conn.cardservice.xsd.v8_1.PinStatusEnum;
import telematik.ws.conn.cardservicecommon.xsd.v2_0.CardTypeType;
import telematik.ws.conn.cardservicecommon.xsd.v2_0.PinResultEnum;

class CardServiceClientTest extends TestBase {

  private CardServiceClient testObj;

  static Stream<PinStatusEnum> pinStatus() {
    return Stream.of(PinStatusEnum.VERIFIED, PinStatusEnum.VERIFIABLE, PinStatusEnum.DISABLED);
  }

  static Stream<PinStatusEnum> pinStatusFailure() {
    return Stream.of(PinStatusEnum.EMPTY_PIN, PinStatusEnum.TRANSPORT_PIN, PinStatusEnum.BLOCKED);
  }

  static Stream<PinResultEnum> pinResultEnum() {
    return Stream.of(PinResultEnum.REJECTED, PinResultEnum.WASBLOCKED, PinResultEnum.NOWBLOCKED);
  }

  @BeforeEach
  void beforeEach() {
    TestDataFactory.initKonnektorTestConfiguration(konnektorInterfaceAssembly());
    testObj = new CardServiceClient(konnektorContextProvider(), konnektorInterfaceAssembly());
  }

  @Test
  void getPinStatusResponseTest() {
    var eventServiceMock = konnektorInterfaceAssembly().eventService();
    var cardServiceMock = konnektorInterfaceAssembly().cardService();

    var getCardsResponse = TestDataFactory.getCardsSmbResponse();
    var cardHandle = TestDataFactory.cardInfoSmb().getCardHandle();

    var getPinStatusResponse =
        new KonnektorInterfaceAnswer<GetPinStatus, GetPinStatusResponse>()
            .setAnswer(TestDataFactory.getPinStatusResponse(PinStatusEnum.VERIFIED));

    Mockito.when(eventServiceMock.getCards(any())).thenReturn(getCardsResponse);
    Mockito.when(cardServiceMock.getPinStatus(any())).then(getPinStatusResponse);

    var response =
        assertDoesNotThrow(
            () -> testObj.getPinStatusResponse(cardHandle, CardServiceClient.PIN_SMC));
    assertNotNull(response);
    assertEquals(KonnektorUtils.STATUS_OK, response.getStatus().getResult());
    assertEquals(PinStatusEnum.VERIFIED, response.getPinStatus());

    var request = getPinStatusResponse.getRequest();
    assertNotNull(request);
    assertEquals(CardServiceClient.PIN_SMC, request.getPinTyp());
  }

  @ParameterizedTest
  @MethodSource("pinStatus")
  void verifyPinSuccessTest(PinStatusEnum pinStatusEnum) {
    var eventServiceMock = konnektorInterfaceAssembly().eventService();
    var cardServiceMock = konnektorInterfaceAssembly().cardService();

    var getCardsResponse = TestDataFactory.getCardsSmbResponse();
    var getPinStatusResponse = TestDataFactory.getPinStatusResponse(pinStatusEnum);
    var verifyPinResponse =
        TestDataFactory.verifyPin(TestDataFactory.getStatusOk(), PinResultEnum.OK);

    Mockito.when(eventServiceMock.getCards(any())).thenReturn(getCardsResponse);
    Mockito.when(cardServiceMock.getPinStatus(any())).thenReturn(getPinStatusResponse);
    Mockito.when(cardServiceMock.verifyPin(any())).thenReturn(verifyPinResponse);

    assertDoesNotThrow(() -> testObj.verifyPin(CardTypeType.SM_B, CardServiceClient.PIN_SMC));
    verify(eventServiceMock).getCards(any());
    verify(cardServiceMock).getPinStatus(any());
  }

  @ParameterizedTest
  @MethodSource("pinStatusFailure")
  void verifyPinThrowsTest(PinStatusEnum pinStatusEnum) {
    var eventServiceMock = konnektorInterfaceAssembly().eventService();
    var cardServiceMock = konnektorInterfaceAssembly().cardService();
    Mockito.when(eventServiceMock.getCards(any()))
        .thenReturn(TestDataFactory.getCardsSmbResponse());
    Mockito.when(cardServiceMock.getPinStatus(any()))
        .thenReturn(TestDataFactory.getPinStatusResponse(pinStatusEnum));
    assertThrows(
        IllegalStateException.class,
        () -> testObj.verifyPin(CardTypeType.SM_B, CardServiceClient.PIN_SMC));
  }

  @Test
  void verifyPinReturnsPinResultOk() {
    var eventServiceMock = konnektorInterfaceAssembly().eventService();
    var cardServiceMock = konnektorInterfaceAssembly().cardService();
    Mockito.when(eventServiceMock.getCards(any()))
        .thenReturn(TestDataFactory.getCardsSmbResponse());
    Mockito.when(cardServiceMock.getPinStatus(any()))
        .thenReturn(TestDataFactory.getPinStatusResponse(PinStatusEnum.VERIFIABLE));
    var verifyPinResponse =
        TestDataFactory.verifyPin(TestDataFactory.getStatusOk(), PinResultEnum.OK);
    Mockito.when(cardServiceMock.verifyPin(any())).thenReturn(verifyPinResponse);
    assertDoesNotThrow(() -> testObj.verifyPin(CardTypeType.SM_B, CardServiceClient.PIN_SMC));
    verify(eventServiceMock).getCards(any());
    verify(cardServiceMock).getPinStatus(any());
  }

  @Test
  void verifyPinReturnsPinResultErrorAndWarning() {
    var eventServiceMock = konnektorInterfaceAssembly().eventService();
    var cardServiceMock = konnektorInterfaceAssembly().cardService();
    Mockito.when(eventServiceMock.getCards(any()))
        .thenReturn(TestDataFactory.getCardsSmbResponse());
    Mockito.when(cardServiceMock.getPinStatus(any()))
        .thenReturn(TestDataFactory.getPinStatusResponse(PinStatusEnum.VERIFIABLE));
    var verifyPinResponse =
        TestDataFactory.verifyPin(TestDataFactory.getStatusWarning(), PinResultEnum.ERROR);
    Mockito.when(cardServiceMock.verifyPin(any())).thenReturn(verifyPinResponse);
    assertThrows(
        IllegalStateException.class,
        () -> testObj.verifyPin(CardTypeType.SM_B, CardServiceClient.PIN_SMC));
    verify(eventServiceMock).getCards(any());
    verify(cardServiceMock).getPinStatus(any());
  }

  @ParameterizedTest
  @MethodSource("pinResultEnum")
  void verifyPinReturnsPinResultWithStatusOkThrowsTest(PinResultEnum pinResult) {
    var eventServiceMock = konnektorInterfaceAssembly().eventService();
    var cardServiceMock = konnektorInterfaceAssembly().cardService();
    Mockito.when(eventServiceMock.getCards(any()))
        .thenReturn(TestDataFactory.getCardsSmbResponse());
    Mockito.when(cardServiceMock.getPinStatus(any()))
        .thenReturn(TestDataFactory.getPinStatusResponse(PinStatusEnum.VERIFIABLE));
    var verifyPinResponse = TestDataFactory.verifyPin(TestDataFactory.getStatusOk(), pinResult);
    Mockito.when(cardServiceMock.verifyPin(any())).thenReturn(verifyPinResponse);
    assertThrows(
        IllegalStateException.class,
        () -> testObj.verifyPin(CardTypeType.SM_B, CardServiceClient.PIN_SMC));
    verify(eventServiceMock).getCards(any());
    verify(cardServiceMock).getPinStatus(any());
  }

  @Test
  void verifySmbTest() {
    var eventServiceMock = konnektorInterfaceAssembly().eventService();
    var cardServiceMock = konnektorInterfaceAssembly().cardService();
    Mockito.when(eventServiceMock.getCards(any()))
        .thenReturn(TestDataFactory.getCardsSmbResponse());
    Mockito.when(cardServiceMock.getPinStatus(any()))
        .thenReturn(TestDataFactory.getPinStatusResponse(PinStatusEnum.VERIFIABLE));
    var verifyPinResponse =
        TestDataFactory.verifyPin(TestDataFactory.getStatusOk(), PinResultEnum.OK);
    Mockito.when(cardServiceMock.verifyPin(any())).thenReturn(verifyPinResponse);
    assertDoesNotThrow(() -> testObj.verifySmb());
    verify(eventServiceMock).getCards(any());
    verify(cardServiceMock).getPinStatus(any());
    verify(cardServiceMock).verifyPin(any());
  }
}
