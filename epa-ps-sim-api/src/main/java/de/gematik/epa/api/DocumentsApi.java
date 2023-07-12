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
import de.gematik.epa.dto.response.FindObjectsResponseDTO;
import de.gematik.epa.dto.response.ResponseDTO;
import de.gematik.epa.dto.response.RetrieveDocumentsResponseDTO;
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
  ResponseDTO replaceDocuments(ReplaceDocumentsRequestDTO request);
}
