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

import de.gematik.epa.dto.request.Confidentiality;
import de.gematik.epa.dto.request.FolderCode;
import de.gematik.epa.dto.request.PermissionHcpoRequest;
import de.gematik.epa.ihe.model.simple.AuthorInstitution;
import de.gematik.epa.utils.StringUtils;
import de.gematik.epa.utils.XmlUtils;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import telematik.ws.conn.cardservice.xsd.v8_1.CardInfoType;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.AuthorizationConfidentialityEnum;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.AuthorizationConfiguration;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.AuthorizationConfiguration.DocumentCategoryList;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.DocumentCategoryEnum;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.GetAuthorizationState;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.RequestFacilityAuthorization;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.RequestFacilityAuthorization.InsurantName;
import telematik.ws.conn.phrs.phrservice.xsd.v2_0.ContextHeader;

@UtilityClass
public class PermissionUtils {
  public static final String PS_USER_AGENT = "PS (Simulation)/1.0.0/gematik GmbH";

  public static RequestFacilityAuthorization createRequestFacilityAuthorizationRequest(
      @NonNull PermissionHcpoRequest permissionHcpoRequest,
      @NonNull AuthorInstitution leiInstitution,
      @NonNull CardInfoType egkInfo,
      @NonNull ContextHeader contextHeader) {
    var requestFacilityAuthorization = new RequestFacilityAuthorization();

    requestFacilityAuthorization.setContext(contextHeader.getContext());
    requestFacilityAuthorization.setRecordIdentifier(contextHeader.getRecordIdentifier());
    requestFacilityAuthorization.setEhcHandle(egkInfo.getCardHandle());
    requestFacilityAuthorization.setInsurantName(insurantNameOf(egkInfo.getCardHolderName()));
    requestFacilityAuthorization.setOrganizationName(leiInstitution.name());
    requestFacilityAuthorization.setAuthorizationConfiguration(
        authorizationConfigurationOf(permissionHcpoRequest));

    return requestFacilityAuthorization;
  }

  public static GetAuthorizationState createRequestGetAuthorizationStateRequest(
      @NonNull ContextHeader contextHeader) {
    var requestGetAuthorizationState = new GetAuthorizationState();
    requestGetAuthorizationState.setContext(contextHeader.getContext());
    requestGetAuthorizationState.setRecordIdentifier(contextHeader.getRecordIdentifier());
    requestGetAuthorizationState.setUserAgent(PS_USER_AGENT);
    return requestGetAuthorizationState;
  }

  // region private

  private static InsurantName insurantNameOf(@NonNull String fullName) {
    var insurantName = new InsurantName();

    var names = StringUtils.getLastAndFirstName(fullName);
    insurantName.setNachname(names.getRight());
    insurantName.setVorname(names.getLeft());

    return insurantName;
  }

  private static AuthorizationConfiguration authorizationConfigurationOf(
      @NonNull PermissionHcpoRequest permissionHcpoRequest) {
    var authorizationConfiguration = new AuthorizationConfiguration();

    authorizationConfiguration.setAuthorizationConfidentiality(
        Optional.ofNullable(permissionHcpoRequest.authorizedConfidentiality())
            .map(Confidentiality::getName)
            .map(AuthorizationConfidentialityEnum::fromValue)
            .orElse(AuthorizationConfidentialityEnum.NORMAL));

    authorizationConfiguration.setExpirationDate(
        Optional.ofNullable(XmlUtils.fromLocalDate(permissionHcpoRequest.expirationDate()))
            .orElse(XmlUtils.fromLocalDate(LocalDate.now().plusDays(7))));

    authorizationConfiguration.setDocumentCategoryList(
        documentCategoryListOf(permissionHcpoRequest.folderCodes()));

    return authorizationConfiguration;
  }

  private static DocumentCategoryList documentCategoryListOf(Collection<FolderCode> folderCodes) {
    var documentCategoryList = new DocumentCategoryList();

    documentCategoryList
        .getDocumentCategoryElement()
        .addAll(
            Optional.ofNullable(folderCodes).stream()
                .flatMap(Collection::stream)
                .map(fc -> DocumentCategoryEnum.fromValue(fc.getName()))
                .toList());

    return documentCategoryList;
  }

  // endregion private
}
