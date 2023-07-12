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

package de.gematik.epa.konnektor.conversion;

import static de.gematik.epa.unit.util.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.epa.dto.request.FolderCode;
import de.gematik.epa.dto.request.PermissionHcpoRequest;
import de.gematik.epa.utils.XmlUtils;
import java.util.Set;
import org.junit.jupiter.api.Test;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_0.DocumentCategoryEnum;

class PermissionUtilsTest {

  @Test
  void createRequestFacilityAuthorizationRequest() {
    var permissionHcpoRequest = new PermissionHcpoRequest(KVNR, Set.of(FolderCode.values()));
    var leiInstitution = authorInstitutionConfiguration(false).authorInstitution();
    var egkInfo = cardInfoEgk(KVNR);
    var contextHeader = contextHeader();

    var result =
        assertDoesNotThrow(
            () ->
                PermissionUtils.createRequestFacilityAuthorizationRequest(
                    permissionHcpoRequest, leiInstitution, egkInfo, contextHeader));

    assertNotNull(result);
    assertEquals(contextHeader.getContext(), result.getContext());
    assertEquals(contextHeader.getRecordIdentifier(), result.getRecordIdentifier());
    assertEquals(egkInfo.getCardHandle(), result.getEhcHandle());
    assertEquals(leiInstitution.name(), result.getOrganizationName());

    assertNotNull(result.getInsurantName());
    var insurantName = result.getInsurantName();

    assertTrue(egkInfo.getCardHolderName().startsWith(insurantName.getVorname()));
    assertTrue(egkInfo.getCardHolderName().endsWith(insurantName.getNachname()));

    assertNotNull(result.getAuthorizationConfiguration());
    var authorizationConfiguration = result.getAuthorizationConfiguration();

    assertEquals(
        permissionHcpoRequest.authorizedConfidentiality().getName(),
        authorizationConfiguration.getAuthorizationConfidentiality().value());
    assertEquals(
        XmlUtils.fromLocalDate(permissionHcpoRequest.expirationDate()),
        authorizationConfiguration.getExpirationDate());

    assertNotNull(authorizationConfiguration.getDocumentCategoryList());
    var documentCategoryList =
        authorizationConfiguration.getDocumentCategoryList().getDocumentCategoryElement();
    assertAll(
        permissionHcpoRequest.folderCodes().stream()
            .map(
                fc ->
                    () ->
                        assertTrue(
                            documentCategoryList.contains(
                                DocumentCategoryEnum.fromValue(fc.getName())))));
    assertEquals(
        0,
        documentCategoryList.stream()
            .filter(
                dc ->
                    !permissionHcpoRequest.folderCodes().contains(FolderCode.fromValue(dc.value())))
            .count());
  }
}
