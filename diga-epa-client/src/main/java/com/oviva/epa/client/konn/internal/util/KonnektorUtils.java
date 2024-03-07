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

package com.oviva.epa.client.konn.internal.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.slf4j.Logger;
import telematik.ws.conn.connectorcommon.xsd.v5_0.Status;

public class KonnektorUtils {

  private KonnektorUtils() {}

  public static final String STATUS_WARNING = "Warning";
  private static final UnaryOperator<String> stdWarningLogIntro =
      operation -> "The Konnektor" + operation + " responded with a warning! Warning:";

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
    if (STATUS_WARNING.equals(konnektorStatus.getResult())) {
      logWarning(logger, konnektorStatus, message);
    }
  }

  // region private

  public static void logWarningIfPresent(@NonNull Logger logger, @NonNull Status konnektorStatus) {
    logWarningIfPresent(logger, konnektorStatus, null);
  }

  // endregion private

}
