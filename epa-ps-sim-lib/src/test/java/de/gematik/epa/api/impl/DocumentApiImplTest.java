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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.epa.dto.request.DeleteObjectsRequestDTO;
import de.gematik.epa.dto.request.FindRequestDTO;
import de.gematik.epa.dto.request.PutDocumentsRequestDTO;
import de.gematik.epa.dto.response.FindObjectsResponseDTO;
import de.gematik.epa.dto.response.ResponseDTO;
import de.gematik.epa.ihe.model.response.RegistryObjectLists;
import de.gematik.epa.ihe.model.response.RetrieveDocumentElement;
import de.gematik.epa.ihe.model.simple.ByteArray;
import de.gematik.epa.unit.util.ResourceLoader;
import de.gematik.epa.unit.util.TestBase;
import de.gematik.epa.unit.util.TestDataFactory;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;
import java.util.List;
import oasis.names.tc.ebxml_regrep.xsd.lcm._3.RemoveObjectsRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import telematik.ws.conn.eventservice.wsdl.v6_1.FaultMessage;
import telematik.ws.conn.phrs.phrservice.wsdl.v2_0.PHRServicePortType;
import telematik.ws.conn.phrs.phrservice.xsd.v2_0.ContextHeader;
import telematik.ws.conn.signatureservice.xsd.v7_5.GetJobNumberResponse;

class DocumentApiImplTest extends TestBase {

  private final DocumentApiImpl documentApi =
      new DocumentApiImpl(
          konnektorContextProvider(), konnektorInterfaceAssembly(), defaultdataProvider());

  PHRServicePortType phrServiceMock = konnektorInterfaceAssembly().phrService();

  @BeforeEach
  void init() {
    TestDataFactory.initKonnektorTestConfiguration(konnektorInterfaceAssembly());
  }

  @Test
  void putDocumentsTest() {
    Mockito.when(
            phrServiceMock.documentRepositoryProvideAndRegisterDocumentSetB(
                Mockito.any(ContextHeader.class),
                Mockito.any(ProvideAndRegisterDocumentSetRequestType.class)))
        .thenReturn(TestDataFactory.registryResponseSuccess());

    PutDocumentsRequestDTO request = ResourceLoader.putDocumentWithFolderMetadataRequest();

    ResponseDTO expectedResponseDTO =
        new ResponseDTO(true, TestDataFactory.REGISTRY_RESPONSE_STATUS_SUCCESS);

    ResponseDTO actualResponseDTO = assertDoesNotThrow(() -> documentApi.putDocuments(request));
    assertEquals(expectedResponseDTO, actualResponseDTO);
  }

  @Test
  void findByPatientIdRequestTest() {
    Mockito.when(
            phrServiceMock.documentRegistryRegistryStoredQuery(
                Mockito.any(ContextHeader.class), Mockito.any(AdhocQueryRequest.class)))
        .thenReturn(TestDataFactory.getSuccessResponse());
    FindRequestDTO requestDTO = ResourceLoader.findByPatientIdRequest();
    FindObjectsResponseDTO actualResponseDTO = documentApi.find(requestDTO);
    final RegistryObjectLists registryObjectLists = new RegistryObjectLists(null, null, null, null);
    FindObjectsResponseDTO expectedResponseDTO =
        new FindObjectsResponseDTO(
            true, TestDataFactory.REGISTRY_RESPONSE_STATUS_SUCCESS, registryObjectLists);
    assertEquals(expectedResponseDTO, actualResponseDTO);
  }

