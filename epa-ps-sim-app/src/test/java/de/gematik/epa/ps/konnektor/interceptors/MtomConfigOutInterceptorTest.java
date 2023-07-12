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

import de.gematik.epa.unit.AppTestDataFactory;
import javax.xml.namespace.QName;
import org.apache.cxf.service.model.MessageInfo;
import org.junit.jupiter.api.Test;

class MtomConfigOutInterceptorTest {

  private final MtomConfigOutInterceptor tstObj = new MtomConfigOutInterceptor();

  @Test
  void handleMessageMtomTest() {
    var soapMessage = AppTestDataFactory.createCxfSoapMessage();

    assertDoesNotThrow(() -> tstObj.handleMessage(soapMessage));

    assertNotNull(soapMessage.get(MTOM_ENABLED));
    assertTrue(soapMessage.get(MTOM_ENABLED) instanceof Boolean);

    var mtomEnabled = (Boolean) soapMessage.get(MTOM_ENABLED);

    assertTrue(mtomEnabled);
  }

  @Test
  void handleMessageNoMtomTest() {
    var soapMessage = AppTestDataFactory.createCxfSoapMessage();
    var msgInfo = soapMessage.get(MessageInfo.class);

    assertNotNull(msgInfo);

    msgInfo.setName(new QName("TotallyWrong_Name"));

    assertDoesNotThrow(() -> tstObj.handleMessage(soapMessage));

    assertNotNull(soapMessage.get(MTOM_ENABLED));
    assertTrue(soapMessage.get(MTOM_ENABLED) instanceof Boolean);

    var mtomEnabled = (Boolean) soapMessage.get(MTOM_ENABLED);

    assertFalse(mtomEnabled);
  }
}
