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

import org.junit.jupiter.api.Test;

class StringUtilsTest {

  @Test
  void ensureEndsWithTest() {
    String testString = "teststring";
    String testSuffix = ":12aB/&";

    var tstResult = assertDoesNotThrow(() -> StringUtils.ensureEndsWith(testString, testSuffix));
    assertNotNull(tstResult);
    assertTrue(tstResult.startsWith(testString));
    assertTrue(tstResult.endsWith(testSuffix));

    var tstResult2 = assertDoesNotThrow(() -> StringUtils.ensureEndsWith(tstResult, testSuffix));
    assertTrue(tstResult.startsWith(testString));
    assertTrue(tstResult.endsWith(testSuffix));
    assertEquals(tstResult, tstResult2);
  }

  @Test
  void ensureStartsWithTest() {
    String testString = "teststring";
    String testPrefix = ":/98xY$=";

    var tstResult = assertDoesNotThrow(() -> StringUtils.ensureStartsWith(testString, testPrefix));
    assertNotNull(tstResult);
    assertTrue(tstResult.startsWith(testPrefix));
    assertTrue(tstResult.endsWith(testString));

    var tstResult2 = assertDoesNotThrow(() -> StringUtils.ensureStartsWith(tstResult, testPrefix));
    assertTrue(tstResult.startsWith(testPrefix));
    assertTrue(tstResult.endsWith(testString));
    assertEquals(tstResult, tstResult2);
  }

  @Test
  void getLastAndFirstNameTest() {
    var name = "Annelise Herta Schmidt";

    var result = assertDoesNotThrow(() -> StringUtils.getLastAndFirstName(name));

    assertNotNull(result);
    assertTrue(name.startsWith(result.getLeft()));
    assertTrue(name.endsWith(result.getRight()));
  }

  @Test
  void getLastAndFirstNameNullTest() {
    var result = assertDoesNotThrow(() -> StringUtils.getLastAndFirstName(null));

    assertNotNull(result);
    assertNotNull(result.getLeft());
    assertNotNull(result.getRight());
    assertFalse(result.getLeft().isEmpty());
    assertFalse(result.getRight().isEmpty());
  }
}
