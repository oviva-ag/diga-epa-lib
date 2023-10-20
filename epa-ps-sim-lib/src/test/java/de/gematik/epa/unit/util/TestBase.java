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

package de.gematik.epa.unit.util;

import de.gematik.epa.config.DefaultdataProvider;
import de.gematik.epa.konnektor.KonnektorConfigurationProvider;
import de.gematik.epa.konnektor.KonnektorContextProvider;
import de.gematik.epa.konnektor.KonnektorInterfaceAssembly;
import java.util.Optional;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public abstract class TestBase {

  @Getter(lazy = true)
  private final KonnektorConfigurationProvider konnektorConfigurationProvider =
      new KonnektorConfigurationProvider(TestDataFactory.createKonnektorConfiguration());

  @Getter(lazy = true)
  private final KonnektorInterfaceAssembly konnektorInterfaceAssembly =
      TestDataFactory.konnektorInterfaceAssemblyMock();

  @Getter(lazy = true)
  private final DefaultdataProvider defaultdataProvider =
      new DefaultdataProvider().defaultdata(TestDataFactory.defaultdata(true, false));

  private KonnektorContextProvider konnektorContextProvider;

  public KonnektorContextProvider konnektorContextProvider() {
    return Optional.ofNullable(konnektorContextProvider)
        .orElseGet(
            () ->
                konnektorContextProvider =
                    new KonnektorContextProvider(
                        konnektorConfigurationProvider(), konnektorInterfaceAssembly()));
  }
}
