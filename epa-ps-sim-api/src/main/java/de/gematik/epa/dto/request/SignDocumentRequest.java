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

package de.gematik.epa.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.gematik.epa.ihe.model.simple.ByteArray;
import io.swagger.v3.oas.annotations.media.Schema;
import java.net.URI;
import java.util.Arrays;

@Schema(
    description =
        "Request Objekt um die Erstellung einer Signatur für das übergebene Dokument durch den Konnektor auszulösen")
public record SignDocumentRequest(
    @JsonProperty @Schema(description = "Dokument welches signiert werden soll") ByteArray document,
    @JsonProperty
        @Schema(
            description =
                "Soll eine QES Signatur erstellt werden? true=ja (verwendet HBA), false=nein (verwendet SM-B)")
        boolean performQES,
    @JsonProperty @Schema(description = "Algorithmus für die Signatur (RSA oder ECC)")
        SignatureAlgorithm signatureAlgorithm) {

  public SignDocumentRequest(
      byte[] document, boolean performQES, SignatureAlgorithm signatureAlgorithm) {
    this(ByteArray.of(document), performQES, signatureAlgorithm);
  }

  public SignatureType signatureType() {
    return SignatureType.CMS;
  }

  @Schema(
      description =
          "Signatur Algorithmus der verwendet werden soll. "
              + "Die Option RSA_ECC bedeutet, dass RSA für Karten der Kartengenerationen 2.0 oder kleiner und ECC für Karten ab Kartengeneration 2.1 verwendet wird")
  public enum SignatureAlgorithm {
    RSA_ECC,
    ECC,
    RSA
  }

  /**
   * An enum with one value is not useful.<br>
   * This only exists already, to make adding other signature types like XML oder S/MIME easier in
   * the future.
   */
  public enum SignatureType {
    CMS(URI.create("urn:ietf:rfc:5652"));

    private final URI uri;

    SignatureType(URI uri) {
      this.uri = uri;
    }

    public URI uri() {
      return uri;
    }

    public static SignatureType of(URI uri) {
      return Arrays.stream(SignatureType.values())
          .filter(value -> value.uri().equals(uri))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("Unsupported URI value " + uri));
    }

    @JsonCreator
    public static SignatureType of(String name) {
      return Arrays.stream(SignatureType.values())
          .filter(value -> value.name().equalsIgnoreCase(name))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("Unknown name " + name));
    }
  }
}
