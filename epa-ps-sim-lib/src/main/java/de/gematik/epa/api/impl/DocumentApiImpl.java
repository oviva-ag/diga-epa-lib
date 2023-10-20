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

import de.gematik.epa.LibIheXdsMain;
import de.gematik.epa.api.DocumentsApi;
import de.gematik.epa.config.DefaultdataProvider;
import de.gematik.epa.context.ContextHeaderAdapter;
import de.gematik.epa.conversion.ResponseUtils;
import de.gematik.epa.dto.request.DeleteObjectsRequestDTO;
import de.gematik.epa.dto.request.FindRequestDTO;
import de.gematik.epa.dto.request.PutDocumentsRequestDTO;
import de.gematik.epa.dto.request.ReplaceDocumentsRequestDTO;
import de.gematik.epa.dto.request.RetrieveDocumentsRequestDTO;
import de.gematik.epa.dto.request.SignDocumentRequest;
import de.gematik.epa.dto.response.FindObjectsResponseDTO;
import de.gematik.epa.dto.response.ResponseDTO;
import de.gematik.epa.dto.response.RetrieveDocumentsResponseDTO;
import de.gematik.epa.dto.response.SignDocumentResponse;
import de.gematik.epa.ihe.model.document.DocumentInterface;
import de.gematik.epa.ihe.model.request.DeleteObjectsRequest;
import de.gematik.epa.ihe.model.request.DocumentReplaceRequest;
import de.gematik.epa.ihe.model.request.DocumentSubmissionRequest;
import de.gematik.epa.ihe.model.request.FindRequest;
import de.gematik.epa.ihe.model.request.RetrieveDocumentsRequest;
import de.gematik.epa.ihe.model.response.ProxyFindResponse;
import de.gematik.epa.ihe.model.response.ProxyResponse;
import de.gematik.epa.ihe.model.simple.ByteArray;
import de.gematik.epa.ihe.model.simple.SubmissionSetMetadata;
import de.gematik.epa.konnektor.KonnektorContextProvider;
import de.gematik.epa.konnektor.KonnektorInterfaceAssembly;
import de.gematik.epa.konnektor.KonnektorUtils;
import de.gematik.epa.konnektor.client.PhrServiceClient;
import de.gematik.epa.konnektor.client.SignatureServiceClient;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;
import telematik.ws.conn.phrs.phrservice.xsd.v2_0.ContextHeader;

/** Implementation of the operations of the {@link DocumentsApi}.<br> */
@Accessors(fluent = true)
@RequiredArgsConstructor
@Slf4j
public class DocumentApiImpl implements DocumentsApi {

  private final KonnektorContextProvider contextProvider;

  private final KonnektorInterfaceAssembly konnektorInterfaceAssembly;

  private final DefaultdataProvider defaultdataProvider;

  @Override
  public ResponseDTO putDocuments(PutDocumentsRequestDTO request) {
    log.info("Running operation putDocuments");
    try (var phrServiceClient = newPhrServiceClient(request.kvnr())) {

      var provideAndRegisterRequest =
          LibIheXdsMain.convertDocumentSubmissionRequest(
              toDocumentSubmissionRequest(request, phrServiceClient));
      var provideAndRegisterResponse =
          phrServiceClient.documentRepositoryProvideAndRegisterDocumentSetB(
              provideAndRegisterRequest);

      return toResponseDTO(provideAndRegisterResponse);
    } catch (Exception e) {
      log.error("Operation putDocuments failed with an exception", e);
      return KonnektorUtils.fromThrowable(e);
    }
  }

  @Override
  public RetrieveDocumentsResponseDTO getDocuments(RetrieveDocumentsRequestDTO request) {
    log.info("Running operation getDocuments");
    try (var phrServiceClient = newPhrServiceClient(request.kvnr())) {
      var iheRequest =
          LibIheXdsMain.convertRetrieveDocumentsRequest(
              toRetrieveDocumentsRequest(request, contextProvider.getContextHeader()));

      var iheResponse = phrServiceClient.documentRepositoryRetrieveDocumentSet(iheRequest);

      return toRetrieveDocumentsResponseDTO(iheResponse);
    } catch (Exception e) {
      log.error("Operation getDocuments failed with an exception", e);
      return toRetrieveDocumentsResponseDTO(KonnektorUtils.fromThrowable(e));
    }
  }

  @Override
  public ResponseDTO deleteObjects(DeleteObjectsRequestDTO request) {
    log.info("Running operation deleteObjects");
    try (var phrServiceClient = newPhrServiceClient(request.kvnr())) {

      var iheRequest = LibIheXdsMain.convertDeleteObjectsRequest(toDeleteObjectsRequest(request));

      var iheResponse = phrServiceClient.documentRegistryDeleteDocumentSet(iheRequest);

      return toResponseDTO(iheResponse);
    } catch (Exception e) {
      log.error("Operation deleteObjects failed with an exception", e);
      return KonnektorUtils.fromThrowable(e);
    }
  }

  @Override
  public FindObjectsResponseDTO find(FindRequestDTO request) {
    log.info("Running operation find");
    try (var phrServiceClient = newPhrServiceClient(request.kvnr())) {

      var fmRequestBody =
          LibIheXdsMain.convertFindRequest(
              toFindRequest(request, contextProvider.getContextHeader()));

      var fmResponse = phrServiceClient.documentRegistryRegistryStoredQuery(fmRequestBody);

      return toFindResponseDTO(ResponseUtils.toProxyFindResponse(fmResponse));
    } catch (Exception e) {
      log.error("Operation find failed with an exception", e);
      return toFindObjectsResponseDTO(KonnektorUtils.fromThrowable(e));
    }
  }

