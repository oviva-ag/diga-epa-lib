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

import de.gematik.epa.api.impl.PermissionApiImpl;
import de.gematik.epa.konnektor.KonnektorContextProvider;
import de.gematik.epa.konnektor.SmbInformationProvider;
import de.gematik.epa.konnektor.client.EventServiceClient;
import de.gematik.epa.konnektor.client.PhrManagementClient;
import org.springframework.stereotype.Service;

@Service
public class PermissionApiEndpoint extends PermissionApiImpl {

  public PermissionApiEndpoint(
      KonnektorContextProvider contextProvider,
      EventServiceClient eventServiceClient,
      SmbInformationProvider smbInformationProvider,
      PhrManagementClient phrMgmtClient) {
    super(contextProvider, eventServiceClient, smbInformationProvider, phrMgmtClient);
  }
}
