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
import edu.umd.cs.findbugs.annotations.NonNull;
import telematik.ws.conn.cardservicecommon.xsd.v2_0.CardTypeType;
import telematik.ws.conn.eventservice.wsdl.v6_1.EventServicePortType;
import telematik.ws.conn.eventservice.xsd.v6_1.GetCards;
import telematik.ws.conn.eventservice.xsd.v6_1.GetCardsResponse;
import telematik.ws.conn.eventservice.xsd.v6_1.ObjectFactory;

public class EventServiceClient {

  private final EventServicePortType eventService;

  private final KonnektorContext context;

  public EventServiceClient(EventServicePortType eventService, KonnektorContext context) {
    this.eventService = eventService;
    this.context = context;
  }

  public GetCardsResponse getSmbInfo() {
    var request = buildGetCards(true, CardTypeType.SM_B);
    return getCards(request);
  }

  public GetCardsResponse getCards(@NonNull GetCards request) {
    return eventService.getCards(request);
  }

  private GetCards buildGetCards(final boolean mandantWide, final CardTypeType cardType) {
    final var getCardsRequest = new ObjectFactory().createGetCards();
    getCardsRequest.setCardType(cardType);
    getCardsRequest.setContext(context.toContext());
    getCardsRequest.setMandantWide(mandantWide);
    return getCardsRequest;
  }
}
