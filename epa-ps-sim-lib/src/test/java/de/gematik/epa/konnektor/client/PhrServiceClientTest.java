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

import de.gematik.epa.unit.util.TestBase;
import de.gematik.epa.unit.util.TestDataFactory;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;
import oasis.names.tc.ebxml_regrep.xsd.lcm._3.RemoveObjectsRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PhrServiceClientTest extends TestBase {

  private PhrServiceClient tstObj;

  @BeforeEach
  void initialize() {
    TestDataFactory.initKonnektorTestConfiguration(konnektorInterfaceAssembly());

    tstObj =
        new PhrServiceClient(
            konnektorContextProvider(),
            konnektorInterfaceAssembly(),
            defaultdataProvider(),
            TestDataFactory.KVNR);
  }

  @Test
  void documentRepositoryProvideAndRegisterDocumentSetBTest() {
    Mockito.when(
            tstObj
                .phrService()
                .documentRepositoryProvideAndRegisterDocumentSetB(Mockito.any(), Mockito.any()))
        .thenReturn(TestDataFactory.registryResponseSuccess());

    var testData = new ProvideAndRegisterDocumentSetRequestType();

    var result =
        assertDoesNotThrow(() -> tstObj.documentRepositoryProvideAndRegisterDocumentSetB(testData));

    assertNotNull(result);
    assertEquals(TestDataFactory.REGISTRY_RESPONSE_STATUS_SUCCESS, result.getStatus());
  }

  @Test
  void documentRegistryRegistryStoredQueryTest() {
    Mockito.when(
            tstObj.phrService().documentRegistryRegistryStoredQuery(Mockito.any(), Mockito.any()))
        .thenReturn(TestDataFactory.getSuccessResponse());

    var testData = new AdhocQueryRequest();

    var result = assertDoesNotThrow(() -> tstObj.documentRegistryRegistryStoredQuery(testData));

    assertNotNull(result);
    assertEquals(TestDataFactory.REGISTRY_RESPONSE_STATUS_SUCCESS, result.getStatus());
  }

  @Test
  void documentRepositoryRetrieveDocumentSetTest() {
    Mockito.when(
            tstObj.phrService().documentRepositoryRetrieveDocumentSet(Mockito.any(), Mockito.any()))
        .thenReturn(TestDataFactory.retrieveDocumentSetResponse());

    var testData = new RetrieveDocumentSetRequestType();

    var result = assertDoesNotThrow(() -> tstObj.documentRepositoryRetrieveDocumentSet(testData));

    assertNotNull(result);
    assertEquals(
        TestDataFactory.REGISTRY_RESPONSE_STATUS_SUCCESS, result.getRegistryResponse().getStatus());
  }

  @Test
  void documentRegistryDeleteDocumentSetTest() {
    Mockito.when(
            tstObj.phrService().documentRegistryDeleteDocumentSet(Mockito.any(), Mockito.any()))
        .thenReturn(TestDataFactory.registryResponseSuccess());

    var testData = new RemoveObjectsRequest();

    var result = assertDoesNotThrow(() -> tstObj.documentRegistryDeleteDocumentSet(testData));

    assertNotNull(result);
    assertEquals(TestDataFactory.REGISTRY_RESPONSE_STATUS_SUCCESS, result.getStatus());
  }
}
