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

import de.gematik.epa.konnektor.KonnektorContextProvider;
import de.gematik.epa.konnektor.KonnektorInterfaceAssembly;
import de.gematik.epa.utils.internal.Synchronizer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public abstract class KonnektorServiceClient implements AutoCloseable {

  protected final KonnektorContextProvider konnektorContextProvider;

  protected final KonnektorInterfaceAssembly konnektorInterfaceAssembly;

  private final Synchronizer synchronizer;

  protected KonnektorServiceClient(
      KonnektorContextProvider konnektorContextProvider,
      KonnektorInterfaceAssembly konnektorInterfaceAssembly) {
    this.konnektorContextProvider = konnektorContextProvider;
    this.konnektorInterfaceAssembly = konnektorInterfaceAssembly;
    synchronizer =
        konnektorContextProvider.konnektorConfigurationProvider().configurationChangeSynchronizer();
  }

  protected abstract void initialize();

  protected final void runInitializationSynchronized() {
    synchronizer.runNonBlocking(this::initialize);
  }

  public final void runOperation(Runnable operation) {
    synchronizer.runNonBlocking(
        () -> {
          initialize();
          operation.run();
        });
  }

  public final <T> T runOperation(Supplier<T> operation) {
    final AtomicReference<T> response = new AtomicReference<>();
    runOperation(() -> response.set(operation.get()));
    return response.get();
  }

  @Override
  public void close() {
    konnektorContextProvider.removeContextHeader();
  }
}
