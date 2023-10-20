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

package de.gematik.epa.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.net.MalformedURLException;
import java.net.URL;

@Schema(description = "Daten um die Address-URL eines Konnektors zu konfigurieren")
public record AddressConfig(
    @JsonProperty(required = true) @Schema(description = "Hostname oder IP des Konnektors")
        String host,
    @JsonProperty(defaultValue = DEFAULT_PORT)
        @Schema(
            description =
                "Portnummer des Ports unter dem die Dienste des Konnektors erreichbar sind")
        Integer port,
    @JsonProperty(defaultValue = DEFAULT_PROTOCOL)
        @Schema(
            description =
                "Schema bzw. Protocol das zur Kommunikation mit dem Konnektor verwendet werden soll. Z.B. http oder https")
        String protocol,
    @JsonProperty
        @Schema(
            description =
                "Pfad unter dem die Konnektordienste, bei der gegebenen Adresse zu erreichen sind. Kann weggelassen werden, wenn kein Pfad benötigt wird")
        String path) {

  public static final String DEFAULT_PROTOCOL = "http";

  public static final String DEFAULT_PORT = "80";

  private static final String DELIMITER = "/";

  public URL createUrl() throws MalformedURLException {
    String fullPath = createFullPath();
    return new URL(protocol, host, port, fullPath);
  }

  private String createFullPath() {
    String fullPath;
    if (path != null && path.startsWith(DELIMITER)) {
      fullPath = path;
    } else {
      fullPath = DELIMITER + (path != null ? path : "");
    }
    return fullPath;
  }
}
