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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.epa.dto.request.SignDocumentRequest;
import de.gematik.epa.dto.request.SignDocumentRequest.SignatureAlgorithm;
import de.gematik.epa.ihe.model.simple.ByteArray;
import de.gematik.epa.unit.util.ResourceLoader;
import de.gematik.epa.unit.util.TestBase;
import de.gematik.epa.unit.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import telematik.ws.conn.signatureservice.xsd.v7_5.GetJobNumberResponse;
import telematik.ws.conn.signatureservice.xsd.v7_5.SignDocument;

class SignatureServiceClientTest extends TestBase {

  private SignatureServiceClient tstObj;

  @BeforeEach
  void beforeEach() {
    TestDataFactory.initKonnektorTestConfiguration(konnektorInterfaceAssembly());
    tstObj = new SignatureServiceClient(konnektorContextProvider(), konnektorInterfaceAssembly());
  }

  @Test
  void transformRequestTest() {
    Mockito.when(konnektorInterfaceAssembly().eventService().getCards(Mockito.any()))
        .thenReturn(TestDataFactory.getCardsSmbResponse());
    Mockito.when(konnektorInterfaceAssembly().signatureService().getJobNumber(Mockito.any()))
        .thenReturn(new GetJobNumberResponse().withJobNumber("Job001"));

    var signDocumentRequest = ResourceLoader.signDocumentRequest();

    var konSignDocumentRequest =
        assertDoesNotThrow(() -> tstObj.transformRequest(signDocumentRequest));

    assertNotNull(konSignDocumentRequest);
    assertEquals(
        signDocumentRequest.signatureAlgorithm().name(), konSignDocumentRequest.getCrypt());
    assertNotNull(konSignDocumentRequest.getCardHandle());
    assertNotNull(konSignDocumentRequest.getJobNumber());
    assertEquals(1, konSignDocumentRequest.getSignRequest().size());

    var konSignRequestForDocument = konSignDocumentRequest.getSignRequest().get(0);
    assertEquals(
        signDocumentRequest.signatureType().uri().toString(),
        konSignRequestForDocument.getOptionalInputs().getSignatureType());
    assertArrayEquals(
        signDocumentRequest.document().value(),
        konSignRequestForDocument.getDocument().getBase64Data().getValue());
  }

  @Test
  void transformRequestMissingDocumentTest() {
    Mockito.when(konnektorInterfaceAssembly().eventService().getCards(Mockito.any()))
        .thenReturn(TestDataFactory.getCardsSmbResponse());
    Mockito.when(konnektorInterfaceAssembly().signatureService().getJobNumber(Mockito.any()))
        .thenReturn(new GetJobNumberResponse().withJobNumber("Job001"));

    var signDocumentRequest =
        new SignDocumentRequest((ByteArray) null, false, SignatureAlgorithm.RSA_ECC);

    var konSignDocument = assertDoesNotThrow(() -> tstObj.transformRequest(signDocumentRequest));

    assertNotNull(konSignDocument);
    assertEquals(1, konSignDocument.getSignRequest().size());
    assertNull(konSignDocument.getSignRequest().get(0));
  }

  @Test
  void transformResponse() {
    var konSignResponse = TestDataFactory.getSignDocumentResponse();

    var signResponse = assertDoesNotThrow(() -> tstObj.transformResponse(konSignResponse));

    assertNotNull(signResponse);
    assertTrue(signResponse.success());
    assertNotNull(signResponse.signatureForm());

    var konSignDocumentResponse = konSignResponse.getSignResponse().get(0);
    assertArrayEquals(
        konSignDocumentResponse.getSignatureObject().getBase64Signature().getValue(),
        signResponse.signatureObject().value());
  }

  @Test
  void signDocument() {
    Mockito.when(konnektorInterfaceAssembly().signatureService().signDocument(Mockito.any()))
        .thenReturn(TestDataFactory.getSignDocumentResponse());

    var signDocumentRequest = new SignDocument();

    var response = assertDoesNotThrow(() -> tstObj.signDocument(signDocumentRequest));

    assertNotNull(response);
  }
}
