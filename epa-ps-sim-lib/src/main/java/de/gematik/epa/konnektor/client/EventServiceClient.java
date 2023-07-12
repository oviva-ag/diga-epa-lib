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
import de.gematik.epa.konnektor.KonnektorInterfaceProvider;
import de.gematik.epa.konnektor.KonnektorUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import telematik.ws.conn.cardservice.xsd.v8_1.CardInfoType;
import telematik.ws.conn.cardservicecommon.xsd.v2_0.CardTypeType;
import telematik.ws.conn.eventservice.wsdl.v6_1.EventServicePortType;
import telematik.ws.conn.eventservice.xsd.v6_1.GetCards;
import telematik.ws.conn.eventservice.xsd.v6_1.GetCardsResponse;
import telematik.ws.conn.eventservice.xsd.v6_1.ObjectFactory;

@Accessors(fluent = true)
@Slf4j
public class EventServiceClient {

  @Getter(lazy = true, value = AccessLevel.MODULE)
  private final EventServicePortType eventService =
      KonnektorInterfaceProvider.defaultInstance().getKonnektorInterfaceAssembly().eventService();

  @Getter(lazy = true, value = AccessLevel.MODULE)
  private final KonnektorContextProvider contextProvider =
      KonnektorContextProvider.defaultInstance();

  public GetCardsResponse getSmbInfo() {
    var request = buildGetCards(true, CardTypeType.SM_B);
    return getCards(request);
  }

  public CardInfoType getEgkInfoToKvnr(@NonNull String kvnr) {
    var request = buildGetCards(true, CardTypeType.EGK);
    var response = getCards(request);

    KonnektorUtils.logWarningIfPresent(
        log, response.getStatus(), KonnektorUtils.warnMsgWithOperationName("getCards"));

    return response.getCards().getCard().stream()
        .filter(ci -> kvnr.equals(ci.getKvnr()))
        .findFirst()
        .orElse(null);
  }

  public GetCardsResponse getCards(@NonNull GetCards request) {
    return eventService().getCards(request);
  }

  // region private

  private GetCards buildGetCards(final boolean mandantWide, final CardTypeType cardType) {
    final var getCardsRequest = new ObjectFactory().createGetCards();
    getCardsRequest.setCardType(cardType);
    getCardsRequest.setContext(contextProvider().getContext());
    getCardsRequest.setMandantWide(mandantWide);
    return getCardsRequest;
  }

  // endregion private
}
