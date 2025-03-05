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

package com.oviva.epa.client.internal.svc;

import com.oviva.epa.client.internal.svc.model.KonnektorContext;
import com.oviva.epa.client.konn.internal.util.KonnektorUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telematik.ws.conn.cardservice.wsdl.v8_1.CardServicePortType;
import telematik.ws.conn.cardservice.xsd.v8_1.GetPinStatus;
import telematik.ws.conn.cardservice.xsd.v8_1.GetPinStatusResponse;
import telematik.ws.conn.cardservice.xsd.v8_1.ObjectFactory;

public class CardServiceClient {

  private static final Logger log = LoggerFactory.getLogger(CardServiceClient.class);
  private final CardServicePortType cardService;
  private final KonnektorContext konnektorContext;

  public CardServiceClient(CardServicePortType cardService, KonnektorContext konnektorContext) {
    this.cardService = cardService;
    this.konnektorContext = konnektorContext;
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

  private GetPinStatus buildGetPinStatus(String cardHandle, String pinType) {
    GetPinStatus getPinStatusRequest = new ObjectFactory().createGetPinStatus();
    getPinStatusRequest.setContext(konnektorContext.toContext());
    getPinStatusRequest.setCardHandle(cardHandle);
    getPinStatusRequest.setPinTyp(pinType);
    return getPinStatusRequest;
  }
}
