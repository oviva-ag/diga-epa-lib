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
import lombok.Getter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class KonnektorInterfaceProviderTest {

  @BeforeAll
  static void setup() {
    TestDataFactory.initKonnektorTestConfiguration();
  }

  @Test
  void getRetrieveKonnektorInterfaceAssemblyTest() {
    KonnektorInterfaceProvider.defaultInstance().setKonnektorInterfaceProviderCallback(null);
    var firstKonnektorInterfaceAssembly =
        assertDoesNotThrow(
            () -> KonnektorInterfaceProvider.defaultInstance().getKonnektorInterfaceAssembly());
    var secondKonnektorInteraceAssembly =
        assertDoesNotThrow(
            () ->
                KonnektorInterfaceProvider.defaultInstance().retrieveKonnektorInterfaceAssembly());

    assertEquals(firstKonnektorInterfaceAssembly, secondKonnektorInteraceAssembly);
  }

  @Test
  void retrieveGetKonnektorInterfaceAssemblyTest() {
    var callback =
        new KonnektorInterfaceProvider.KonnektorInterfaceProviderCallback() {

          @Getter private boolean executed = false;

          @Override
          public KonnektorInterfaceAssembly provideInterfaceAssembly() {
            executed = true;
            return TestDataFactory.konnektorInterfaceAssemblyMock();
          }
        };

    assertDoesNotThrow(
        () ->
            KonnektorInterfaceProvider.defaultInstance()
                .setKonnektorInterfaceProviderCallback(callback));

    var firstKonnektorInterfaceAssembly =
        assertDoesNotThrow(
            () ->
                KonnektorInterfaceProvider.defaultInstance().retrieveKonnektorInterfaceAssembly());
    var secondKonnektorInteraceAssembly =
        assertDoesNotThrow(
            () -> KonnektorInterfaceProvider.defaultInstance().getKonnektorInterfaceAssembly());

    assertEquals(firstKonnektorInterfaceAssembly, secondKonnektorInteraceAssembly);
    assertTrue(callback.isExecuted());
  }
}
