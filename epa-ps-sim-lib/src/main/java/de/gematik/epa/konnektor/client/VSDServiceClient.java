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

import de.gematik.epa.dto.response.ReadVSDResponseDTO;
import de.gematik.epa.konnektor.KonnektorContextProvider;
import de.gematik.epa.konnektor.KonnektorInterfaceAssembly;
import de.gematik.epa.konnektor.conversion.VSDServiceUtils;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Objects;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import telematik.ws.conn.cardservice.xsd.v8_1.CardInfoType;
import telematik.ws.conn.cardservicecommon.xsd.v2_0.CardTypeType;
import telematik.ws.conn.connectorcontext.xsd.v2_0.ContextType;
import telematik.ws.conn.vsds.vsdservice.wsdl.v5_2.VSDServicePortType;
import telematik.ws.conn.vsds.vsdservice.xsd.v5_2.ReadVSD;
import telematik.ws.conn.vsds.vsdservice.xsd.v5_2.ReadVSDResponse;

@Accessors(fluent = true)
@Slf4j
@Getter
public class VSDServiceClient extends KonnektorServiceClient {
  private VSDServicePortType vsdService;
  private ContextType context;
  private EventServiceClient eventService;
  private final String kvnr;

  public VSDServiceClient(
      KonnektorContextProvider konnektorContextProvider,
      KonnektorInterfaceAssembly konnektorInterfaceAssembly,
      String kvnr) {
    super(konnektorContextProvider, konnektorInterfaceAssembly);
    this.kvnr = kvnr;
    runInitializationSynchronized();
  }

  @Override
  protected void initialize() {
    context = konnektorContextProvider.getContext();
    vsdService = konnektorInterfaceAssembly.vsdService();
    eventService = new EventServiceClient(konnektorContextProvider, konnektorInterfaceAssembly);
  }

  public ReadVSD transformRequest(String kvnr) {
    return new ReadVSD()
        .withContext(context)
        .withEhcHandle(getEhcHandle(kvnr))
        .withHpcHandle(getHcpHandle())
        .withReadOnlineReceipt(true)
        .withPerformOnlineCheck(true);
  }

  public ReadVSDResponseDTO transformResponse(ReadVSDResponse response) throws IOException {
    int result = VSDServiceUtils.getResultOfOnlineCheckEGK(response);
    boolean isSuccess = VSDServiceUtils.isResultSuccessful(result);
    String message = isSuccess ? "ReadVSD operation was successful" : "ReadVSD operation failed";
    return new ReadVSDResponseDTO(isSuccess, message, result);
  }

  public ReadVSDResponse readVSD(@NonNull ReadVSD request) {
    return vsdService.readVSD(request);
  }

  // region private
  private String getHcpHandle() {
    return eventService.getCardHandle(CardTypeType.SMC_B);
  }

  private String getEhcHandle(String kvnr) {
    CardInfoType cardInfoType = retrieveCardInfo(kvnr);
    return cardInfoType.getCardHandle();
  }

  private CardInfoType retrieveCardInfo(String kvnr) throws NoSuchElementException {
    try (EventServiceClient eventServiceClient =
        new EventServiceClient(konnektorContextProvider, konnektorInterfaceAssembly)) {
      return Objects.requireNonNull(
          eventServiceClient.getEgkInfoToKvnr(kvnr),
          "No egkInfo could be retrieved for KVNR " + Objects.toString(kvnr(), "null"));
    } catch (Exception e) {
      throw new NoSuchElementException(e.getMessage());
    }
  }

  // endregion private

}
