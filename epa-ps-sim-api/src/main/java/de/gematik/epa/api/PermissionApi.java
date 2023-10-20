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

package de.gematik.epa.api;

import de.gematik.epa.dto.request.GetAuthorizationStateRequest;
import de.gematik.epa.dto.request.PermissionHcpoRequest;
import de.gematik.epa.dto.response.GetAuthorizationStateResponseDTO;
import de.gematik.epa.dto.response.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("permission")
public interface PermissionApi {

  @POST
  @Path("/permissionHcpo")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Operation(
      summary = "Adhoc Berechtigung für einen Leistungserbringer einstellen",
      description =
          "Mittels dieser Operation könne Adhoc Berechtigungen für den Leistungserbringer, dessen SM-B gegenwärtig im Konnektor zu dem konfigurierten Context verfügbar ist, für ein Aktenkonto eingestellt werden."
              + " Kann nur ausgeführt werden, wenn die zum Aktenkonto gehörige eGK ebenfalls im Konnektor verfügbar ist.",
      requestBody =
          @RequestBody(
              required = true,
              description =
                  "Berechtigungsdaten und Identifikationsmerkmal des Aktenkontos (KVNR) zum Erstellen der adhoc Berechtigung",
              content = @Content(schema = @Schema(implementation = PermissionHcpoRequest.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Statusinformation, ob die Operation erfolgreich ausgeführt werden konnte.",
            content = @Content(schema = @Schema(implementation = ResponseDTO.class)))
      })
  ResponseDTO permissionHcpo(PermissionHcpoRequest request);

  @POST
  @Path("/getAuthorizationState")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Operation(
      summary = "Operation getAuthorizationState des Konnektors ausführen",
      description =
          "Mittels dieser Operation können die Authorisierungsinformationen für das ausgewählte Aktenkonto (KVNR) abgerufen werden.",
      requestBody =
          @RequestBody(
              required = true,
              description = "Identifizierungsinformation (KVNR) für das Aktenkonto",
              content =
                  @Content(schema = @Schema(implementation = GetAuthorizationStateRequest.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Liste der authorisierten Anwendungen, ggf. Statusinformationen im Falle eines Fehlers",
            content =
                @Content(schema = @Schema(implementation = GetAuthorizationStateResponseDTO.class)))
      })
  GetAuthorizationStateResponseDTO getAuthorizationState(GetAuthorizationStateRequest request);
}
