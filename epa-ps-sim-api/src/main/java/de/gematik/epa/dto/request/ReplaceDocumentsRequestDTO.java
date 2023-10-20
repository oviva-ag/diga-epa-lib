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
import de.gematik.epa.ihe.model.document.ReplaceDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Request um Dokumente in einem Aktenkonto zu ersetzen")
public record ReplaceDocumentsRequestDTO(
    @JsonProperty(required = true) @Schema(description = KVNR_DESCRIPTION) String kvnr,
    @JsonProperty(required = true)
        @Schema(
            description =
                "Liste von Dokumenten, den zugehörigen Metadaten und der entryUUID des jeweils zu ersetzenden Dokuments")
        List<ReplaceDocument> documentSets) {}
