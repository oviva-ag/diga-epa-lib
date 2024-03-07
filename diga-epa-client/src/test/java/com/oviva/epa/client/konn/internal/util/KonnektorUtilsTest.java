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

package com.oviva.epa.client.konn.internal.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import telematik.ws.conn.connectorcommon.xsd.v5_0.Status;
import telematik.ws.tel.error.telematikerror.xsd.v2_0.Error;

class KonnektorUtilsTest {

  private Logger logger;

  private Status statusWarning;

  @BeforeEach
  void initialize() {
    logger = mock(Logger.class);

    statusWarning = new Status();
    statusWarning.setResult(KonnektorUtils.STATUS_WARNING);
    statusWarning.setError(new Error());

    when(logger.isWarnEnabled()).thenReturn(Boolean.TRUE);
  }

  @Test
  void warnMsgWithOperationNameTest() {
    var operation = "operateNow";

    var result =
        Assertions.assertDoesNotThrow(() -> KonnektorUtils.warnMsgWithOperationName(operation));

    assertNotNull(result);
    assertTrue(result.contains(operation));
  }

  @Test
  void logWarningTest() {
    var msg = "This is a message";
    var arg1Capture = ArgumentCaptor.forClass(Object.class);
    var arg2Capture = ArgumentCaptor.forClass(Object.class);

    assertDoesNotThrow(() -> KonnektorUtils.logWarning(logger, statusWarning, msg));

    verify(logger).warn(anyString(), arg1Capture.capture(), arg2Capture.capture());

    assertEquals(msg + ": ", arg1Capture.getValue());
    assertEquals(statusWarning.getError(), arg2Capture.getValue());
  }

  @Test
  void logWarningNoMsgTest() {
    var arg1Capture = ArgumentCaptor.forClass(Object.class);
    var arg2Capture = ArgumentCaptor.forClass(Object.class);

    assertDoesNotThrow(() -> KonnektorUtils.logWarning(logger, statusWarning));

    verify(logger).warn(anyString(), arg1Capture.capture(), arg2Capture.capture());

    assertEquals("The Konnektor responded with a warning! Warning:", arg1Capture.getValue());
    assertEquals(statusWarning.getError(), arg2Capture.getValue());
  }

  @Test
  void logWarningIfPresentTest() {
    var msg = "This is a message";
    var arg1Capture = ArgumentCaptor.forClass(Object.class);
    var arg2Capture = ArgumentCaptor.forClass(Object.class);

    assertDoesNotThrow(() -> KonnektorUtils.logWarningIfPresent(logger, statusWarning, msg));

    verify(logger).warn(anyString(), arg1Capture.capture(), arg2Capture.capture());

    assertEquals(msg + ": ", arg1Capture.getValue());
    assertEquals(statusWarning.getError(), arg2Capture.getValue());
  }

  @Test
  void logWarningIfPresentResultOkTest() {
    assertDoesNotThrow(
        () -> KonnektorUtils.logWarningIfPresent(logger, new Status().withResult("OK")));

    verify(logger, never()).warn(anyString(), any(), any());
  }
}
