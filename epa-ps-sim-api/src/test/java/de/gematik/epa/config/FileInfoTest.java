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

package de.gematik.epa.config;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.gematik.epa.ihe.model.simple.ByteArray;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class FileInfoTest {

  private static final String PATH = "/path/to/file.txt";
  private static final ByteArray CONTENT = ByteArray.of(PATH.getBytes(StandardCharsets.UTF_8));

  @Test
  void getFilePathTest() {
    var tstObj = assertDoesNotThrow(() -> new FileInfo(PATH));

    assertTrue(tstObj.isFilePath());
    assertFalse(tstObj.isFileContent());

    assertEquals(PATH, tstObj.getFilePath());
  }

  @Test
  void getFileContentTest() {
    var tstObj = assertDoesNotThrow(() -> new FileInfo(CONTENT));

    assertTrue(tstObj.isFileContent());
    assertFalse(tstObj.isFilePath());

    assertEquals(CONTENT, tstObj.getFileContent());
  }

  @Test
  void testFilePathToStringTest() {
    var tstObj = new FileInfo(PATH);

    var toString = assertDoesNotThrow(tstObj::toString);

    assertNotNull(toString);
    assertTrue(toString.contains(PATH));
  }

  @Test
  void testFileContentToStringTest() {
    var tstObj = new FileInfo(CONTENT);

    var toString = assertDoesNotThrow(tstObj::toString);

    assertNotNull(toString);
    assertTrue(toString.contains(CONTENT.toString()));
  }

  @Test
  void testFilePathHashCodeTest() {
    var tstObj = new FileInfo(PATH);

    var hashCode = assertDoesNotThrow(tstObj::hashCode);

    assertNotNull(hashCode);
  }

  @Test
  void testFileContentHashCodeTest() {
    var tstObj = new FileInfo(CONTENT);

    var hashCode = assertDoesNotThrow(tstObj::hashCode);

    assertNotNull(hashCode);
  }

  @Test
  void testEqualsTest() {
    var tstObj1 = new FileInfo(PATH);
    var tstObj2 = new FileInfo(PATH);

    var equals = assertDoesNotThrow(() -> tstObj1.equals(tstObj2));

    assertTrue(equals);

    tstObj1.setFileContent(CONTENT);

    assertNotEquals(tstObj1, tstObj2);

    tstObj2.setFileContent(CONTENT);

    assertEquals(tstObj2, tstObj1);

    tstObj1.setFilePath(null);
    tstObj2.setFilePath(null);

    assertEquals(tstObj1, tstObj2);

    var cutPath = PATH.concat("0");

    tstObj2.setFileContent(ByteArray.of(cutPath.getBytes(StandardCharsets.UTF_8)));

    assertNotEquals(tstObj2, tstObj1);

    tstObj1.setFileContent(null);
    tstObj2.setFileContent(null);
    tstObj1.setFilePath(cutPath);
    tstObj2.setFilePath(PATH);

    assertNotEquals(tstObj1, tstObj2);
  }

  @Test
  void fileInfoPathTest() {
    var tstObj = new FileInfo("/path/to/keystore/file.p12");
    var tlsCfg = new TlsConfig(tstObj, "00", "PKCS12", List.of());
    var objMapper = getObjectMapper();

    var serialized = assertDoesNotThrow(() -> objMapper.writeValueAsString(tlsCfg));

    assertNotNull(serialized);

    var deserialized = assertDoesNotThrow(() -> objMapper.readValue(serialized, TlsConfig.class));

    assertNotNull(deserialized);

    assertEquals(tstObj, deserialized.keystorefile());
  }

  @Test
  void fileInfoContentTest() {
    var tstObj =
        new FileInfo(ByteArray.of("/path/to/keystore/file.p12".getBytes(StandardCharsets.UTF_8)));
    var tlsCfg = new TlsConfig(tstObj, "00", "PKCS12", List.of());
    var objMapper = getObjectMapper();

    var serialized = assertDoesNotThrow(() -> objMapper.writeValueAsString(tlsCfg));

    assertNotNull(serialized);

    var deserialized = assertDoesNotThrow(() -> objMapper.readValue(serialized, TlsConfig.class));

    assertNotNull(deserialized);

    assertEquals(tstObj, deserialized.keystorefile());
  }

  private ObjectMapper getObjectMapper() {
    return new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .setSerializationInclusion(Include.NON_ABSENT)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }
}
