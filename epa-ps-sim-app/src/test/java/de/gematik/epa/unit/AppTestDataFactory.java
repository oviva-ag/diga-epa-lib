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

package de.gematik.epa.unit;

import static org.apache.cxf.message.Message.*;

import de.gematik.epa.data.Context;
import de.gematik.epa.ps.konnektor.config.AddressConfig;
import de.gematik.epa.ps.konnektor.config.BasicAuthenticationConfig;
import de.gematik.epa.ps.konnektor.config.KonnektorConfiguration;
import de.gematik.epa.ps.konnektor.config.KonnektorConnectionConfiguration;
import de.gematik.epa.ps.konnektor.config.ProxyAddressConfig;
import de.gematik.epa.ps.konnektor.config.TlsConfig;
import de.gematik.epa.ps.konnektor.impl.KonnektorInterfaceImplGenerator;
import de.gematik.epa.ps.konnektor.interceptors.HomeCommunityBlockOutInterceptor;
import java.util.List;
import javax.xml.namespace.QName;
import lombok.experimental.UtilityClass;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessageInfo.Type;
import org.apache.cxf.service.model.ServiceInfo;
import telematik.ws.conn.connectorcommon.xsd.v5_0.Status;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_0.GetHomeCommunityIDResponse;

@UtilityClass
public class AppTestDataFactory {

  public static final String MANDANT_ID = "Test_Mandant";
  public static final String CLIENTSYSTEM_ID = "Test_Clientsytem";
  public static final String WORKPLACE_ID = "Test_Workplace";
  public static final String USER_ID = "Test_User";
  public static final String STATUS_RESULT_OK = "OK";
  public static final String KVNR = "X123456789";
  public static final String HOME_COMMUNITY_ID = "1.2.3.4.5.67";

  public static KonnektorConfiguration createKonnektorConfiguration() {
    return new KonnektorConfiguration(
        createKonnektorConnectionConfiguration(), createKonnektorContext());
  }

  public static Context createKonnektorContext() {
    return new Context(MANDANT_ID, CLIENTSYSTEM_ID, WORKPLACE_ID, USER_ID);
  }

  public static KonnektorConnectionConfiguration createKonnektorConnectionConfiguration() {
    return new KonnektorConnectionConfiguration(
        createAddress(),
        createTlsConfig(),
        createProxyAddressConfig(),
        createBasicAuthenticationData());
  }

  public static AddressConfig createAddress() {
    return new AddressConfig(
        "localhost",
        Integer.parseInt(AddressConfig.DEFAULT_PORT),
        KonnektorInterfaceImplGenerator.HTTPS_PROTOCOL,
        "services");
  }

  public static TlsConfig createTlsConfig() {
    return new TlsConfig(
        "test.p12",
        "test1234",
        "PKCS12",
        List.of("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", "TLS_DHE_RSA_WITH_AES_256_CBC_SHA"));
  }

  public static ProxyAddressConfig createProxyAddressConfig() {
    return new ProxyAddressConfig(
        "localhost", Integer.parseInt(ProxyAddressConfig.DEFAULT_PORT), true);
  }

  public static BasicAuthenticationConfig createBasicAuthenticationData() {
    return new BasicAuthenticationConfig("root", "root", true);
  }

  public static SoapMessage createCxfSoapMessage() {
    var soapMessage = new SoapMessage(Soap12.getInstance());
    var interfaceInfo =
        new InterfaceInfo(
            new ServiceInfo(), new QName(HomeCommunityBlockOutInterceptor.PHR_SERVICE_PORT_NAME));
    var operationInfo =
        interfaceInfo.addOperation(
            new QName(HomeCommunityBlockOutInterceptor.PROV_AND_REG_OPERATION_NAME));
    var msgInfo = new MessageInfo(operationInfo, Type.OUTPUT, operationInfo.getName());
    soapMessage.put(MessageInfo.class, msgInfo);
    soapMessage.put(REQUESTOR_ROLE, Boolean.TRUE);
    return soapMessage;
  }

  public static Status getStatusOk() {
    var status = new Status();
    status.setResult(STATUS_RESULT_OK);

    return status;
  }

  public static GetHomeCommunityIDResponse createGetHomeCommunityResponse() {
    var getHomeCommunityResponse = new GetHomeCommunityIDResponse();
    getHomeCommunityResponse.setStatus(getStatusOk());
    getHomeCommunityResponse.setHomeCommunityID(HOME_COMMUNITY_ID);
    return getHomeCommunityResponse;
  }
}
