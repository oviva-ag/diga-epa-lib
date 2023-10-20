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

import de.gematik.epa.dto.request.SignDocumentRequest;
import de.gematik.epa.dto.request.SignDocumentRequest.SignatureType;
import de.gematik.epa.dto.response.SignDocumentResponse.SignatureForm;
import de.gematik.epa.ihe.model.simple.ByteArray;
import de.gematik.epa.konnektor.KonnektorContextProvider;
import de.gematik.epa.konnektor.KonnektorInterfaceAssembly;
import de.gematik.epa.konnektor.KonnektorUtils;
import de.gematik.epa.utils.XmlUtils;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.experimental.Accessors;
import oasis.names.tc.dss._1_0.core.schema.Base64Data;
import oasis.names.tc.dss._1_0.core.schema.Base64Signature;
import telematik.ws.conn.cardservicecommon.xsd.v2_0.CardTypeType;
import telematik.ws.conn.connectorcontext.xsd.v2_0.ContextType;
import telematik.ws.conn.signatureservice.wsdl.v7_5.SignatureServicePortType;
import telematik.ws.conn.signatureservice.xsd.v7_5.DocumentType;
import telematik.ws.conn.signatureservice.xsd.v7_5.GetJobNumber;
import telematik.ws.conn.signatureservice.xsd.v7_5.SignDocument;
import telematik.ws.conn.signatureservice.xsd.v7_5.SignDocumentResponse;
import telematik.ws.conn.signatureservice.xsd.v7_5.SignRequest;
import telematik.ws.conn.signatureservice.xsd.v7_5.SignRequest.OptionalInputs;
import telematik.ws.conn.signatureservice.xsd.v7_5.SignResponse;

@Accessors(fluent = true)
public class SignatureServiceClient extends KonnektorServiceClient {

  public static final String TV_MODE_DEFAULT = "NONE";

  private SignatureServicePortType signatureService;

  private EventServiceClient eventServiceClient;

  private ContextType context;

  public SignatureServiceClient(
      KonnektorContextProvider konnektorContextProvider,
      KonnektorInterfaceAssembly konnektorInterfaceAssembly) {
    super(konnektorContextProvider, konnektorInterfaceAssembly);
    runInitializationSynchronized();
  }

  @Override
  protected void initialize() {
    context = konnektorContextProvider.getContext();
    signatureService = konnektorInterfaceAssembly.signatureService();
    eventServiceClient =
        new EventServiceClient(konnektorContextProvider, konnektorInterfaceAssembly);
  }

  public SignDocument transformRequest(SignDocumentRequest request) {
    var cardHandle =
        eventServiceClient.getCardHandle(
            request.performQES() ? CardTypeType.HB_AX : CardTypeType.SM_B);
    var jobNumber =
        signatureService.getJobNumber(new GetJobNumber().withContext(context)).getJobNumber();

    return new SignDocument()
        .withTvMode(TV_MODE_DEFAULT)
        .withContext(context)
        .withCardHandle(cardHandle)
        .withJobNumber(jobNumber)
        .withCrypt(request.signatureAlgorithm().name())
        .withSignRequest(buildSignRequest(request.document(), request.signatureType()));
  }

  public de.gematik.epa.dto.response.SignDocumentResponse transformResponse(
      SignDocumentResponse konResponse) {
    return Optional.ofNullable(konResponse).map(SignDocumentResponse::getSignResponse).stream()
        .flatMap(Collection::stream)
        .findFirst()
        .map(
            sr ->
                new de.gematik.epa.dto.response.SignDocumentResponse(
                    KonnektorUtils.fromStatus(sr.getStatus()),
                    getSignatureObject(sr),
                    Objects.nonNull(sr.getSignatureObject().getSignaturePtr())
                        ? SignatureForm.DOCUMENT_WITH_SIGNATURE
                        : SignatureForm.SIGNATURE))
        .orElse(null);
  }

  public SignDocumentResponse signDocument(@NonNull SignDocument request) {
    return signatureService.signDocument(request);
  }

  private SignRequest buildSignRequest(ByteArray document, @NonNull SignatureType signatureType) {
    return Optional.ofNullable(document)
        .map(ByteArray::value)
        .map(v -> new Base64Data().withMimeType("text/plain; charset=utf-8").withValue(v))
        .map(b64d -> new DocumentType().withBase64Data(b64d).withShortText("Sign document"))
        .map(
            dt ->
                new SignRequest()
                    .withDocument(dt)
                    .withRequestID(UUID.randomUUID().toString())
                    .withOptionalInputs(
                        new OptionalInputs()
                            .withIncludeEContent(true)
                            .withSignatureType(signatureType.uri().toString())))
        .orElse(null);
  }

  private ByteArray getSignatureObject(SignResponse signResponse) {
    return Optional.ofNullable(signResponse.getSignatureObject().getSignature())
        .map(XmlUtils::marshal)
        .or(
            () ->
                Optional.ofNullable(signResponse.getSignatureObject().getBase64Signature())
                    .map(Base64Signature::getValue))
        .or(
            () ->
                Optional.ofNullable(signResponse.getOptionalOutputs().getDocumentWithSignature())
                    .map(telematik.ws.conn.connectorcommon.xsd.v5_0.DocumentType::getBase64XML))
        .or(
            () ->
                Optional.ofNullable(signResponse.getOptionalOutputs().getDocumentWithSignature())
                    .map(docWithSig -> docWithSig.getBase64Data().getValue()))
        .map(ByteArray::of)
        .orElse(null);
  }
}
