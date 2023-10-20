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

import de.gematik.epa.config.Context;
import de.gematik.epa.context.ContextHeaderAdapter;
import de.gematik.epa.context.ContextHeaderBuilder;
import de.gematik.epa.context.RecordIdentifierImpl;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import telematik.ws.conn.connectorcontext.xsd.v2_0.ContextType;
import telematik.ws.conn.phrs.phrmanagementservice.wsdl.v2_5.PHRManagementServicePortType;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.GetHomeCommunityID;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.ObjectFactory;
import telematik.ws.conn.phrs.phrservice.xsd.v2_0.ContextHeader;
import telematik.ws.fd.phr.phrcommon.xsd.v1_1.InsurantIdType;

/**
 * Class to provide the Konnektor ContextHeader for client requests of Konnektor operations.<br>
 * The requests for Konnektor operations triggered by this library will get the Konnektor {@link
 * telematik.ws.conn.connectorcontext.xsd.v2_0.ContextType}, {@link
 * telematik.ws.fd.phr.phrcommon.xsd.v1_1.RecordIdentifierType} or the combination of both as {@link
 * ContextHeader} from an instance of this provider.<br>
 * A using application must supply a {@link KonnektorConfigurationProvider}.<br>
 * ContextHeaders are created and cached in a ThreadLocal field, thus being multi threading capable.
 * For the creation of the {@link ContextHeader} the KVNR must be supplied, which should be included
 * in the clients request. The HomeCommunityID is retrieved using the {@link
 * PHRManagementServicePortType#getHomeCommunityID(GetHomeCommunityID)} operation! Thus, this class
 * depends on the {@link KonnektorInterfaceAssembly} and can't be used without the later being
 * implemented! Once the HomeCommunityId for a KVNR has been retrieved, it is stored in a Map,
 * making sure that during runtime, for each KVNR the getHomeCommunityID operation is only called
 * once (There were load issues in the TI, for which too many getHomeCommunity calls were partly
 * responsible).
 */
@RequiredArgsConstructor
@Accessors(fluent = true)
@Data
public class KonnektorContextProvider {

  @Getter(lazy = true)
  private static final String defaultRootId = new InsurantIdType().getRoot();

  private final KonnektorConfigurationProvider konnektorConfigurationProvider;

  private final KonnektorInterfaceAssembly konnektorInterfaceAssembly;

  @Getter(AccessLevel.PROTECTED)
  private final ThreadLocal<ContextHeaderAdapter> contextHeader = new ThreadLocal<>();

  @Getter(AccessLevel.PROTECTED)
  private final Map<String, String> hcidCache = new ConcurrentHashMap<>();

  /**
   * Create the {@link ContextHeader} for the given KVNR, using the configured Konnektor context.
   * For all KVNR it is assumed, that the belonging root id is the same as retrieved by the call
   * {@code new InsurantIdType().getRoot()}. So far no exception to this rule has occurred, once it
   * does, the code must be changed such, that the root id is also provided by the caller.
   *
   * @param kvnr KVNR which was transmitted in the request of the called operation
   * @return the ContextHeader just created. Can always be retrieved again by calling {@link
   *     #getContextHeader()}
   */
  public ContextHeaderAdapter createContextHeader(@NonNull String kvnr) {
    var contextHeaderBuilder =
        new ContextHeaderBuilder()
            .mandantId(konnektorContext().mandantId())
            .clientSystemId(konnektorContext().clientSystemId())
            .workplaceId(konnektorContext().workplaceId())
            .userId(konnektorContext().userId())
            .extension(kvnr)
            .root(defaultRootId());

    contextHeaderBuilder.homeCommunity(getHcid(contextHeaderBuilder));

    contextHeader.set(contextHeaderBuilder.buildContextHeader());
    return getContextHeader();
  }

  /**
   * Get the ContextHeader for the current operation.<br>
   * Only works after {@link #createContextHeader(String)} was called and before {@link
   * #removeContextHeader()} was called.
   *
   * @return the ContextHeader, which was created ({@link #createContextHeader(String)}) on the
   *     current thread
   */
  public ContextHeaderAdapter getContextHeader() {
    return contextHeader.get();
  }

  public ContextType getContext() {
    return Optional.ofNullable(getContextHeader())
        .map(ContextHeader::getContext)
        .or(
            () ->
                Optional.ofNullable(konnektorContext())
                    .map(
                        konCtx ->
                            new ContextHeaderBuilder()
                                .workplaceId(konCtx.workplaceId())
                                .clientSystemId(konCtx.clientSystemId())
                                .mandantId(konCtx.mandantId())
                                .userId(konCtx.userId())
                                .buildContext()))
        .orElseThrow(
            () ->
                new MissingResourceException(
                    "No context has been set at the ContextProvider",
                    this.getClass().getSimpleName(),
                    "konnektorContext"));
  }

  public RecordIdentifierImpl getRecordIdentifier() {
    return Optional.ofNullable(getContextHeader())
        .map(ContextHeaderAdapter::getRecordIdentifier)
        .orElseThrow(
            () ->
                new MissingResourceException(
                    "No contextHeader has been created at the ContextProvider",
                    this.getClass().getSimpleName(),
                    "contextHeader"));
  }

  /**
   * Remove the ContextHeader of the current operation from the cache. Purpose of this function is
   * to prevent the cache from swelling up with old data over time. So it is not strictly necessary
   * for the functionality, but rather for reducing memory usage and to prevent decreasing
   * performance during runtime.
   */
  public void removeContextHeader() {
    contextHeader.remove();
  }

  @SneakyThrows
  private String getHcid(ContextHeaderBuilder contextHeaderBuilder) {
    return hcidCache.computeIfAbsent(
        contextHeaderBuilder.extension(),
        absentKvnrIdentifier -> {
          var getHomeCommunityIDRequest =
              new ObjectFactory()
                  .createGetHomeCommunityID()
                  .withInsurantID(contextHeaderBuilder.buildInsurantId())
                  .withContext(contextHeaderBuilder.buildContext());
          return konnektorInterfaceAssembly
              .phrManagementService()
              .getHomeCommunityID(getHomeCommunityIDRequest)
              .getHomeCommunityID();
        });
  }

  private Context konnektorContext() {
    return konnektorConfigurationProvider.context();
  }
}
