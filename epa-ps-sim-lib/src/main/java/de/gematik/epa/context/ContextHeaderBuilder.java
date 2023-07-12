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

import lombok.Data;
import lombok.experimental.Accessors;
import telematik.ws.conn.connectorcontext.xsd.v2_0.ContextType;
import telematik.ws.conn.phrs.phrservice.xsd.v2_0.ContextHeader;

/**
 * Builder to build the {@link ContextHeader} and its elements. Thus, it can also be used to build
 * the {@link ContextType} for basic Konnektor operations.<br>
 * The field extension holds, what in ePA speech is known as KVNR.
 */
@Data
@Accessors(fluent = true)
public final class ContextHeaderBuilder {

  private String mandantId;
  private String clientSystemId;
  private String workplaceId;
  private String userId;
  private String root;
  private String extension;
  private String homeCommunity;

  public ContextType buildContext() {
    var context = new ContextType();
    context.setMandantId(mandantId);
    context.setClientSystemId(clientSystemId);
    context.setWorkplaceId(workplaceId);
    context.setUserId(userId);
    return context;
  }

  public InsurantIdImpl buildInsurantId() {
    var insurantId = new InsurantIdImpl();
    insurantId.setRoot(root);
    insurantId.setExtension(extension);
    return insurantId;
  }

  public RecordIdentifierImpl buildRecordIdentifier() {
    var recordIdentifier = new RecordIdentifierImpl();
    recordIdentifier.setHomeCommunityId(homeCommunity);

    recordIdentifier.setInsurantId(buildInsurantId());

    return recordIdentifier;
  }

  public ContextHeaderAdapter buildContextHeader() {
    var contextHeader = new ContextHeaderAdapter();
    contextHeader.setContext(buildContext());
    contextHeader.setRecordIdentifier(buildRecordIdentifier());

    return contextHeader;
  }
}
