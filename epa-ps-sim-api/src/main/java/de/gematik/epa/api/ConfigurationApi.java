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

import de.gematik.epa.dto.request.KonnektorConfigurationRequestDTO;
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

@Path("configuration")
public interface ConfigurationApi {

  /**
   * Configuration of the Konnektor with which to communicate
   *
   * @param request {@link KonnektorConfigurationRequestDTO} configuration data of the Konnektor
   * @return {@link ResponseDTO} information whether configuring the Konnektor was successfull and
   *     if not detail information about the cause of failure
   */
  @POST
  @Path("configureKonnektor")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Operation(
      summary =
          "Operation um die Konfigurationsdaten zum verwendeten Konnektor zur Laufzeit zu ändern",
      description =
          "Soll zur Laufzeit des epa-ps der verwendete Konnektor geändert werden oder einzelne Konfigurationsdaten für den Konnektor geändert werden, kann das mittels dieser Operation erfolgen.",
      requestBody =
          @RequestBody(
              required = true,
              description = "Konfigurationsdaten für den Konnektor",
              content =
                  @Content(
                      schema = @Schema(implementation = KonnektorConfigurationRequestDTO.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Statusinformation, ob die Operation erfolgreich ausgeführt werden konnte.",
            content = @Content(schema = @Schema(implementation = ResponseDTO.class)))
      })
  ResponseDTO configureKonnektor(KonnektorConfigurationRequestDTO request);
}
