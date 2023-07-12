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

import java.util.function.Supplier;
import lombok.SneakyThrows;

/**
 * In the middle of your Optional magic, you get an IDE complaint, because the {@link Supplier} in
 * your or statement uses a function with a throw clause? Use this Supplier instead, works just the
 * same, except for an ugly cast expression.
 *
 * @param <T> type of the result supplied by this supplier
 */
@FunctionalInterface
public interface ThrowingSupplier<T> extends Supplier<T> {

  @SuppressWarnings("java:S112")
  T supply() throws Throwable;

  @Override
  @SneakyThrows
  default T get() {
    return supply();
  }
}
