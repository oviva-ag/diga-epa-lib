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

import de.gematik.epa.config.KonnektorConnectionConfigurationDTO;
import de.gematik.epa.dto.request.KonnektorConfigurationRequestDTO;
import de.gematik.epa.unit.util.TestDataFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class KonnektorConfigurationProviderTest {

  @Test
  void updateKonnektorConfigurations() {
    var tstData = TestDataFactory.createKonnektorConfiguration();
    var tstDto =
        new KonnektorConfigurationRequestDTO(
            new KonnektorConnectionConfigurationDTO(
                tstData.connection().address(),
                tstData.connection().tlsConfig(),
                tstData.connection().proxyAddress(),
                tstData.connection().basicAuthentication(),
                Boolean.TRUE),
            tstData.context(),
            Boolean.TRUE);

    tstData.context(null).connection().address(null);

    var tstObj = new KonnektorConfigurationProvider(tstData);

    var tstData2 = assertDoesNotThrow(() -> tstObj.updateKonnektorConfigurations(tstDto));

    assertNotNull(tstData2);
    assertNotNull(tstData2.context());
    assertNotNull(tstData2.connection());
    assertNotNull(tstData2.connection().address());
  }

  @Test
  void updateKonnektorConnectionKonfiguration() {
    var tstData = TestDataFactory.createKonnektorConfiguration();

    var tstDto =
        new KonnektorConnectionConfigurationDTO(
            tstData.connection().address(), null, null, null, Boolean.FALSE);

    tstData.connection().address(null);

    var tstObj = new KonnektorConfigurationProvider(tstData);

    var tstData2 = assertDoesNotThrow(() -> tstObj.updateKonnektorConnectionKonfiguration(tstDto));

    assertNotNull(tstData2);
    assertNotNull(tstData2.address());
    assertNull(tstData2.proxyAddress());
    assertNull(tstData2.basicAuthentication());
    assertNull(tstData2.tlsConfig());
  }
}