  @Override
  public ResponseDTO replaceDocuments(ReplaceDocumentsRequestDTO request) {
    log.info("Running operation replaceDocuments");
    try (var phrServiceClient = newPhrServiceClient(request.kvnr())) {

      var provideAndRegisterRequest =
          LibIheXdsMain.convertDocumentReplaceRequest(
              toDocumentReplaceRequest(request, phrServiceClient));

      var provideAndRegisterResponse =
          phrServiceClient.documentRepositoryProvideAndRegisterDocumentSetB(
              provideAndRegisterRequest);

      return toResponseDTO(provideAndRegisterResponse);
    } catch (Exception e) {
      log.error("Operation replaceDocuments failed with an exception", e);
      return KonnektorUtils.fromThrowable(e);
    }
  }

  @Override
  public SignDocumentResponse signDocument(SignDocumentRequest request) {
    log.info("Running operation signDocument");
    try (var signatureServiceClient =
        new SignatureServiceClient(contextProvider, konnektorInterfaceAssembly)) {
      var konRequest = signatureServiceClient.transformRequest(request);

      var konResponse = signatureServiceClient.signDocument(konRequest);

      return signatureServiceClient.transformResponse(konResponse);
    } catch (Exception e) {
      log.error("Operation signDocument failed with an exception", e);
      return toSignDocumentResponse(KonnektorUtils.fromThrowable(e));
    }
  }

  // region private

  private PhrServiceClient newPhrServiceClient(String kvnr) {
    return new PhrServiceClient(
        contextProvider, konnektorInterfaceAssembly, defaultdataProvider, kvnr);
  }

  private FindObjectsResponseDTO toFindResponseDTO(ProxyFindResponse proxyFindResponse) {
    return new FindObjectsResponseDTO(
        proxyFindResponse.success(),
        proxyFindResponse.statusMessage(),
        proxyFindResponse.registryObjectLists());
  }

  private ResponseDTO toResponseDTO(ProxyResponse proxyResponse) {
    return new ResponseDTO(proxyResponse.success(), proxyResponse.statusMessage());
  }

  private RetrieveDocumentsResponseDTO toRetrieveDocumentsResponseDTO(
      RetrieveDocumentSetResponseType iheResponse) {
    var proxyResponse = LibIheXdsMain.convertRetrieveDocumentSetResponse(iheResponse);
    return new RetrieveDocumentsResponseDTO(
        proxyResponse.success(), proxyResponse.statusMessage(), proxyResponse.documents());
  }

  private RetrieveDocumentsResponseDTO toRetrieveDocumentsResponseDTO(ResponseDTO responseDTO) {
    return new RetrieveDocumentsResponseDTO(
        responseDTO.success(), responseDTO.statusMessage(), List.of());
  }

  private ResponseDTO toResponseDTO(RegistryResponseType iheResponse) {
    var proxyResponse = ResponseUtils.toProxyResponse(iheResponse);
    return toResponseDTO(proxyResponse);
  }

  private FindObjectsResponseDTO toFindObjectsResponseDTO(ResponseDTO responseDTO) {
    return new FindObjectsResponseDTO(responseDTO.success(), responseDTO.statusMessage(), null);
  }

  private SignDocumentResponse toSignDocumentResponse(ResponseDTO responseDTO) {
    return new SignDocumentResponse(
        responseDTO.success(), responseDTO.statusMessage(), (ByteArray) null, null);
  }

  private FindRequest toFindRequest(FindRequestDTO dto, ContextHeaderAdapter contextHeader) {
    return new FindRequest(
        contextHeader.getRecordIdentifier(), dto.returnType(), dto.query(), dto.queryData());
  }

  private DocumentSubmissionRequest toDocumentSubmissionRequest(
      PutDocumentsRequestDTO request, PhrServiceClient phrServiceClient) {
    return new DocumentSubmissionRequest(
        phrServiceClient.contextHeader().getRecordIdentifier(),
        request.documentSets(),
        getSubmissionSetMetadata(request.documentSets(), phrServiceClient));
  }

  private DocumentReplaceRequest toDocumentReplaceRequest(
      ReplaceDocumentsRequestDTO request, PhrServiceClient phrServiceClient) {
    return new DocumentReplaceRequest(
        phrServiceClient.contextHeader().getRecordIdentifier(),
        request.documentSets(),
        getSubmissionSetMetadata(request.documentSets(), phrServiceClient));
  }

  private SubmissionSetMetadata getSubmissionSetMetadata(
      List<? extends DocumentInterface> documentSet, PhrServiceClient phrServiceClient) {
    return new SubmissionSetMetadata(
        Collections.singletonList(
            defaultdataProvider.useFirstDocumentAuthorForSubmissionSet()
                ? defaultdataProvider.getSubmissionSetAuthorFromDocuments(documentSet)
                : defaultdataProvider.getSubmissionSetAuthorFromConfig(
                    phrServiceClient.authorInstitutionProvider())),
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

  private DeleteObjectsRequest toDeleteObjectsRequest(DeleteObjectsRequestDTO psRequest) {
    return new DeleteObjectsRequest(psRequest.entryUUIDs());
  }

  // endregion private
}
