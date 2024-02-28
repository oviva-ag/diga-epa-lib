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

package telematik.ws.conn.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import telematik.ws.tel.error.telematikerror.xsd.v2_0.Error;
import telematik.ws.tel.error.telematikerror.xsd.v2_0.Error.Trace;

class FaultMessageExceptionTest {

  private final Error faultInfo =
      new Error()
          .withMessageID("ID1")
          .withTrace(new Trace().withCode(BigInteger.ONE).withErrorText("kaputt"));

  private final String msg = "This is all wrong";

  @Test
  void minConstructorTest() {
    FaultMessageForTest tstObj = assertDoesNotThrow(() -> new FaultMessageForTest(faultInfo));

    var toString = assertDoesNotThrow(tstObj::toString);

    assertTrue(toString.contains(faultInfo.toString()));
  }

  @Test
  void msgConstructorTest() {
    FaultMessageForTest tstObj = assertDoesNotThrow(() -> new FaultMessageForTest(msg, faultInfo));

    var toString = assertDoesNotThrow(tstObj::toString);

    assertTrue(toString.contains(faultInfo.toString()));
    assertTrue(toString.contains(msg));
  }

  @Test
  void causeConstructorTest() {
    var cause = new IllegalArgumentException("That's so illegal");
    FaultMessageForTest tstObj =
        assertDoesNotThrow(() -> new FaultMessageForTest(msg, cause, null));

    var toString = assertDoesNotThrow(tstObj::toString);

    assertTrue(toString.contains(msg));
  }

  private static class FaultMessageForTest extends FaultMessageException {

    private final Error faultInfo;

    protected FaultMessageForTest(Error faultInfo) {
      super();
      this.faultInfo = faultInfo;
    }

    protected FaultMessageForTest(String message, Throwable cause, Error faultInfo) {
      super(message, cause);
      this.faultInfo = faultInfo;
    }

    protected FaultMessageForTest(String message, Error faultInfo) {
      super(message);
      this.faultInfo = faultInfo;
    }

    @Override
    public Error getFaultInfo() {
      return faultInfo;
    }
  }
}
