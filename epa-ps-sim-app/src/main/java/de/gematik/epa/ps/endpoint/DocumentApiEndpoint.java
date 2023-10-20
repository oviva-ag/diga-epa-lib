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

package de.gematik.epa.ps.endpoint;

import de.gematik.epa.api.impl.DocumentApiImpl;
import de.gematik.epa.config.DefaultdataProvider;
import de.gematik.epa.konnektor.KonnektorContextProvider;
import de.gematik.epa.konnektor.KonnektorInterfaceAssembly;
import org.springframework.stereotype.Service;

/**
 * Create an endpoint of the {@link de.gematik.epa.api.DocumentsApi} interface based on the {@link
 * DocumentApiImpl} implementation.<br>
 * It is autoconfigured through the cxf-spring-boot-starter-jaxrs, so no manual setup is required
 */
@Service
public final class DocumentApiEndpoint extends DocumentApiImpl {

  public DocumentApiEndpoint(
      KonnektorContextProvider contextProvider,
      KonnektorInterfaceAssembly konnektorInterfaceAssembly,
      DefaultdataProvider defaultdataProvider) {
    super(contextProvider, konnektorInterfaceAssembly, defaultdataProvider);
  }
}
