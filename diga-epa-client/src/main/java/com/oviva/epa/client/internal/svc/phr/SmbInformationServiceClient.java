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

package com.oviva.epa.client.internal.svc.phr;

import com.oviva.epa.client.internal.svc.CertificateServiceClient;
import com.oviva.epa.client.internal.svc.EventServiceClient;
import com.oviva.epa.client.internal.svc.phr.model.SmbInformation;
import de.gematik.epa.ihe.model.simple.AuthorInstitution;
import java.util.List;
import telematik.ws.conn.cardservice.xsd.v8_1.CardInfoType;

public class SmbInformationServiceClient {

  private final EventServiceClient eventServiceClient;

  private final CertificateServiceClient certificateServiceClient;

  public SmbInformationServiceClient(
      EventServiceClient eventServiceClient, CertificateServiceClient certificateServiceClient) {
    this.eventServiceClient = eventServiceClient;
    this.certificateServiceClient = certificateServiceClient;
  }

  private static AuthorInstitution smbInformationToAuthorInstitution(SmbInformation smbInfo) {
    return new AuthorInstitution(smbInfo.cardHolderName(), smbInfo.telematikId());
  }

  public List<SmbInformation> getSmbInformations() {
    var insertedSmbs = eventServiceClient.getSmbInfo().getCards().getCard();

    return insertedSmbs.stream().map(this::retrieveSmbInformation).toList();
  }

  public List<AuthorInstitution> getAuthorInstitutions() {
    return getSmbInformations().stream()
        .map(SmbInformationServiceClient::smbInformationToAuthorInstitution)
        .toList();
  }

  private SmbInformation retrieveSmbInformation(CardInfoType cardInfo) {
    var telematikId = certificateServiceClient.getTelematikIdToCard(cardInfo);

    return new SmbInformation(telematikId, cardInfo.getIccsn(), cardInfo.getCardHolderName());
  }
}
