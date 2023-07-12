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

package de.gematik.epa.konnektor;

import de.gematik.epa.data.SmbInformation;
import de.gematik.epa.ihe.model.simple.AuthorInstitution;
import de.gematik.epa.konnektor.client.CertificateServiceClient;
import de.gematik.epa.konnektor.client.EventServiceClient;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import telematik.ws.conn.cardservice.xsd.v8_1.CardInfoType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
public class SmbInformationProvider {

  @Getter(lazy = true)
  private static final SmbInformationProvider defaultInstance = new SmbInformationProvider();

  @Getter(lazy = true)
  private final EventServiceClient eventServiceClient = new EventServiceClient();

  @Getter(lazy = true)
  private final CertificateServiceClient certificateServiceClient = new CertificateServiceClient();

  private final Map<String, SmbInformation> knownSmb = new ConcurrentHashMap<>();

  public List<SmbInformation> getSmbInformations() {
    var insertedSmbs = eventServiceClient().getSmbInfo().getCards().getCard();
    var unknownSmbs =
        insertedSmbs.stream()
            .filter(cardInfo -> !knownSmb.containsKey(cardInfo.getIccsn()))
            .toList();

    unknownSmbs.stream()
        .map(this::retrieveSmbInformation)
        .forEach(smbInfo -> knownSmb.put(smbInfo.iccsn(), smbInfo));

    return insertedSmbs.stream().map(cardInfo -> knownSmb.get(cardInfo.getIccsn())).toList();
  }

  public List<AuthorInstitution> getAuthorInstitutions() {
    return getSmbInformations().stream()
        .map(SmbInformationProvider::smbInformationToAuthorInstitution)
        .toList();
  }

  public AuthorInstitution getOneAuthorInstitution() {
    return getAuthorInstitutions().stream().findFirst().orElse(null);
  }

  private SmbInformation retrieveSmbInformation(CardInfoType cardInfo) {
    var telematikId = certificateServiceClient().getTelematikIdToCard(cardInfo);

    return new SmbInformation(telematikId, cardInfo.getIccsn(), cardInfo.getCardHolderName());
  }

  private static AuthorInstitution smbInformationToAuthorInstitution(SmbInformation smbInfo) {
    return new AuthorInstitution(smbInfo.cardHolderName(), smbInfo.telematikId());
  }
}
