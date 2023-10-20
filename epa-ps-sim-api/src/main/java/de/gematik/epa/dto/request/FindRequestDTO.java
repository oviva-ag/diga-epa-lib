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
import de.gematik.epa.ihe.model.query.Query;
import de.gematik.epa.ihe.model.query.QueryKey;
import de.gematik.epa.ihe.model.query.ReturnType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

@Schema(
    description =
        "Request um den Suchtyp und die Suchparameter für die Objektsuche in einem Aktenkonto zu übergeben")
public record FindRequestDTO(
    @JsonProperty(required = true) @Schema(description = KVNR_DESCRIPTION) String kvnr,
    @JsonProperty(required = true)
        @Schema(
            description =
                "Art der Suche. Unterstützt werden Suche nach Dokumenten (FindDocuments & FindDocumentsByTitle), der aktuellen Version zu einem Dokument (GetRelatedApprovedDocuments), nach Foldern (FindFolders) und ihren Inhalten (GetFolderAndContents), sowie nach allen Objekttypen (GetAll).")
        Query query,
    @JsonProperty(required = true)
        @Schema(
            description = "Liste der Queryparameter und der dafür zu setzenden Werte.",
            example =
                """
                {
                    "$XDSDocumentEntryStatus": [
                      "urn:oasis:names:tc:ebxml-regrep:StatusType:Approved"
                    ],
                    "$XDSDocumentEntryEntryUUID": [
                      "urn:uuid:8299ca9b-065f-43fa-8fc0-e94f42726328"
                    ]
                }
                """)
        Map<QueryKey, List<String>> queryData,
    @JsonProperty(required = true)
        @Schema(
            description =
                "Art der Rückgabe der Suchergebnisse, als Liste von Objektreferenzen (ObjectRef) oder als vollständige Metadaten (LeafClass)")
        ReturnType returnType) {}
