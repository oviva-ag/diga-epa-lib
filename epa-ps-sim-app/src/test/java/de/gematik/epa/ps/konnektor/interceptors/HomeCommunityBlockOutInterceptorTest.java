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

package de.gematik.epa.ps.konnektor.interceptors;

import static org.apache.cxf.message.Message.*;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.epa.konnektor.KonnektorContextProvider;
import de.gematik.epa.konnektor.KonnektorInterfaceProvider;
import de.gematik.epa.unit.AppTestDataFactory;
import de.gematik.epa.unit.TestKonnektorInterfaceImplGenerator;
import ihe.iti.xdr._2014.HomeCommunityBlock;
import jakarta.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import lombok.SneakyThrows;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.apache.cxf.service.model.MessageInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestKonnektorInterfaceImplGenerator.class})
class HomeCommunityBlockOutInterceptorTest {

  private final HomeCommunityBlockOutInterceptor tstObj = new HomeCommunityBlockOutInterceptor();

  @SneakyThrows
  @BeforeEach
  void init() {
    var phrManagementSrv =
        KonnektorInterfaceProvider.defaultInstance()
            .getKonnektorInterfaceAssembly()
            .phrManagementService();
    Mockito.when(phrManagementSrv.getHomeCommunityID(Mockito.any()))
        .thenReturn(AppTestDataFactory.createGetHomeCommunityResponse());
    KonnektorContextProvider.defaultInstance().createContextHeader(AppTestDataFactory.KVNR);
  }

  @Test
  void handleMessageTest() {
    var soapMessage = AppTestDataFactory.createCxfSoapMessage();

    assertDoesNotThrow(() -> tstObj.handleMessage(soapMessage));

    assertHcidHeader(soapMessage);
  }

  @Test
  void handleMessageNoClientTest() {
    var soapMessage = AppTestDataFactory.createCxfSoapMessage();
    soapMessage.remove(REQUESTOR_ROLE);

    assertDoesNotThrow(() -> tstObj.handleMessage(soapMessage));

    assertFalse(soapMessage.containsKey(Header.HEADER_LIST));
  }

  @Test
  void handleMessageWrongMessageType() {
    var soapMessage = AppTestDataFactory.createCxfSoapMessage();
    var msgInfo = soapMessage.get(MessageInfo.class);

    assertNotNull(msgInfo);

    var operation = msgInfo.getOperation();
    operation.setName(new QName("TotallyWrong_Name"));

    assertDoesNotThrow(() -> tstObj.handleMessage(soapMessage));

    assertFalse(soapMessage.containsKey(Header.HEADER_LIST));
  }

  @Test
  void handleMessageHeaderListAlreadyPresentTest() {
    var soapMessage = AppTestDataFactory.createCxfSoapMessage();

    soapMessage.putIfAbsent(Header.HEADER_LIST, new ArrayList<Header>());

    assertDoesNotThrow(() -> tstObj.handleMessage(soapMessage));

    assertHcidHeader(soapMessage);
  }

  private void assertHcidHeader(SoapMessage soapMessage) {
    assertTrue(soapMessage.containsKey(Header.HEADER_LIST));

    var headerListObj = soapMessage.get(Header.HEADER_LIST);

    assertTrue(headerListObj instanceof List);

    var headerList = (List<?>) headerListObj;

    assertFalse(headerList.isEmpty());

    var hcidHeaderObj = headerList.get(0);

    assertTrue(hcidHeaderObj instanceof Header);

    var hcidHeader = (Header) hcidHeaderObj;

    assertTrue(hcidHeader.getObject() instanceof JAXBElement<?>);

    var hcidJaxbObj = (JAXBElement<?>) hcidHeader.getObject();

    assertTrue(hcidJaxbObj.getValue() instanceof HomeCommunityBlock);

    var hcidBlock = (HomeCommunityBlock) hcidJaxbObj.getValue();

    assertEquals(AppTestDataFactory.HOME_COMMUNITY_ID, hcidBlock.getHomeCommunityId());
  }
}
