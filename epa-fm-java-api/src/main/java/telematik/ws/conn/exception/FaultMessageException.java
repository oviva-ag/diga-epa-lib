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

package telematik.ws.conn.exception;

import jakarta.xml.ws.WebServiceException;
import java.util.Optional;

/** Super class for the FaultMessage fault classes, of the Konnektor SOAP services. */
public abstract class FaultMessageException extends WebServiceException {

  protected FaultMessageException() {
    super();
  }

  protected FaultMessageException(String message, Throwable cause) {
    super(message, cause);
  }

  protected FaultMessageException(String message) {
    super(message);
  }

  public abstract telematik.ws.tel.error.telematikerror.xsd.v2_0.Error getFaultInfo();

  @Override
  public String toString() {
    return super.toString() + Optional.ofNullable(getFaultInfo()).map(fi -> ";\t" + fi).orElse("");
  }
}
