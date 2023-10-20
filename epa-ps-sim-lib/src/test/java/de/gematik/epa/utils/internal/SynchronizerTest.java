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

package de.gematik.epa.utils.internal;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.epa.utils.internal.SynchronizerTest.LogEntry.Phase;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

class SynchronizerTest {

  public static final String BLOCKING_THREAD_NAME_SUFFIX = " BLOCKING";

  private final Synchronizer synchronizer = new Synchronizer();

  private final List<LogEntry> log = new ArrayList<>();

  @Test
  void runTest() {

    var threadNB1 = new Thread(nonBlockingThreadRunnable);

    var threadNB2 = new Thread(nonBlockingThreadRunnable);

    var threadNB3 = new Thread(nonBlockingThreadRunnable);

    var threadB = new Thread(blockingThreadRunnable);

    var threadName1 = threadNB1.getName();
    var threadName2 = threadNB2.getName();
    var threadName3 = threadNB3.getName();
    var threadNameB = threadB.getName() + BLOCKING_THREAD_NAME_SUFFIX;

    try {
      assertDoesNotThrow(threadNB1::start);
      Thread.sleep(333);

      assertDoesNotThrow(threadNB2::start);
      Thread.sleep(333);

      assertDoesNotThrow(threadB::start);
      Thread.sleep(333);

      assertDoesNotThrow(threadNB3::start);
    } catch (InterruptedException ignored) {
    }

    try {
      threadNB1.join(20000);
      threadNB2.join(15000);
      threadB.join(10000);
      threadNB3.join(5000);
    } catch (InterruptedException e) {
      fail("Unexpected InterruptedException caught", e);
    }

    var logEntryOneIsDone = find(Phase.DONE, threadName1);
    assertBefore(find(Phase.RUNNING, threadName2), logEntryOneIsDone);
    assertBefore(find(Phase.CREATED, threadNameB), logEntryOneIsDone);
    assertBefore(find(Phase.CREATED, threadName3), logEntryOneIsDone);
    assertBefore(logEntryOneIsDone, find(Phase.RUNNING, threadNameB));
    assertBefore(logEntryOneIsDone, find(Phase.RUNNING, threadName3));
    assertBefore(find(Phase.DONE, threadNameB), find(Phase.RUNNING, threadName3));
  }

  private final Runnable nonBlockingThreadRunnable =
      () -> {
        log(Phase.CREATED);
        synchronizer.runNonBlocking(runnable(null));
      };

  private final Runnable blockingThreadRunnable =
      () -> {
        log(Phase.CREATED, BLOCKING_THREAD_NAME_SUFFIX);
        synchronizer.runBlocking(runnable(BLOCKING_THREAD_NAME_SUFFIX));
      };

  private Runnable runnable(String threadNamePart) {
    return new Runnable() {
      @SneakyThrows
      @Override
      public void run() {
        log(Phase.RUNNING, threadNamePart);
        Thread.sleep(2000);
        log(Phase.DONE, threadNamePart);
      }
    };
  }

  private void log(Phase phase, String threadNamePart) {
    log.add(
        LogEntry.of()
            .phase(phase)
            .threadName(
                Thread.currentThread().getName() + Objects.requireNonNullElse(threadNamePart, ""))
            .timestamp(LocalDateTime.now())
            .position(log.size()));
  }

  private void log(Phase phase) {
    log(phase, null);
  }

  private LogEntry find(Phase phase, String threadName) {
    return log.stream()
        .filter(le -> le.phase().equals(phase))
        .filter(le -> le.threadName().equals(threadName))
        .findFirst()
        .orElse(null);
  }

  private void assertBefore(LogEntry first, LogEntry second) {
    assertTrue(first.position() < second.position());
  }

  @Data
  @Accessors(fluent = true)
  static class LogEntry {
    private LocalDateTime timestamp;
    private String threadName;
    private Phase phase;
    private int position;

    enum Phase {
      CREATED,
      RUNNING,
      DONE
    }

    public static LogEntry of() {
      return new LogEntry();
    }
  }
}
