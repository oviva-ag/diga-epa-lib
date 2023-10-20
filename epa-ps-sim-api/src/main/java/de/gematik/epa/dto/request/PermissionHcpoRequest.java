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
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Set;

@Schema(
    description =
        "Request um Adhoc Berechtigungen eines Leistungserbringers (identifiziert durch den Konnektor) auf ein Aktenkonto (identifiziert durch die übergebene KVNR) zu erstellen")
public record PermissionHcpoRequest(
    @JsonProperty(required = true) @Schema(description = KVNR_DESCRIPTION) String kvnr,
    @Schema(description = "Datum bis zu dem die Berechtigungen gültig sein sollen")
        LocalDate expirationDate,
    @JsonProperty(defaultValue = "normal")
        @Schema(
            description = "Für Dokumente welcher Vertraulichkeitsstufe gelten die Berechtigungen")
        Confidentiality authorizedConfidentiality,
    @JsonProperty
        @Schema(
            description = "Liste der Ordner auf die der Leistungserbringer zugreifen dürfen soll")
        Set<FolderCode> folderCodes) {

  public static final long DEFAULT_DURATION_IN_DAYS = 7L;

  public static final Confidentiality DEFAULT_CONFIDENTIALITY = Confidentiality.NORMAL;

  public PermissionHcpoRequest(String kvnr, LocalDate expirationDate, Set<FolderCode> folderCodes) {
    this(kvnr, expirationDate, DEFAULT_CONFIDENTIALITY, folderCodes);
  }

  public PermissionHcpoRequest(
      String kvnr, Confidentiality authorizedConfidentiality, Set<FolderCode> folderCodes) {
    this(
        kvnr,
        LocalDate.now().plusDays(DEFAULT_DURATION_IN_DAYS),
        authorizedConfidentiality,
        folderCodes);
  }

  public PermissionHcpoRequest(String kvnr, Set<FolderCode> folderCodes) {
    this(kvnr, DEFAULT_CONFIDENTIALITY, folderCodes);
  }
}
