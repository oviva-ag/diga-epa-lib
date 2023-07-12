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

package de.gematik.epa.context;

import static de.gematik.epa.unit.util.Assertions.*;
import static de.gematik.epa.unit.util.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ContextHeaderBuilderTest {

  @Test
  void buildContextHeader() {
    var builder =
        new ContextHeaderBuilder()
            .mandantId(MANDANT_ID)
            .clientSystemId(CLIENTSYSTEM_ID)
            .workplaceId(WORKPLACE_ID)
            .userId(USER_ID)
            .homeCommunity(HOME_COMMUNITY_ID)
            .extension(KVNR)
            .root(ROOT);

    var expectedResult = contextHeader();
    var actualResult = builder.buildContextHeader();

    assertNotNull(actualResult);
    assertEquals(expectedResult, actualResult);
  }
}
