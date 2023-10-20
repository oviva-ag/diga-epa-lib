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

package de.gematik.epa.konnektor.config;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.epa.unit.util.TestDataFactory;
import org.junit.jupiter.api.Test;

class KonnektorConnectionConfigurationMutableTest {

  @Test
  void replace() {
    var tstData = TestDataFactory.createKonnektorConnectionConfiguration();
    var tstObj =
        TestDataFactory.createKonnektorConnectionConfiguration()
            .address(null)
            .proxyAddress(null)
            .basicAuthentication(null)
            .tlsConfig(null);

    var result = assertDoesNotThrow(() -> tstObj.replace(tstData));

    assertNotNull(result);
    assertEquals(tstData, result);
  }

  @Test
  void update() {
    var tstData = TestDataFactory.createKonnektorConnectionConfiguration().address(null);
    var tstObj =
        TestDataFactory.createKonnektorConnectionConfiguration()
            .basicAuthentication(null)
            .tlsConfig(null);

    var result = assertDoesNotThrow(() -> tstObj.update(tstData));

    assertNotNull(result);
    assertNotEquals(tstData, result);

    assertEquals(tstObj.address(), result.address());
    assertEquals(tstData.basicAuthentication(), result.basicAuthentication());
    assertEquals(tstData.tlsConfig(), result.tlsConfig());
  }
}
