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

import de.gematik.epa.dto.request.DeleteObjectsRequestDTO;
import de.gematik.epa.dto.request.FindRequestDTO;
import de.gematik.epa.dto.request.PutDocumentsRequestDTO;
import de.gematik.epa.dto.request.ReplaceDocumentsRequestDTO;
import de.gematik.epa.dto.request.RetrieveDocumentsRequestDTO;
import de.gematik.epa.dto.request.SignDocumentRequest;
import de.gematik.epa.dto.response.FindObjectsResponseDTO;
import de.gematik.epa.dto.response.ResponseDTO;
import de.gematik.epa.dto.response.RetrieveDocumentsResponseDTO;
import de.gematik.epa.dto.response.SignDocumentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("/document")
public interface DocumentsApi {

  /**
   * Dokumente in das Aktenkonto einstellen
   *
   * <p>Umsetzung epa-FM Operation putDocuments (Afo A_14373-03 und A_14373-05)
   */
  @POST
  @Path("/putDocuments")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Operation(
      summary = "Dokument(e) in ein Aktenkonto einstellen",
      description =
          "Mittels dieser Operation können Dokumente, ggf. mit Folder oder Referenz auf einen bestehenden Folder in eine Aktenkonto (identifiziert mittels KVNR) eingestellt werden.",
      requestBody =
          @RequestBody(
              required = true,
              description =
                  "Übergibt das Dokument und die dazugehörigen Metadaten, sowie ggf. Metadaten des Folders",
              content = @Content(schema = @Schema(implementation = PutDocumentsRequestDTO.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Statusinformation, ob die Operation erfolgreich ausgeführt werden konnte.",
            content = @Content(schema = @Schema(implementation = ResponseDTO.class)))
      })
  ResponseDTO putDocuments(PutDocumentsRequestDTO request);

  /**
   * Dokumente aus einem Aktenkonto abholen
   *
   * @param request Aktenkonte-ID (KVNR) and UniqueIDs der abzuholenden Dokumente
   * @return Statusinformationen und Liste der abgeholten Dokumente
   */
  @POST
  @Path("/getDocuments")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Operation(
      summary = "Dokument(e) aus dem Aktenkonto abrufen",
      description =
          "Mittels dieser Operation können Dokumente aus einem Aktenkonto abgerufen werden",
      requestBody =
          @RequestBody(
              required = true,
              description =
                  "Übergibt die notwendigen Daten um die Dokumente im Aktenkonto identifizieren zu können",
              content =
                  @Content(schema = @Schema(implementation = RetrieveDocumentsRequestDTO.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Die abgerufenen Dokumente oder ggf. Statusinformationen, wenn Fehler aufgetreten sind.",
            content =
                @Content(schema = @Schema(implementation = RetrieveDocumentsResponseDTO.class)))
      })
  RetrieveDocumentsResponseDTO getDocuments(RetrieveDocumentsRequestDTO request);

  /**
   * Dokumente oder Ordner aus dem Aktenkonto loeschen
   *
   * @param request Aktenkonte-ID (KVNR) und entrUUIDs der zu löschenden Objekte
   * @return Statusinformationen über den Ablauf der Operation
   */
  @POST
  @Path("/deleteObjects")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Operation(
      summary = "Dokument(e) in einem Aktenkonto löschen",
      description = "Mittels dieser Operation können Dokumente in einem Aktenkonto gelöscht werden",
      requestBody =
          @RequestBody(
              required = true,
              description =
                  "Übergibt die Daten zur Identifikation des Aktenkontos (KVNR) und der zu löschenden Dokumente (entryUUID)",
              content = @Content(schema = @Schema(implementation = DeleteObjectsRequestDTO.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Statusinformation, ob die Operation erfolgreich ausgeführt werden konnte.",
            content = @Content(schema = @Schema(implementation = ResponseDTO.class)))
      })
  ResponseDTO deleteObjects(DeleteObjectsRequestDTO request);

  /**
   * Dokumente im Aktenkonto finden
   *
   * <p>Umsetzung epa-FM Operation find (Afo A_14373-03 and A_14373-05)
   */
  @POST
  @Path("/find")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Operation(
      summary = "Objekte in einem Aktenkonto finden",
      description =
          "Mittels dieser Operation können Dokumente, Folder, SubmissionSets und Associations in einem Aktenkonto gefunden werden und deren Metadaten zurückgegeben werden",
      requestBody =
          @RequestBody(
              required = true,
              description = "Übergibt die Art der Suche und die Suchparameter für die Suche",
              content = @Content(schema = @Schema(implementation = FindRequestDTO.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Metadaten zu den gefundenen Objekten oder ggf. Statusinformationen zu aufgetretenen Fehlern.",
            content = @Content(schema = @Schema(implementation = FindObjectsResponseDTO.class)))
      })
  FindObjectsResponseDTO find(FindRequestDTO request);

  /**
   * Dokument im Aktenkonto durch neue Dokumente ersetzen
   *
   * @param request Aktenkonte-ID (KVNR), entryUUIDs der zu ersetzenden Dokumente und die neuen
   *     Dokumente samt Metadaten
   * @return Statusinformationen über den Ablauf der Operation
   */
  @POST
  @Path("/replaceDocuments")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Operation(
      summary = "Dokument(e) in einem Aktenkonto ersetzen",
      description = "Mittels dieser Operation können Dokumente in einem Aktenkonto ersetzt werden",
      requestBody =
          @RequestBody(
              required = true,
              description =
                  "Dokument(e) und deren Metadaten, die andere Dokumente, identifiziert durch ihre entryUUID, ersetzen sollen",
              content =
                  @Content(schema = @Schema(implementation = ReplaceDocumentsRequestDTO.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Statusinformation, ob die Operation erfolgreich ausgeführt werden konnte.",
            content = @Content(schema = @Schema(implementation = ResponseDTO.class)))
      })
  ResponseDTO replaceDocuments(ReplaceDocumentsRequestDTO request);

  @POST
  @Path("/signDocument")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Operation(
      summary = "Dokument signieren",
      description =
          "Dokument durch den Konnektor signieren lassen. Gegenwärtig wird nur CMS Signatur mit eingebettetem Dokument unterstützt",
      requestBody =
          @RequestBody(
              required = true,
              description = "Dokument welches signiert werden soll, sowie Signaturparameter",
              content = @Content(schema = @Schema(implementation = SignDocumentRequest.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Erstellte Signatur im Erfolgsfall, andernfalls Fehlerinformationen",
            content = @Content(schema = @Schema(implementation = SignDocumentResponse.class)))
      })
  SignDocumentResponse signDocument(SignDocumentRequest request);
}