  @Test
  void getDocumentsTest() {
    var iheResponse = TestDataFactory.retrieveDocumentSetResponse();
    Mockito.when(
            phrServiceMock.documentRepositoryRetrieveDocumentSet(
                Mockito.any(ContextHeader.class),
                Mockito.any(RetrieveDocumentSetRequestType.class)))
        .thenReturn(iheResponse);

    var request = ResourceLoader.retrieveDocumentsRequest();

    var response = assertDoesNotThrow(() -> documentApi.getDocuments(request));

    assertNotNull(response);

    assertArrayEquals(
        iheResponse.getDocumentResponse().stream()
            .map(RetrieveDocumentSetResponseType.DocumentResponse::getDocumentUniqueId)
            .toArray(),
        response.documents().stream().map(RetrieveDocumentElement::documentUniqueId).toArray());

    assertArrayEquals(
        iheResponse.getDocumentResponse().stream()
            .map(RetrieveDocumentSetResponseType.DocumentResponse::getDocument)
            .map(ByteArray::of)
            .toArray(),
        response.documents().stream().map(RetrieveDocumentElement::document).toArray());
  }

  @Test
  void deleteObjectsTest() {
    Mockito.when(
            phrServiceMock.documentRegistryDeleteDocumentSet(
                Mockito.any(ContextHeader.class), Mockito.any(RemoveObjectsRequest.class)))
        .thenReturn(TestDataFactory.registryResponseSuccess());

    var request = new DeleteObjectsRequestDTO(TestDataFactory.KVNR, List.of("id1", "id2"));

    var response = assertDoesNotThrow(() -> documentApi.deleteObjects(request));

    assertNotNull(response);
    assertTrue(response.success());
    assertEquals(TestDataFactory.REGISTRY_RESPONSE_STATUS_SUCCESS, response.statusMessage());
  }

  @Test
  void replaceDocumentsTest() {
    Mockito.when(
            phrServiceMock.documentRepositoryProvideAndRegisterDocumentSetB(
                Mockito.any(ContextHeader.class),
                Mockito.any(ProvideAndRegisterDocumentSetRequestType.class)))
        .thenReturn(TestDataFactory.registryResponseSuccess());

    var replaceDocumentsRequest = ResourceLoader.replaceDocumentsRequest();

    ResponseDTO expectedResponseDTO =
        new ResponseDTO(true, TestDataFactory.REGISTRY_RESPONSE_STATUS_SUCCESS);

    ResponseDTO actualResponseDTO =
        assertDoesNotThrow(() -> documentApi.replaceDocuments(replaceDocumentsRequest));

    assertNotNull(actualResponseDTO);
    assertEquals(expectedResponseDTO, actualResponseDTO);
  }

  @Test
  void replaceDocumentsExceptionTest() {
    var exception = new RuntimeException("I am the expected exception");
    Mockito.when(
            phrServiceMock.documentRepositoryProvideAndRegisterDocumentSetB(
                Mockito.any(ContextHeader.class),
                Mockito.any(ProvideAndRegisterDocumentSetRequestType.class)))
        .thenThrow(exception);

    var replaceDocumentsRequest = ResourceLoader.replaceDocumentsRequest();

    var actualResponseDTO =
        assertDoesNotThrow(() -> documentApi.replaceDocuments(replaceDocumentsRequest));

    assertNotNull(actualResponseDTO);
    assertFalse(actualResponseDTO.success());
    assertTrue(actualResponseDTO.statusMessage().contains(exception.getMessage()));
  }

  @Test
  void putDocumentsExceptionTest() {
    var exception = new RuntimeException("I am the expected exception");
    Mockito.when(
            phrServiceMock.documentRepositoryProvideAndRegisterDocumentSetB(
                Mockito.any(ContextHeader.class),
                Mockito.any(ProvideAndRegisterDocumentSetRequestType.class)))
        .thenThrow(exception);

    var putDocumentsRequest = ResourceLoader.putDocumentWithFolderMetadataRequest();

    var actualResponseDTO = assertDoesNotThrow(() -> documentApi.putDocuments(putDocumentsRequest));

    assertNotNull(actualResponseDTO);
    assertFalse(actualResponseDTO.success());
    assertTrue(actualResponseDTO.statusMessage().contains(exception.getMessage()));
  }

