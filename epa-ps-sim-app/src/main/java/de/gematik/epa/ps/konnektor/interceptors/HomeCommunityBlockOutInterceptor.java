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

package de.gematik.epa.ps.konnektor.interceptors;

import static org.apache.cxf.message.Message.*;

import de.gematik.epa.konnektor.KonnektorContextProvider;
import ihe.iti.xdr._2014.HomeCommunityBlock;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;
import jakarta.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.xml.namespace.QName;
import lombok.SneakyThrows;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.OperationInfo;
import telematik.ws.conn.phrs.phrservice.xsd.v2_0.ContextHeader;

/**
 * This Interceptor for the SOAP messages to the Konnektor has only one purpose, which is to add the
 * homeCommunityBlock to the {@link
 * telematik.ws.conn.phrs.phrservice.wsdl.v2_0.PHRServicePortType#documentRepositoryProvideAndRegisterDocumentSetB(ContextHeader,
 * ProvideAndRegisterDocumentSetRequestType)} requests.
 */
public class HomeCommunityBlockOutInterceptor extends AbstractSoapInterceptor {

  protected static final QName HOME_COMMUNITY_BLOCK =
      new QName("urn:ihe:iti:xdr:2014", "homeCommunityBlock");
  public static final String PHR_SERVICE_PORT_NAME = "PHRService_PortType";
  public static final String PROV_AND_REG_OPERATION_NAME =
      "DocumentRepository_ProvideAndRegisterDocumentSet-b";

  private final KonnektorContextProvider contextProvider;

  public HomeCommunityBlockOutInterceptor() {
    this(KonnektorContextProvider.defaultInstance());
  }

  public HomeCommunityBlockOutInterceptor(KonnektorContextProvider contextProvider) {
    super(Phase.POST_LOGICAL);
    this.contextProvider = contextProvider;
  }

  @Override
  @SneakyThrows
  public void handleMessage(SoapMessage message) throws Fault {
    if (!isXCDRTransaction(message)) return;

    HomeCommunityBlock homeCommunityBlock = new HomeCommunityBlock();
    homeCommunityBlock.setHomeCommunityId(
        contextProvider.getContextHeader().getRecordIdentifier().getHomeCommunityId());
    JAXBElement<HomeCommunityBlock> jaxbElement =
        new JAXBElement<>(HOME_COMMUNITY_BLOCK, HomeCommunityBlock.class, homeCommunityBlock);

    var soapHeader =
        new Header(
            jaxbElement.getName(), jaxbElement, new JAXBDataBinding(jaxbElement.getDeclaredType()));

    List<Header> headers =
        Optional.ofNullable(message.get(Header.HEADER_LIST))
            .filter(value -> List.class.isAssignableFrom(value.getClass()))
            .map(value -> (List<?>) value)
            .map(
                list ->
                    list.stream().filter(Header.class::isInstance).map(Header.class::cast).toList())
            .map(ArrayList::new)
            .orElseGet(ArrayList::new);

    headers.add(soapHeader);
    message.put(Header.HEADER_LIST, headers);
  }

  // see https://www.ihe.net/uploadedFiles/Documents/ITI/IHE_ITI_Suppl_XCDR.pdf
  // 3.41.4.1.2.2 XDR Document Source Options
  private static boolean isXCDRTransaction(Message message) {
    // only for up- and downloading documents and only for requests
    try {
      MessageInfo messageInfo =
          Optional.ofNullable(message.get(MessageInfo.class))
              .orElseThrow(NullPointerException::new);

      if (Optional.ofNullable(messageInfo.getOperation())
              .map(OperationInfo::getName)
              .map(QName::getLocalPart)
              .filter(PROV_AND_REG_OPERATION_NAME::equals)
              .isPresent()
          && Optional.ofNullable(messageInfo.getOperation().getInterface())
              .map(InterfaceInfo::getName)
              .map(QName::getLocalPart)
              .filter(PHR_SERVICE_PORT_NAME::equals)
              .isPresent()) return message.get(REQUESTOR_ROLE) != null;
      return false;
    } catch (NullPointerException npe) {
      return false;
    }
  }
}
