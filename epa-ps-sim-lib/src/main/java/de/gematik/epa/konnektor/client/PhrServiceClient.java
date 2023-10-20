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

package de.gematik.epa.konnektor.client;

import de.gematik.epa.config.AuthorInstitutionProvider;
import de.gematik.epa.config.DefaultdataProvider;
import de.gematik.epa.context.ContextHeaderAdapter;
import de.gematik.epa.konnektor.KonnektorContextProvider;
import de.gematik.epa.konnektor.KonnektorInterfaceAssembly;
import de.gematik.epa.konnektor.SmbInformationProvider;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import oasis.names.tc.ebxml_regrep.xsd.lcm._3.RemoveObjectsRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;
import telematik.ws.conn.phrs.phrservice.wsdl.v2_0.PHRServicePortType;

@Accessors(fluent = true)
public class PhrServiceClient extends KonnektorServiceClient {

  @Getter private PHRServicePortType phrService;

  private DefaultdataProvider defaultdataProvider;

  @Getter private ContextHeaderAdapter contextHeader;

  @Getter private AuthorInstitutionProvider authorInstitutionProvider;

  private String kvnr;

  public PhrServiceClient(
      @NonNull KonnektorContextProvider konnektorContextProvider,
      @NonNull KonnektorInterfaceAssembly konnektorInterfaceAssembly,
      @NonNull DefaultdataProvider defaultdataProvider,
      @NonNull String kvnr) {
    super(konnektorContextProvider, konnektorInterfaceAssembly);
    this.defaultdataProvider = defaultdataProvider;
    this.kvnr = kvnr;
    runInitializationSynchronized();
  }

  @Override
  protected void initialize() {
    contextHeader = konnektorContextProvider.createContextHeader(kvnr);
    phrService = konnektorInterfaceAssembly.phrService();
    if (defaultdataProvider.useAuthorInstitutionFromConfigForSubmissionSet()) {
      authorInstitutionProvider = defaultdataProvider.authorInstitutionProviderFromConfig();
    } else {
      authorInstitutionProvider =
          new SmbInformationProvider(konnektorContextProvider, konnektorInterfaceAssembly);
    }
  }

  public RegistryResponseType documentRepositoryProvideAndRegisterDocumentSetB(
      @NonNull ProvideAndRegisterDocumentSetRequestType body) {
    return phrService.documentRepositoryProvideAndRegisterDocumentSetB(contextHeader, body);
  }

  public AdhocQueryResponse documentRegistryRegistryStoredQuery(@NonNull AdhocQueryRequest body) {
    return phrService.documentRegistryRegistryStoredQuery(contextHeader, body);
  }

  public RetrieveDocumentSetResponseType documentRepositoryRetrieveDocumentSet(
      @NonNull RetrieveDocumentSetRequestType body) {
    return phrService.documentRepositoryRetrieveDocumentSet(contextHeader, body);
  }

  public RegistryResponseType documentRegistryDeleteDocumentSet(
      @NonNull RemoveObjectsRequest body) {
    return phrService.documentRegistryDeleteDocumentSet(contextHeader, body);
  }
}
