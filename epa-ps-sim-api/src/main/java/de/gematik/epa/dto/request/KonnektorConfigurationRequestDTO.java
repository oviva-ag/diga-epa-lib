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

import static de.gematik.epa.constants.Documentation.AS_UPDATE_DESCRIPTION;

import de.gematik.epa.config.Context;
import de.gematik.epa.config.KonnektorConfiguration;
import de.gematik.epa.config.KonnektorConnectionConfigurationDTO;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Primary configuration object for configuration regarding the Konnektor.<br>
 *
 * @param connection Configurations for the connection to the Konnektor
 * @param context a context, that must be present in the info model of the Konnektor.
 * @param asUpdate Switch, whether the submitted data should update the already configured data
 *     (true) or replace them (false).<br>
 *     Update means, that for an element not set, the already configured data will continue to be
 *     used, sparing the caller the need to resubmit unchanged data.
 */
@Schema(description = "Request um die Konnektor spezifische Konfiguration des epa-ps-sim zu ändern")
public record KonnektorConfigurationRequestDTO(
    @Schema(description = "Konfigurationsdaten bzgl. der Verbindung zum Konnektor")
        KonnektorConnectionConfigurationDTO connection,
    @Schema(
            description =
                "Context wie er für Aufrufe von Konnektor-Operationen verwendet werden soll")
        Context context,
    @Schema(description = AS_UPDATE_DESCRIPTION) Boolean asUpdate)
    implements KonnektorConfiguration {}
