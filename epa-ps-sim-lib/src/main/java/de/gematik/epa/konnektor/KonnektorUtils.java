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

package de.gematik.epa.konnektor;

import de.gematik.epa.dto.response.AuthorizedApplication;
import de.gematik.epa.dto.response.GetAuthorizationStateResponseDTO;
import de.gematik.epa.dto.response.ResponseDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import telematik.ws.conn.connectorcommon.xsd.v5_0.Status;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_0.AuthorizedApplicationType;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_0.GetAuthorizationStateResponse;
import telematik.ws.tel.error.telematikerror.xsd.v2_0.Error;

@UtilityClass
public class KonnektorUtils {

  public static final String STATUS_OK = "OK";

  public static final String STATUS_WARNING = "Warning";

  public static String warnMsgWithOperationName(@NonNull String operationName) {
    return stdWarningLogIntro.apply(" operation " + operationName);
  }

  public static void logWarning(
      @NonNull Logger logger, @NonNull Status konnektorStatus, String message) {
    if (logger.isWarnEnabled()) {
      var msgIntro =
          Optional.ofNullable(message).map(msg -> msg + ": ").orElse(stdWarningLogIntro.apply(""));
      logger.warn("{}\t{}", msgIntro, konnektorStatus.getError());
    }
  }

  public static void logWarning(Logger logger, Status konnektorStatus) {
    logWarning(logger, konnektorStatus, null);
  }

  public static void logWarningIfPresent(
      @NonNull Logger logger, @NonNull Status konnektorStatus, String message) {
    if (STATUS_WARNING.equals(konnektorStatus.getResult()))
      logWarning(logger, konnektorStatus, message);
  }

  public static void logWarningIfPresent(@NonNull Logger logger, @NonNull Status konnektorStatus) {
    logWarningIfPresent(logger, konnektorStatus, null);
  }

  public static ResponseDTO fromStatus(@NonNull Status status) {
    return new ResponseDTO(
        true, Optional.ofNullable(status.getError()).map(Error::toString).orElse(null));
  }

  public static ResponseDTO fromThrowable(@NonNull Throwable throwable) {
    var statusMsgBuilder = new StringBuilder().append(throwable);

    return new ResponseDTO(false, appendCauses(throwable, statusMsgBuilder).toString());
  }

  public static GetAuthorizationStateResponseDTO getAuthorizationStateResponse(
      @NonNull GetAuthorizationStateResponse response) {
    List<AuthorizedApplication> authorizedApplications = new ArrayList<>();

    GetAuthorizationStateResponse.AuthorizationStatusList authorizationStatusList =
        response.getAuthorizationStatusList();
    if (authorizationStatusList != null) {
      List<AuthorizedApplicationType> authorizedApplicationList =
          authorizationStatusList.getAuthorizedApplication();
      if (authorizedApplicationList != null) {
        authorizedApplicationList.forEach(
            authorizedApplicationType -> {
              final var authorizedApplication =
                  new AuthorizedApplication(
                      authorizedApplicationType.getApplicationName(),
                      authorizedApplicationType
                          .getValidTo()
                          .toGregorianCalendar()
                          .toZonedDateTime()
                          .toLocalDate());
              authorizedApplications.add(authorizedApplication);
            });
      }
    }
    return new GetAuthorizationStateResponseDTO(
        true,
        Optional.ofNullable(response.getStatus().getError()).map(Error::toString).orElse(null),
        authorizedApplications);
  }

  // region private

  private static final UnaryOperator<String> stdWarningLogIntro =
      operation -> "The Konnektor" + operation + " responded with a warning! Warning:";

  private static StringBuilder appendCauses(Throwable throwable, StringBuilder builder) {
    Throwable cause = throwable.getCause();

    while (Objects.nonNull(cause)) {
      builder.append(";\t").append(cause);
      cause = cause.getCause();
    }

    return builder;
  }

  // endregion private

}
