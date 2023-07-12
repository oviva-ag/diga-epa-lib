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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.epa.unit.util.TestDataFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SmbInformationProviderTest {

  @BeforeEach
  void beforeEach() {
    TestDataFactory.initKonnektorTestConfiguration();
  }

  @SneakyThrows
  @Test
  void getSmbInformationsTest() {
    TestDataFactory.setupMocksForSmbInformationProvider();
    var getCardsSmbResponse = TestDataFactory.getCardsSmbResponse();

    var smbInformations =
        assertDoesNotThrow(() -> SmbInformationProvider.defaultInstance().getSmbInformations());

    assertNotNull(smbInformations);
    assertEquals(getCardsSmbResponse.getCards().getCard().size(), smbInformations.size());
  }

  @Test
  void getAuthorInstitutionsTest() {
    TestDataFactory.setupMocksForSmbInformationProvider();

    var authorInformations =
        assertDoesNotThrow(() -> SmbInformationProvider.defaultInstance().getAuthorInstitutions());

    assertNotNull(authorInformations);
    assertFalse(authorInformations.isEmpty());
  }

  @Test
  void getOneAuthorInstitutionTest() {
    TestDataFactory.setupMocksForSmbInformationProvider();

    var authorInformation =
        assertDoesNotThrow(
            () -> SmbInformationProvider.defaultInstance().getOneAuthorInstitution());

    assertEquals(TestDataFactory.cardInfoSmb().getCardHolderName(), authorInformation.name());
    assertEquals(TestDataFactory.SMB_AUT_TELEMATIK_ID, authorInformation.identifier());
  }
}
