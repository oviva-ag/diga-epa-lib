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

import static de.gematik.epa.constants.Documentation.KVNR_DESCRIPTION;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.gematik.epa.ihe.model.document.Document;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(
    description =
        "Request um Dokumente in ein Aktenkonto einzustellen. Das Aktenkonto wird dabei durch die übergebene KVNR identifiziert.")
public record PutDocumentsRequestDTO(
    @JsonProperty(required = true) @Schema(description = KVNR_DESCRIPTION) String kvnr,
    @JsonProperty
        @Schema(
            description =
                "Liste der einzustellenden Dokumente inklusive Metadaten und Folderinformationen."
                    + "Folderinformationen müssen nur übergeben werden, wenn ein Dokument in einen neuen oder bestehenden Folder eingestellt werden soll."
                    + "Dabei gilt: Ist die entryUUID des Folders gesetzt, ist das Dokument in einen bestehenden Folder einzustellen, enthält die codeList mindestens einen Wert ist ein neuer Folder anzulegen, trifft keines von beiden zu, sind die Folder Metadaten ungültig.")
        List<Document> documentSets) {}
