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

package de.gematik.epa.api.impl;

import de.gematik.epa.api.DocumentsApi;
import de.gematik.epa.config.DefaultdataProvider;
import de.gematik.epa.context.ContextHeaderAdapter;
import de.gematik.epa.conversion.AdhocQueryUtils;
import de.gematik.epa.conversion.DeleteObjectsUtils;
import de.gematik.epa.conversion.ProvideAndRegisterUtils;
import de.gematik.epa.conversion.ResponseUtils;
import de.gematik.epa.conversion.RetrieveDocumentsUtils;
import de.gematik.epa.dto.request.DeleteObjectsRequestDTO;
import de.gematik.epa.dto.request.FindRequestDTO;
import de.gematik.epa.dto.request.PutDocumentsRequestDTO;
import de.gematik.epa.dto.request.ReplaceDocumentsRequestDTO;
import de.gematik.epa.dto.request.RetrieveDocumentsRequestDTO;
import de.gematik.epa.dto.response.FindObjectsResponseDTO;
import de.gematik.epa.dto.response.ResponseDTO;
import de.gematik.epa.dto.response.RetrieveDocumentsResponseDTO;
import de.gematik.epa.ihe.model.document.DocumentInterface;
import de.gematik.epa.ihe.model.request.DeleteObjectsRequest;
import de.gematik.epa.ihe.model.request.DocumentReplaceRequest;
import de.gematik.epa.ihe.model.request.DocumentSubmissionRequest;
import de.gematik.epa.ihe.model.request.FindRequest;
import de.gematik.epa.ihe.model.request.RetrieveDocumentsRequest;
import de.gematik.epa.ihe.model.response.ProxyFindResponse;
import de.gematik.epa.ihe.model.response.ProxyResponse;
import de.gematik.epa.ihe.model.simple.SubmissionSetMetadata;
import de.gematik.epa.konnektor.KonnektorContextProvider;
import de.gematik.epa.konnektor.KonnektorInterfaceProvider;
import de.gematik.epa.konnektor.KonnektorUtils;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;
import telematik.ws.conn.phrs.phrservice.wsdl.v2_0.PHRServicePortType;
import telematik.ws.conn.phrs.phrservice.xsd.v2_0.ContextHeader;

/**
 * Implementation of the operations of the {@link DocumentsApi}.<br>
 * For this to work, the user of the implementation must have provided the {@link
 * KonnektorInterfaceProvider#defaultInstance()} and {@link
 * KonnektorContextProvider#defaultInstance()} with the interfaces to the Konnektor services and the
 * context to be used for calls to the Konnektor.
 */
@Accessors(fluent = true)
@Slf4j
public class DocumentApiImpl implements DocumentsApi {

  @Getter(lazy = true, value = AccessLevel.PROTECTED)
  private final KonnektorInterfaceProvider interfaceProvider =
      KonnektorInterfaceProvider.defaultInstance();

  @Getter(lazy = true, value = AccessLevel.PROTECTED)
  private final KonnektorContextProvider contextProvider =
      KonnektorContextProvider.defaultInstance();

  @Getter(lazy = true, value = AccessLevel.PROTECTED)
  private final DefaultdataProvider defaultdataProvider = DefaultdataProvider.defaultInstance();

  @Getter(lazy = true, value = AccessLevel.PROTECTED)
  private final PHRServicePortType phrServiceClient =
      interfaceProvider().getKonnektorInterfaceAssembly().phrService();

  @Override
  public ResponseDTO putDocuments(PutDocumentsRequestDTO request) {
    try {

      log.info("Running operation putDocuments");
      var contextHeader = contextProvider().createContextHeader(request.kvnr());
      var provideAndRegisterRequest =
          ProvideAndRegisterUtils.toProvideAndRegisterDocumentSetRequest(
              getDocumentSubmissionRequest(request, contextHeader));
      var provideAndRegisterResponse =
          phrServiceClient()
              .documentRepositoryProvideAndRegisterDocumentSetB(
                  contextHeader, provideAndRegisterRequest);

      return toResponseDto(provideAndRegisterResponse);
    } catch (Exception e) {
      log.error("Operation putDocuments failed with an exception", e);
      return KonnektorUtils.fromThrowable(e);
    }
  }

  @Override
  public RetrieveDocumentsResponseDTO getDocuments(RetrieveDocumentsRequestDTO request) {
    try {
      log.info("Running operation getDocuments");
      var contextHeader = contextProvider().createContextHeader(request.kvnr());
      var iheRequest =
          RetrieveDocumentsUtils.toRetrieveDocumentSetRequest(
              toRetrieveDocumentsRequest(request, contextHeader));

      var iheResponse =
          phrServiceClient().documentRepositoryRetrieveDocumentSet(contextHeader, iheRequest);

      return toRetrieveDocumentsResponseDto(iheResponse);
    } catch (Exception e) {
      log.error("Operation getDocuments failed with an exception", e);
      return toRetrieveDocumentsResponseDto(KonnektorUtils.fromThrowable(e));
    }
  }

