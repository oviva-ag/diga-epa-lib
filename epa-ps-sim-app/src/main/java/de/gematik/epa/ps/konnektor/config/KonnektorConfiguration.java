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

package de.gematik.epa.ps.konnektor.config;

import de.gematik.epa.data.Context;
import lombok.With;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Primary configuration object for configuration regarding the Konnektor.<br>
 * It's intended use is to be read from the application configuration of this Spring Boot App. It is
 * expected, that in the application configuration (e.g. {@code application.yaml}), there is a
 * section {@code konnektor}, which holds the configuration data, to be loaded into an instance of
 * this record, which will then be used by the {@link
 * de.gematik.epa.ps.konnektor.impl.KonnektorInterfaceImplGenerator} to configure this app.
 *
 * @param connection Configurations for the connection to the Konnektor
 * @param context a context, that must be present in the info model of the Konnektor, which shall be
 *     used for the {@link telematik.ws.conn.connectorcontext.xsd.v2_0.ContextType} element in the
 *     requests to the Konnektor
 */
@ConfigurationProperties("konnektor")
@With
public record KonnektorConfiguration(
    KonnektorConnectionConfiguration connection, Context context) {}
