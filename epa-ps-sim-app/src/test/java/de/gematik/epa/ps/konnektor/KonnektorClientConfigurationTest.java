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

package de.gematik.epa.ps.konnektor;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.epa.unit.AppTestDataFactory;
import de.gematik.epa.unit.TestKonnektorClientConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

class KonnektorClientConfigurationTest {

  @Test
  void constructorTest() {
    var result =
        assertDoesNotThrow(
            () ->
                new KonnektorClientConfiguration(
                    AppTestDataFactory.createKonnektorConfiguration(),
                    new DefaultResourceLoader()));

    assertNotNull(result.konnektorConfiguration());
    assertNotNull(result.resourceLoader());
  }

  @Test
  void konnektorConfigurationProviderTest() {
    var tstObj =
        new KonnektorClientConfiguration(
            AppTestDataFactory.createKonnektorConfiguration(), new DefaultResourceLoader());

    var konCfgProvider1 = assertDoesNotThrow(tstObj::konnektorConfigurationProvider);

    assertNotNull(konCfgProvider1);

    var konCfgProvider2 = assertDoesNotThrow(tstObj::konnektorConfigurationProvider);

    assertSame(konCfgProvider1, konCfgProvider2);
  }

  @Test
  void konnektorInterfaceAssemblyTest() {
    var tstObj =
        new TestKonnektorClientConfiguration(
            AppTestDataFactory.createKonnektorConfiguration(), new DefaultResourceLoader());

    var konIntAssembly1 = assertDoesNotThrow(tstObj::konnektorInterfaceAssembly);

    assertNotNull(konIntAssembly1);

    var konIntAssembly2 = assertDoesNotThrow(tstObj::konnektorInterfaceAssembly);

    assertSame(konIntAssembly1, konIntAssembly2);
  }

  @Test
  void konnektorContextProviderTest() {
    var tstObj =
        new TestKonnektorClientConfiguration(
            AppTestDataFactory.createKonnektorConfiguration(), new DefaultResourceLoader());

    var konCtxProvider1 = assertDoesNotThrow(tstObj::konnektorContextProvider);

    assertNotNull(konCtxProvider1);

    var konCtxProvider2 = assertDoesNotThrow(tstObj::konnektorContextProvider);

    assertSame(konCtxProvider1, konCtxProvider2);
  }
}