  @Override
  public ResponseDTO deleteObjects(DeleteObjectsRequestDTO request) {
    try {
      log.info("Running operation deleteObjects");

      var contextHeader = contextProvider().createContextHeader(request.kvnr());
      var iheRequest = DeleteObjectsUtils.toRemoveObjectsRequest(toDeleteObjectRequest(request));

      var iheResponse =
          phrServiceClient().documentRegistryDeleteDocumentSet(contextHeader, iheRequest);

      return toResponseDto(iheResponse);
    } catch (Exception e) {
      log.error("Operation deleteObjects failed with an exception", e);
      return KonnektorUtils.fromThrowable(e);
    }
  }

  @Override
  public FindObjectsResponseDTO find(FindRequestDTO dto) {
    try {
      log.info("Running operation find");

      var contextHeader = contextProvider().createContextHeader(dto.kvnr());
      FindRequest request = toFindRequest(dto, contextHeader);
      AdhocQueryRequest fmRequestBody = AdhocQueryUtils.generateFindRequestBody(request);
      AdhocQueryResponse fmResponse =
          phrServiceClient().documentRegistryRegistryStoredQuery(contextHeader, fmRequestBody);
      return getFindResponseDTO(ResponseUtils.toProxyFindResponse(fmResponse));
    } catch (Exception e) {
      log.error("Operation find failed with an exception", e);
      return toFindObjectsResponseDto(KonnektorUtils.fromThrowable(e));
    }
  }

  @Override
  public ResponseDTO replaceDocuments(ReplaceDocumentsRequestDTO request) {
    try {
      log.info("Running operation replaceDocuments");
      var contextHeader = contextProvider().createContextHeader(request.kvnr());
      var provideAndRegisterRequest =
          ProvideAndRegisterUtils.toProvideAndRegisterDocumentSetRequest(
              getDocumentReplaceRequest(request, contextHeader));
      var provideAndRegisterResponse =
          phrServiceClient()
              .documentRepositoryProvideAndRegisterDocumentSetB(
                  contextHeader, provideAndRegisterRequest);

      return toResponseDto(provideAndRegisterResponse);
    } catch (Exception e) {
      log.error("Operation replaceDocuments failed with an exception", e);
      return KonnektorUtils.fromThrowable(e);
    }
  }

  // region private

  private FindObjectsResponseDTO getFindResponseDTO(ProxyFindResponse proxyFindResponse) {
    return new FindObjectsResponseDTO(
        proxyFindResponse.success(),
        proxyFindResponse.statusMessage(),
        proxyFindResponse.registryObjectLists());
  }

  private ResponseDTO getResponseDTO(ProxyResponse proxyResponse) {
    return new ResponseDTO(proxyResponse.success(), proxyResponse.statusMessage());
  }

  private RetrieveDocumentsResponseDTO toRetrieveDocumentsResponseDto(
      RetrieveDocumentSetResponseType iheResponse) {
    var proxyResponse = ResponseUtils.toRetrieveDocumentResponse(iheResponse);
    return new RetrieveDocumentsResponseDTO(
        proxyResponse.success(), proxyResponse.statusMessage(), proxyResponse.documents());
  }

  private RetrieveDocumentsResponseDTO toRetrieveDocumentsResponseDto(ResponseDTO responseDTO) {
    return new RetrieveDocumentsResponseDTO(
        responseDTO.success(), responseDTO.statusMessage(), List.of());
  }

  private ResponseDTO toResponseDto(RegistryResponseType iheResponse) {
    var proxyResponse = ResponseUtils.toProxyResponse(iheResponse);
    return getResponseDTO(proxyResponse);
  }

  private FindObjectsResponseDTO toFindObjectsResponseDto(ResponseDTO responseDTO) {
    return new FindObjectsResponseDTO(responseDTO.success(), responseDTO.statusMessage(), null);
  }

  private FindRequest toFindRequest(FindRequestDTO dto, ContextHeaderAdapter contextHeader) {
    return new FindRequest(
        contextHeader.getRecordIdentifier(), dto.returnType(), dto.query(), dto.queryData());
  }

  private DocumentSubmissionRequest getDocumentSubmissionRequest(
      PutDocumentsRequestDTO request, ContextHeaderAdapter contextHeader) {
    return new DocumentSubmissionRequest(
        contextHeader.getRecordIdentifier(),
        request.documentSets(),
        getSubmissionSetMetadata(request.documentSets()));
  }

  private DocumentReplaceRequest getDocumentReplaceRequest(
      ReplaceDocumentsRequestDTO request, ContextHeaderAdapter contextHeader) {
    return new DocumentReplaceRequest(
        contextHeader.getRecordIdentifier(),
        request.documentSets(),
        getSubmissionSetMetadata(request.documentSets()));
  }

  private SubmissionSetMetadata getSubmissionSetMetadata(
      List<? extends DocumentInterface> documentSet) {
    return new SubmissionSetMetadata(
        Collections.singletonList(defaultdataProvider().getSubmissionSetAuthor(documentSet)),
        null,
        LocalDateTime.now(),
        null,
        null,
        null);
  }

  private RetrieveDocumentsRequest toRetrieveDocumentsRequest(
      RetrieveDocumentsRequestDTO psRequest, ContextHeader contextHeader) {
    return new RetrieveDocumentsRequest(
        contextHeader.getRecordIdentifier().getHomeCommunityId(), psRequest.documentUniqueIds());
  }

  private DeleteObjectsRequest toDeleteObjectRequest(DeleteObjectsRequestDTO psRequest) {
    return new DeleteObjectsRequest(psRequest.entryUUIDs());
  }

  // endregion private
}
