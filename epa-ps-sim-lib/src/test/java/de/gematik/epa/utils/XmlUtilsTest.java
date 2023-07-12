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

package de.gematik.epa.utils;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.epa.unit.util.TestDataFactory;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import telematik.ws.conn.cardservice.xsd.v8_1.GetPinStatus;
import telematik.ws.conn.connectorcontext.xsd.v2_0.ContextType;

class XmlUtilsTest {

  private static final ContextType CONTEXT = TestDataFactory.contextType();

  private static final GetPinStatus GET_PIN_STATUS = createGetPinStatus();

  @Test
  void marshalJaxbElement() {
    var contextMarshalled =
        assertDoesNotThrow(
            () -> XmlUtils.marshal(CONTEXT), "Method XmlUtils.marshal threw unexpected exception");

    assertNotNull(contextMarshalled, "Returned marshalled XML was null");

    var contextAgain =
        assertDoesNotThrow(
            () -> XmlUtils.unmarshal(ContextType.class, contextMarshalled),
            "Method XmlUtils.unmarshal threw unexpected exception");

    assertNotNull(contextAgain, "Returned marshalled XML was null");
    assertContext(CONTEXT, contextAgain);
  }

  @Test
  void marshalXmlRoot() {
    var getPinStatusMarshalled =
        assertDoesNotThrow(
            () -> XmlUtils.marshal(GET_PIN_STATUS),
            "Method XmlUtils.marshal threw unexpected exception");

    assertNotNull(getPinStatusMarshalled, "Returned marshalled XML was null");

    var getPinStatusAgain =
        assertDoesNotThrow(
            () -> XmlUtils.unmarshal(GetPinStatus.class, getPinStatusMarshalled),
            "Method XmlUtils.unmarshal threw unexpected exception");

    assertNotNull(getPinStatusAgain, "Returned marshalled XML was null");
    assertEquals(GET_PIN_STATUS.getCardHandle(), getPinStatusAgain.getCardHandle());
    assertEquals(GET_PIN_STATUS.getPinTyp(), getPinStatusAgain.getPinTyp());
    assertContext(GET_PIN_STATUS.getContext(), getPinStatusAgain.getContext());
  }

  private static GetPinStatus createGetPinStatus() {
    var result = new GetPinStatus();
    result.setCardHandle("card_handle_01");
    result.setContext(CONTEXT);
    result.setPinTyp("MOEP");
    return result;
  }

  private static void assertContext(ContextType expected, ContextType value) {
    assertEquals(expected.getClientSystemId(), value.getClientSystemId());
    assertEquals(expected.getMandantId(), value.getMandantId());
    assertEquals(expected.getWorkplaceId(), value.getWorkplaceId());
    assertEquals(expected.getUserId(), value.getUserId());
  }

  @Test
  void fromLocalDateTest() {
    var localDate = LocalDate.now();

    var result = assertDoesNotThrow(() -> XmlUtils.fromLocalDate(localDate));

    assertNotNull(result);
    assertEquals(localDate.getYear(), result.getYear());
    assertEquals(localDate.getMonthValue(), result.getMonth());
    assertEquals(localDate.getDayOfMonth(), result.getDay());
  }

  @Test
  void fromLocalDateNullTest() {
    var result = assertDoesNotThrow(() -> XmlUtils.fromLocalDate(null));

    assertNull(result);
  }
}
