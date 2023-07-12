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

package de.gematik.epa.konnektor;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Class to provide the client implementations of the Konnektor Webservices collected in an {@link
 * KonnektorInterfaceAssembly} object.<br>
 * An application using this library, must create said implementations and provide them to an
 * instance of this class, either directly or via an implementation of the {@link
 * KonnektorInterfaceProviderCallback} interface and such make them available to this library.
 */
@NoArgsConstructor
@Accessors(fluent = true)
public class KonnektorInterfaceProvider {

  @Getter(lazy = true)
  private static final KonnektorInterfaceProvider defaultInstance =
      new KonnektorInterfaceProvider();

  private final AtomicReference<KonnektorInterfaceAssembly> konnektorInterfaceAssembly =
      new AtomicReference<>();

  private final AtomicReference<KonnektorInterfaceProviderCallback>
      konnektorInterfaceProviderCallback = new AtomicReference<>();

  /**
   * Directly set the {@link KonnektorInterfaceAssembly} implementation, containing all the client
   * implementations of the Konnektor Webservices.
   *
   * @param interfaceAssembly KonnektorInterfaceAssembly holding the Konnektor service client
   *     implementations
   */
  public void setKonnektorInterfaceAssembly(KonnektorInterfaceAssembly interfaceAssembly) {
    konnektorInterfaceAssembly.set(interfaceAssembly);
  }

  /**
   * Get the {@link KonnektorInterfaceAssembly} implementation stored in this provider and if none
   * is stored, try to retrieve it using the stored {@link KonnektorInterfaceProviderCallback}
   *
   * @return {@link KonnektorInterfaceAssembly} or {@code null}, if none is present or can be
   *     retrieved via callback
   */
  public KonnektorInterfaceAssembly getKonnektorInterfaceAssembly() {
    return Optional.ofNullable(konnektorInterfaceAssembly.get())
        .orElseGet(this::retrieveKonnektorInterfaceAssembly);
  }

  /**
   * Retrieve a {@link KonnektorInterfaceAssembly} implementation using the stored callback and if
   * none is stored, or it does return null, return the {@link KonnektorInterfaceAssembly} stored in
   * this provider.
   *
   * @return {@link KonnektorInterfaceAssembly} or {@code null}, if none can be retrieved
   */
  public KonnektorInterfaceAssembly retrieveKonnektorInterfaceAssembly() {
    return konnektorInterfaceAssembly.updateAndGet(
        oldIA ->
            Optional.ofNullable(konnektorInterfaceProviderCallback.get())
                .map(KonnektorInterfaceProviderCallback::provideInterfaceAssembly)
                .orElse(oldIA));
  }

  public void setKonnektorInterfaceProviderCallback(
      KonnektorInterfaceProviderCallback providerCallback) {
    konnektorInterfaceProviderCallback.set(providerCallback);
  }

  /**
   * Interface for the creation of a class, which can provide an {@link KonnektorInterfaceAssembly}
   * implementation. To be used it must be set at the provider using the {@link
   * #setKonnektorInterfaceProviderCallback(KonnektorInterfaceProviderCallback)} method.
   */
  public interface KonnektorInterfaceProviderCallback {
    KonnektorInterfaceAssembly provideInterfaceAssembly();
  }
}
