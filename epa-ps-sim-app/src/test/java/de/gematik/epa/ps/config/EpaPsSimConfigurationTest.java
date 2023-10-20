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

package de.gematik.epa.ps.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class EpaPsSimConfigurationTest {

  @ParameterizedTest
  @MethodSource("beanMethodCalls")
  void konnektorContextProviderTest(ThrowingSupplier<?> beanMethodCall) {
    var result = assertDoesNotThrow(beanMethodCall);

    assertNotNull(result);
  }

  private static List<ThrowingSupplier<?>> beanMethodCalls() {
    var tstObj = new EpaPsSimConfiguration(new DefaultdataConfig(null));

    return List.of(tstObj::defaultdataProvider);
  }
}
