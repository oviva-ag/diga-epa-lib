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

package de.gematik.epa.konnektor.cxf.interceptors;

import static de.gematik.epa.konnektor.cxf.interceptors.HomeCommunityBlockOutInterceptor.*;
import static org.apache.cxf.message.Message.*;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.epa.unit.util.TestDataFactory;
import ihe.iti.xdr._2014.HomeCommunityBlock;
import jakarta.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.service.model.MessageInfo;
import org.junit.jupiter.api.Test;
import telematik.ws.conn.phrs.phrservice.xsd.v2_0.ContextHeader;

class HomeCommunityBlockOutInterceptorTest {

  private final HomeCommunityBlockOutInterceptor tstObj = new HomeCommunityBlockOutInterceptor();

  @Test
  void handleMessageGetCtxHeaderTest() {
    var soapMessage = TestDataFactory.createCxfSoapMessage();
    soapMessage.put(ContextHeader.class, TestDataFactory.contextHeader());

    assertDoesNotThrow(() -> tstObj.handleMessage(soapMessage));

    assertHcidHeader(soapMessage);
  }

  @Test
  void handleMessageGetHeaderCtxHeaderTest() {
    var soapMessage = TestDataFactory.createCxfSoapMessage();
    soapMessage
        .getHeaders()
        .add(
            new Header(
                CONTEXT_HEADER_NAME,
                new JAXBElement<>(
                    CONTEXT_HEADER_NAME, ContextHeader.class, TestDataFactory.contextHeader())));

    assertDoesNotThrow(() -> tstObj.handleMessage(soapMessage));

    assertHcidHeader(soapMessage);
  }

  @Test
  void handleMessageGetContentCtxHeaderTest() {
    var soapMessage = TestDataFactory.createCxfSoapMessage();
    soapMessage.setContent(List.class, new ArrayList<>());
    CastUtils.cast(soapMessage.getContent(List.class)).add(TestDataFactory.contextHeader());

    assertDoesNotThrow(() -> tstObj.handleMessage(soapMessage));

    assertHcidHeader(soapMessage);
  }

  @Test
  void handleMessageNoClientTest() {
    var soapMessage = TestDataFactory.createCxfSoapMessage();
    soapMessage.remove(REQUESTOR_ROLE);

    assertDoesNotThrow(() -> tstObj.handleMessage(soapMessage));

    assertFalse(soapMessage.containsKey(Header.HEADER_LIST));
  }

  @Test
  void handleMessageWrongMessageType() {
    var soapMessage = TestDataFactory.createCxfSoapMessage();
    var msgInfo = soapMessage.get(MessageInfo.class);

    assertNotNull(msgInfo);

    var operation = msgInfo.getOperation();
    operation.setName(new QName("TotallyWrong_Name"));

    assertDoesNotThrow(() -> tstObj.handleMessage(soapMessage));

    assertFalse(soapMessage.containsKey(Header.HEADER_LIST));
  }

  @Test
  void handleMessageHeaderListAlreadyPresentTest() {
    var soapMessage = TestDataFactory.createCxfSoapMessage();
    soapMessage.put(ContextHeader.class, TestDataFactory.contextHeader());

    soapMessage.putIfAbsent(Header.HEADER_LIST, new ArrayList<Header>());

    assertDoesNotThrow(() -> tstObj.handleMessage(soapMessage));

    assertHcidHeader(soapMessage);
  }

  private void assertHcidHeader(SoapMessage soapMessage) {
    var hcidHeaderObj = soapMessage.getHeader(HOME_COMMUNITY_BLOCK);

    assertNotNull(hcidHeaderObj);

    assertTrue(hcidHeaderObj.getObject() instanceof JAXBElement<?>);

    var hcidJaxbObj = (JAXBElement<?>) hcidHeaderObj.getObject();

    assertTrue(hcidJaxbObj.getValue() instanceof HomeCommunityBlock);

    var hcidBlock = (HomeCommunityBlock) hcidJaxbObj.getValue();

    assertEquals(TestDataFactory.HOME_COMMUNITY_ID, hcidBlock.getHomeCommunityId());
  }
}
