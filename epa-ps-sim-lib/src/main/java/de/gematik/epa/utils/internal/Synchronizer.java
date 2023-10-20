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

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.SneakyThrows;

public class Synchronizer {

  private final ReentrantLock lock = new ReentrantLock();

  private final CountLatch nonBlockingCount = new CountLatch();

  @SneakyThrows
  public void runBlocking(Runnable code) {
    try {
      lock.lock();
      nonBlockingCount.await();
      code.run();
    } finally {
      lock.unlock();
    }
  }

  @SneakyThrows
  public void runNonBlocking(Runnable code) {
    try {
      nonBlockingCount.countUp();
      if (lock.isLocked()) {
        nonBlockingCount.countDown();
        lock.lock();
        nonBlockingCount.countUp();
        lock.unlock();
      }
      code.run();
    } finally {
      nonBlockingCount.countDown();
    }
  }

  static class CountLatch {

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private int count = 0;

    public void countDown() {
      lock.lock();
      try {
        if (count > 0) {
          count--;
          if (count == 0) {
            condition.signalAll();
          }
        }
      } finally {
        lock.unlock();
      }
    }

    public void countUp() {
      lock.lock();
      try {
        count++;
      } finally {
        lock.unlock();
      }
    }

    public void await() throws InterruptedException {
      lock.lock();
      try {
        while (count > 0) {
          condition.await();
        }
      } finally {
        lock.unlock();
      }
    }
  }
}
