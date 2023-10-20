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

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Konfigurationsdaten für eine TLS Verbindung")
public record TlsConfig(
    @Schema(
            description =
                "Keystore mit dem privaten Schlüsselmaterial für das epa-ps-sim. Kan entweder als Dateipfad (auf den das epa-ps-sim dann natürlich Zugriff haben muss) oder als Keystore übergeben werden.")
        FileInfo keystorefile,
    String keystorepassword,
    @Schema(description = "Art des Keystores, z.B. PKCS12", example = "PKCS12") String keystoretype,
    @Schema(
            description = "Liste der zulässigen Ciphersuiten",
            example =
                "['TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384', 'TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256', 'TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384', 'TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256', 'TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA', 'TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA']")
        List<String> ciphersuites) {}
