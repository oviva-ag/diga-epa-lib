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

package de.gematik.epa.konnektor.conversion;

import static de.gematik.epa.konnektor.conversion.VSDServiceUtils.NO_UPDATES;
import static de.gematik.epa.konnektor.conversion.VSDServiceUtils.UPDATES_SUCCESSFUL;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.Test;

class VSDServiceUtilsTest {

  @Test
  void decodeDataTest() {
    String validBase64 = "SGVsbG8gV29ybGQ=";
    byte[] expectedBytes = "Hello World".getBytes();
    byte[] actual = VSDServiceUtils.decodeData(validBase64);
    assertArrayEquals(expectedBytes, actual);
  }

  @Test
  void unzipDecodedDataTest() throws IOException {
    String testData = "This is some compressed data.";
    byte[] compressedData = compressData(testData);

    String unzippedData = VSDServiceUtils.unzipDecodedData(compressedData);
    assertEquals(testData, unzippedData);
  }

  private byte[] compressData(String data) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
      gzipOutputStream.write(data.getBytes(StandardCharsets.ISO_8859_1));
    }
    return outputStream.toByteArray();
  }

  @Test
  void getETest() {
    String xmlString =
        """
      <?xml version="1.0" encoding="ISO-8859-1" standalone="yes"?>
      <PN CDM_VERSION="1.0.0" xmlns="http://ws.gematik.de/fa/vsdm/pnw/v1.0">
          <TS>20230920124345</TS>
          <E>3</E>
      </PN>
      """;
    BigInteger actual = VSDServiceUtils.getE(xmlString);
    assertEquals(new BigInteger("3"), actual);
  }

  @Test
  void testResultIsSuccessfulUpdatesSuccessful() {
    boolean actual = VSDServiceUtils.isResultSuccessful(UPDATES_SUCCESSFUL);
    assertTrue(actual);
  }

  @Test
  void testResultIsSuccessfulNoUpdates() {
    boolean actual = VSDServiceUtils.isResultSuccessful(NO_UPDATES);
    assertTrue(actual);
  }

  @Test
  void testResultIsNotSuccessful() {
    int result = 42; // Ein unbekannter Wert
    boolean actual = VSDServiceUtils.isResultSuccessful(result);
    assertFalse(actual);
  }
}
