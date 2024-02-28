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

package com.oviva.poc.svc.phr;

import com.oviva.poc.svc.model.KonnektorContext;
import com.oviva.poc.svc.phr.model.RecordIdentifier;
import edu.umd.cs.findbugs.annotations.NonNull;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;
import telematik.ws.conn.phrs.phrservice.wsdl.v2_0.PHRServicePortType;

public class PhrServiceClient {

  private final PHRServicePortType phrService;

  private final KonnektorContext konnektorContext;

  public PhrServiceClient(PHRServicePortType phrService, KonnektorContext konnektorContext) {
    this.phrService = phrService;
    this.konnektorContext = konnektorContext;
  }

  public RegistryResponseType documentRepositoryProvideAndRegisterDocumentSetB(
      RecordIdentifier recordIdentifier, @NonNull ProvideAndRegisterDocumentSetRequestType body) {

    var contextHeader =
        ContextHeaderBuilder.fromKonnektorContext(konnektorContext)
            .recordIdentifier(recordIdentifier)
            .build();

    return phrService.documentRepositoryProvideAndRegisterDocumentSetB(contextHeader, body);
  }
}
