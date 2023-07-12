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

package de.gematik.epa.unit.util;

import telematik.ws.conn.phrs.phrservice.xsd.v2_0.ContextHeader;

public class PhrServiceAnswer<S, T> extends KonnektorInterfaceAnswer<S, T> {

  @Override
  public S getRequest() {
    return invocation().getArgument(1);
  }

  public ContextHeader getContextHeader() {
    return invocation().getArgument(0);
  }
}
