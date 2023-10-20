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

package de.gematik.epa.dto.response;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.epa.dto.response.SignDocumentResponse.SignatureForm;
import de.gematik.epa.ihe.model.simple.ByteArray;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class SignDocumentResponseTest {

  @Test
  void signDocumentResponseTest() {
    var baseResponse = new ResponseDTO(true, "All went well");
    var signedDoc = "I am a signed document".getBytes(StandardCharsets.UTF_8);
    var signDocumentResponse1 =
        assertDoesNotThrow(
            () ->
                new SignDocumentResponse(
                    baseResponse.success(),
                    baseResponse.statusMessage(),
                    signedDoc,
                    SignatureForm.DOCUMENT_WITH_SIGNATURE));

    var signDocumentResponse2 =
        assertDoesNotThrow(
            () ->
                new SignDocumentResponse(
                    baseResponse, ByteArray.of(signedDoc), SignatureForm.DOCUMENT_WITH_SIGNATURE));

    assertEquals(signDocumentResponse1, signDocumentResponse2);
  }
}
