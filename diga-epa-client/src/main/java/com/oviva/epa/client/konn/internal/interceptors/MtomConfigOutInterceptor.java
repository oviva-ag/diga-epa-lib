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

package com.oviva.epa.client.konn.internal.interceptors;

import static org.apache.cxf.message.Message.*;

import java.util.Optional;
import javax.xml.namespace.QName;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.AbstractMessageContainer;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.MessageInfo;

public class MtomConfigOutInterceptor extends AbstractPhaseInterceptor<Message> {

  public static final String PROV_AND_REG_OPERATION_NAME =
      HomeCommunityBlockOutInterceptor.PROV_AND_REG_OPERATION_NAME;

  public static final String RETRIEVE_DOCUMENT_OPERATION_NAME =
      "DocumentRepository_RetrieveDocumentSet";

  public MtomConfigOutInterceptor() {
    this(Phase.PRE_STREAM);
  }

  public MtomConfigOutInterceptor(String phase) {
    super(phase);
  }

  @Override
  public void handleMessage(Message message) throws Fault {

    var operationQName =
        Optional.ofNullable(message.get(WSDL_OPERATION))
            .filter(QName.class::isInstance)
            .map(QName.class::cast)
            .or(
                () ->
                    Optional.ofNullable(message.get(BindingMessageInfo.class))
                        .map(BindingMessageInfo::getMessageInfo)
                        .or(() -> Optional.ofNullable(message.get(MessageInfo.class)))
                        .map(AbstractMessageContainer::getName))
            .orElse(new QName("unknown"));

    switch (operationQName.getLocalPart()) {
      case PROV_AND_REG_OPERATION_NAME, RETRIEVE_DOCUMENT_OPERATION_NAME ->
          message.put(MTOM_ENABLED, true);
      default -> message.put(MTOM_ENABLED, false);
    }
  }
}
