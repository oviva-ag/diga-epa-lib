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

package de.gematik.epa.utils;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.epa.unit.util.ResourceLoader;
import de.gematik.epa.unit.util.TestDataFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class CertificateUtilsTest {

  @Test
  @SneakyThrows
  void toX509CertificateTest() {
    var certAsByteArray = ResourceLoader.autCertificateAsByteArray();

    var certificate = assertDoesNotThrow(() -> CertificateUtils.toX509Certificate(certAsByteArray));

    assertNotNull(certificate);
    assertNotNull(certificate.getSerialNumber());
  }

  @SneakyThrows
  @Test
  void getTelematikIdFromCertificateTest() {

    var certAsByteArray = ResourceLoader.autCertificateAsByteArray();

    var certificate = CertificateUtils.toX509Certificate(certAsByteArray);

    var telematikId =
        assertDoesNotThrow(() -> CertificateUtils.getTelematikIdFromCertificate(certificate));

    assertNotNull(telematikId);
    assertEquals(TestDataFactory.SMB_AUT_TELEMATIK_ID, telematikId);
  }

  @SneakyThrows
  @Test
  void getTelematikIdFromCertificateNotPresentTest() {
    var certAsByteArray = ResourceLoader.readBytesFromResource(ResourceLoader.OTHER_CERTIFICATE);

    var certificate = CertificateUtils.toX509Certificate(certAsByteArray);

    var telematikId =
        assertDoesNotThrow(() -> CertificateUtils.getTelematikIdFromCertificate(certificate));

    assertNull(telematikId);
  }
}
