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

import static de.gematik.epa.constants.Documentation.STATUS_MSG_DESCRIPTION;
import static de.gematik.epa.constants.Documentation.SUCCESS_DESCRIPTION;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.gematik.epa.ihe.model.simple.ByteArray;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response der signDocument Operation mit der erzeugten Signatur")
public record SignDocumentResponse(
    @JsonProperty(required = true) @Schema(description = SUCCESS_DESCRIPTION) Boolean success,
    @Schema(description = STATUS_MSG_DESCRIPTION) String statusMessage,
    @Schema(description = "Die erzeugte Signatur oder das Dokument mit Signatur")
        ByteArray signatureObject,
    @Schema(description = "Format des zurückgegebenen Signaturobjektes")
        SignatureForm signatureForm) {

  public SignDocumentResponse(
      Boolean success, String statusMessage, byte[] signatureObject, SignatureForm signatureForm) {
    this(success, statusMessage, ByteArray.of(signatureObject), signatureForm);
  }

  public SignDocumentResponse(
      ResponseDTO responseBase, ByteArray signatureObject, SignatureForm signatureForm) {
    this(responseBase.success(), responseBase.statusMessage(), signatureObject, signatureForm);
  }

  @Schema(
      description =
          "Format eines Signaturobjektes. 'SIGNATURE' bedeutet, dass es sich um eine Signatur (enveloping oder detached) handelt, "
              + "'DOCUMENT_WITH_SIGNATURE', dass die Signatur im Dokument eingebettet ist (enveloped signature)")
  public enum SignatureForm {
    SIGNATURE,
    DOCUMENT_WITH_SIGNATURE
  }
}