  @Test
  void retrieveDocumentsExceptionTest() {
    var exception = new RuntimeException("I am the expected exception");
    Mockito.when(
            phrServiceMock.documentRepositoryRetrieveDocumentSet(
                Mockito.any(ContextHeader.class),
                Mockito.any(RetrieveDocumentSetRequestType.class)))
        .thenThrow(exception);

    var retrieveDocumentsRequest = ResourceLoader.retrieveDocumentsRequest();

    var actualResponseDTO =
        assertDoesNotThrow(() -> documentApi.getDocuments(retrieveDocumentsRequest));

    assertNotNull(actualResponseDTO);
    assertFalse(actualResponseDTO.success());
    assertTrue(actualResponseDTO.statusMessage().contains(exception.getMessage()));
  }

  @Test
  void deleteDocumentsExceptionTest() {
    var exception = new RuntimeException("I am the expected exception");
    Mockito.when(
            phrServiceMock.documentRegistryDeleteDocumentSet(
                Mockito.any(ContextHeader.class), Mockito.any(RemoveObjectsRequest.class)))
        .thenThrow(exception);

    var deleteObjectsRequest =
        new DeleteObjectsRequestDTO(TestDataFactory.KVNR, List.of("id1", "id2"));

    var actualResponseDTO =
        assertDoesNotThrow(() -> documentApi.deleteObjects(deleteObjectsRequest));

    assertNotNull(actualResponseDTO);
    assertFalse(actualResponseDTO.success());
    assertTrue(actualResponseDTO.statusMessage().contains(exception.getMessage()));
  }

  @Test
  void findExceptionTest() {
    var exception = new RuntimeException("I am the expected exception");
    Mockito.when(
            phrServiceMock.documentRegistryRegistryStoredQuery(
                Mockito.any(ContextHeader.class), Mockito.any(AdhocQueryRequest.class)))
        .thenThrow(exception);

    var findRequest = ResourceLoader.findByPatientIdRequest();

    var actualResponseDTO = assertDoesNotThrow(() -> documentApi.find(findRequest));

    assertNotNull(actualResponseDTO);
    assertFalse(actualResponseDTO.success());
    assertTrue(actualResponseDTO.statusMessage().contains(exception.getMessage()));
  }

  @Test
  void signDocumentTest() {
    Mockito.when(konnektorInterfaceAssembly().eventService().getCards(Mockito.any()))
        .thenReturn(TestDataFactory.getCardsSmbResponse());
    Mockito.when(konnektorInterfaceAssembly().signatureService().getJobNumber(Mockito.any()))
        .thenReturn(new GetJobNumberResponse().withJobNumber("Job001"));
    Mockito.when(konnektorInterfaceAssembly().signatureService().signDocument(Mockito.any()))
        .thenReturn(TestDataFactory.getSignDocumentResponse());

    var signRequest = ResourceLoader.signDocumentRequest();

    var response = assertDoesNotThrow(() -> documentApi.signDocument(signRequest));

    assertNotNull(response);
    assertTrue(response.success());
    assertNotNull(response.signatureObject());
    assertNotNull(response.signatureForm());
  }

  @Test
  void signDocumentExceptionTest() {
    var exceptionMsg = "No card terminal active";
    Mockito.when(konnektorInterfaceAssembly().eventService().getCards(Mockito.any()))
        .thenThrow(new FaultMessage(exceptionMsg, TestDataFactory.getTelematikError()));

    var signRequest = ResourceLoader.signDocumentRequest();

    var response = assertDoesNotThrow(() -> documentApi.signDocument(signRequest));

    assertNotNull(response);
    assertFalse(response.success());
    assertNull(response.signatureObject());
    assertNull(response.signatureForm());
    assertNotNull(response.statusMessage());
    assertTrue(response.statusMessage().contains(exceptionMsg));
  }
}
