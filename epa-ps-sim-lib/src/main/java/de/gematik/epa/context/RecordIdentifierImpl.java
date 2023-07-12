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

package de.gematik.epa.context;

import de.gematik.epa.ihe.model.simple.RecordIdentifier;
import java.util.Objects;
import telematik.ws.fd.phr.phrcommon.xsd.v1_1.InsurantIdType;
import telematik.ws.fd.phr.phrcommon.xsd.v1_1.RecordIdentifierType;

public class RecordIdentifierImpl extends RecordIdentifierType implements RecordIdentifier {

  @Override
  public InsurantIdImpl getInsurantId() {
    return InsurantIdImpl.of(super.getInsurantId());
  }

  @Override
  public void setInsurantId(InsurantIdType value) {
    super.setInsurantId(InsurantIdImpl.of(value));
  }

  public static RecordIdentifierImpl of(RecordIdentifierType recordIdentifier) {
    if (Objects.isNull(recordIdentifier)) return null;
    if (recordIdentifier instanceof RecordIdentifierImpl recordIdentifierImpl) {
      return recordIdentifierImpl;
    } else {
      return (RecordIdentifierImpl)
          new RecordIdentifierImpl()
              .withHomeCommunityId(recordIdentifier.getHomeCommunityId())
              .withInsurantId(recordIdentifier.getInsurantId());
    }
  }
}
