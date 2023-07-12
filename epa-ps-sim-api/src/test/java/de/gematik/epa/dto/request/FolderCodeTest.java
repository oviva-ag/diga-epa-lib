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

package de.gematik.epa.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FolderCodeTest {

  @Test
  void fromValueTest() {
    var result = assertDoesNotThrow(() -> FolderCode.fromValue(FolderCode.DENTISTRY_OMS.getName()));

    assertNotNull(result);
    assertEquals(FolderCode.DENTISTRY_OMS, result);
  }

  @Test
  void fromValueUpperCaseTest() {
    var result = assertDoesNotThrow(() -> FolderCode.fromValue(FolderCode.OTHER_MEDICAL.name()));

    assertNotNull(result);
    assertEquals(FolderCode.OTHER_MEDICAL, result);
  }

  @Test
  void fromValueNullTest() {
    assertThrows(IllegalArgumentException.class, () -> FolderCode.fromValue(null));
  }

  @Test
  void fromValueWrongValueTest() {
    var wrongValue = "wrong Folder";
    var result =
        assertThrows(IllegalArgumentException.class, () -> FolderCode.fromValue(wrongValue));

    assertNotNull(result);
    assertTrue(result.getMessage().contains(wrongValue));
  }
}
