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

import de.gematik.epa.konnektor.KonnektorContextProvider;
import de.gematik.epa.konnektor.KonnektorInterfaceAssembly;
import de.gematik.epa.konnektor.KonnektorUtils;
import java.math.BigInteger;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import telematik.ws.conn.cardservice.wsdl.v8_1.CardServicePortType;
import telematik.ws.conn.cardservice.xsd.v8_1.GetPinStatus;
import telematik.ws.conn.cardservice.xsd.v8_1.GetPinStatusResponse;
import telematik.ws.conn.cardservice.xsd.v8_1.ObjectFactory;
import telematik.ws.conn.cardservice.xsd.v8_1.PinStatusEnum;
import telematik.ws.conn.cardservice.xsd.v8_1.VerifyPin;
import telematik.ws.conn.cardservicecommon.xsd.v2_0.CardTypeType;
import telematik.ws.conn.cardservicecommon.xsd.v2_0.PinResponseType;
import telematik.ws.conn.cardservicecommon.xsd.v2_0.PinResultEnum;
import telematik.ws.conn.connectorcontext.xsd.v2_0.ContextType;

@Accessors(fluent = true)
@Slf4j
public class CardServiceClient extends KonnektorServiceClient {
  private CardServicePortType cardService;
  private ContextType contextType;
  private EventServiceClient eventServiceClient;

  public static final String PIN_SMC = "PIN.SMC";
  private static final String PIN_VERIFICATION_MESSAGE =
      "Please verify PIN manually via Konnektor management interface.";
  private static final String PIN_STATUS = "PIN status is ";

  public CardServiceClient(
      KonnektorContextProvider konnektorContextProvider,
      KonnektorInterfaceAssembly konnektorInterfaceAssembly) {
    super(konnektorContextProvider, konnektorInterfaceAssembly);
    runInitializationSynchronized();
  }

  @Override
  protected void initialize() {
    contextType = konnektorContextProvider.getContext();
    cardService = konnektorInterfaceAssembly.cardService();
    eventServiceClient =
        new EventServiceClient(konnektorContextProvider, konnektorInterfaceAssembly);
  }

  public GetPinStatusResponse getPinStatusResponse(String cardHandle, String pinType) {
    GetPinStatus request = buildGetPinStatus(cardHandle, pinType);
    GetPinStatusResponse response = getPinStatus(request);
    KonnektorUtils.logWarningIfPresent(
        log, response.getStatus(), KonnektorUtils.warnMsgWithOperationName("getPinStatus"));
    return response;
  }

  // region private
  private GetPinStatusResponse getPinStatus(@NonNull GetPinStatus request) {
    return cardService.getPinStatus(request);
  }

  private PinResponseType getPinResult(@NonNull VerifyPin request) {
    return cardService.verifyPin(request);
  }

  private GetPinStatus buildGetPinStatus(String cardHandle, String pinType) {
    GetPinStatus getPinStatusRequest = new ObjectFactory().createGetPinStatus();
    getPinStatusRequest.setContext(contextType);
    getPinStatusRequest.setCardHandle(cardHandle);
    getPinStatusRequest.setPinTyp(pinType);
    return getPinStatusRequest;
  }

  private VerifyPin buildVerifyPin(String cardHandle, String pinType) {
    VerifyPin verifyPinRequest = new ObjectFactory().createVerifyPin();
    verifyPinRequest.setContext(contextType);
    verifyPinRequest.setCardHandle(cardHandle);
    verifyPinRequest.setPinTyp(pinType);
    return verifyPinRequest;
  }

  private void runVerifyPin(String cardHandle, String pinType) {
    VerifyPin request = buildVerifyPin(cardHandle, pinType);
    PinResponseType response = getPinResult(request);
    PinResultEnum pinResult = response.getPinResult();
    String status = response.getStatus().getResult();
    BigInteger leftTries = response.getLeftTries();

    KonnektorUtils.logWarningIfPresent(
        log, response.getStatus(), KonnektorUtils.warnMsgWithOperationName("verifyPin"));

    String errorMessage = "";

    if (pinResult.equals(PinResultEnum.OK)) {
      log.info("PIN was successfully verified, PinResult: {}, Status: {}", pinResult, status);
      return;
    } else if (pinResult.equals(PinResultEnum.ERROR)) {
      errorMessage = "a processing error has occurred";
    } else if (pinResult.equals(PinResultEnum.REJECTED)) {
      errorMessage = "PIN was incorrect. The number of remaining attempts: " + leftTries;
    } else if (pinResult.equals(PinResultEnum.NOWBLOCKED)
        || pinResult.equals(PinResultEnum.WASBLOCKED)) {
      errorMessage = "PIN was entered incorrectly three times and is now blocked";
    } else {
      errorMessage = "unknown error";
    }

    logAndThrowError(errorMessage, pinResult, status);
  }

  private void logAndThrowError(String errorMessage, PinResultEnum pinResult, String status) {
    String logMessage =
        "VerifyPin was not successful, "
            + errorMessage
            + ", PinResult: {}, Status: {}. "
            + PIN_VERIFICATION_MESSAGE;
    log.error(logMessage, pinResult, status);
    throw new IllegalStateException(String.format(logMessage, pinResult, status));
  }

  // endregion private

  public void verifyPin(CardTypeType cardTypeType, String pinType) {
    String cardHandle = eventServiceClient.getCardHandle(cardTypeType);
    GetPinStatusResponse getResponse = getPinStatusResponse(cardHandle, pinType);
    PinStatusEnum pinStatus = getResponse.getPinStatus();
    BigInteger leftTries = getResponse.getLeftTries();

    switch (pinStatus) {
      case VERIFIED -> log.info("PIN is already {}.", pinStatus);
      case DISABLED -> log.info(
          "PIN is {}. PIN protection switched off (verification not required).", pinStatus);
      case TRANSPORT_PIN -> {
        var errMsg =
            String.format(
                "PIN is %s and needs to be changed manually via Konnektor management interface. You have %s attempts for PIN entry.",
                pinStatus, leftTries);
        log.error(errMsg);
        throw new IllegalStateException(errMsg);
      }
      case BLOCKED -> {
        var errMsg =
            String.format(
                "PIN is %s and needs be unblocked with PUK manually via Konnektor management interface. You have %s attempts for PUK entry.",
                pinStatus, leftTries);
        log.error(errMsg);
        throw new IllegalStateException(errMsg);
      }
      case EMPTY_PIN -> {
        log.error("PIN is empty. Please set the PIN manually via Konnektor management interface.");
        throw new IllegalStateException(
            PIN_STATUS
                + pinStatus
                + "Please set the PIN manually via Konnektor management interface.");
      }
      case VERIFIABLE -> {
        log.info("PIN is {} and verification process starts...", pinStatus);
        runVerifyPin(cardHandle, pinType);
      }
      default -> {
        var errMsg =
            String.format(
                "Unknown PIN status: %s. Please check the PIN status manually via Konnektor management interface.",
                pinStatus);
        log.error(errMsg);
        throw new IllegalStateException(errMsg);
      }
    }
  }

  public void verifySmb() {
    verifyPin(CardTypeType.SM_B, PIN_SMC);
  }
}
