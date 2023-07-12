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

import static de.gematik.epa.dto.request.PermissionHcpoRequest.DEFAULT_CONFIDENTIALITY;
import static de.gematik.epa.dto.request.PermissionHcpoRequest.DEFAULT_DURATION_IN_DAYS;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PermissionHcpoRequestTest {

  private static final String KVNR = "X324832743";

  private static final Set<FolderCode> FOLDER_CODE_SET =
      Set.of(FolderCode.DENTALRECORD, FolderCode.EAB);

  @Test
  void minimalConstructorTest() {
    var result = assertDoesNotThrow(() -> new PermissionHcpoRequest(KVNR, FOLDER_CODE_SET));

    assertEquals(KVNR, result.kvnr());
    assertEquals(FOLDER_CODE_SET, result.folderCodes());
    assertEquals(DEFAULT_CONFIDENTIALITY, result.authorizedConfidentiality());
    assertEquals(LocalDate.now().plusDays(DEFAULT_DURATION_IN_DAYS), result.expirationDate());
  }

  @Test
  void noConfidentialityConstructorTest() {
    var duration = 5L;
    var result =
        assertDoesNotThrow(
            () ->
                new PermissionHcpoRequest(
                    KVNR, LocalDate.now().plusDays(duration), FOLDER_CODE_SET));

    assertEquals(KVNR, result.kvnr());
    assertEquals(FOLDER_CODE_SET, result.folderCodes());
    assertEquals(DEFAULT_CONFIDENTIALITY, result.authorizedConfidentiality());
    assertEquals(LocalDate.now().plusDays(duration), result.expirationDate());
  }
}
