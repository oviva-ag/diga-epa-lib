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

import de.gematik.epa.config.AddressConfig;
import de.gematik.epa.config.BasicAuthenticationConfig;
import de.gematik.epa.config.Context;
import de.gematik.epa.config.FileInfo;
import de.gematik.epa.config.ProxyAddressConfig;
import de.gematik.epa.config.TlsConfig;
import de.gematik.epa.konnektor.cxf.KonnektorInterfacesCxfImpl;
import de.gematik.epa.konnektor.cxf.interceptors.HomeCommunityBlockOutInterceptor;
import de.gematik.epa.ps.konnektor.config.KonnektorConfigurationData;
import de.gematik.epa.ps.konnektor.config.KonnektorConnectionConfigurationData;
import java.math.BigInteger;
import java.util.List;
import javax.xml.namespace.QName;
import lombok.experimental.UtilityClass;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessageInfo.Type;
import org.apache.cxf.service.model.ServiceInfo;
import telematik.ws.conn.cardservice.xsd.v8_1.CardInfoType;
import telematik.ws.conn.cardservice.xsd.v8_1.Cards;
import telematik.ws.conn.cardservice.xsd.v8_1.GetPinStatusResponse;
import telematik.ws.conn.cardservice.xsd.v8_1.PinStatusEnum;
import telematik.ws.conn.cardservicecommon.xsd.v2_0.CardTypeType;
import telematik.ws.conn.connectorcommon.xsd.v5_0.Status;
import telematik.ws.conn.eventservice.xsd.v6_1.GetCardsResponse;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.GetHomeCommunityIDResponse;

@UtilityClass
public class AppTestDataFactory {

  public static final String MANDANT_ID = "Test_Mandant";
  public static final String CLIENTSYSTEM_ID = "Test_Clientsytem";
  public static final String WORKPLACE_ID = "Test_Workplace";
  public static final String USER_ID = "Test_User";
  public static final String STATUS_RESULT_OK = "OK";
  public static final String KVNR = "X123456789";
  public static final String HOME_COMMUNITY_ID = "1.2.3.4.5.67";

  public static KonnektorConfigurationData createKonnektorConfiguration() {
    return new KonnektorConfigurationData()
        .connection(createKonnektorConnectionConfiguration())
        .context(createKonnektorContext());
  }

  public static Context createKonnektorContext() {
    return new Context(MANDANT_ID, CLIENTSYSTEM_ID, WORKPLACE_ID, USER_ID);
  }

  public static KonnektorConnectionConfigurationData createKonnektorConnectionConfiguration() {
    return new KonnektorConnectionConfigurationData()
        .address(createAddress())
        .tlsConfig(createTlsConfig())
        .proxyAddress(createProxyAddressConfig())
        .basicAuthentication(createBasicAuthenticationData());
  }

  public static AddressConfig createAddress() {
    return new AddressConfig(
        "localhost",
        Integer.parseInt(AddressConfig.DEFAULT_PORT),
        KonnektorInterfacesCxfImpl.HTTPS_PROTOCOL,
        "services");
  }

  public static TlsConfig createTlsConfig() {
    return new TlsConfig(
        new FileInfo("test.p12"),
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

  public static CardInfoType cardInfoSmb() {
    var cardInfo = new CardInfoType();
    cardInfo.setCardType(CardTypeType.SMC_B);
    cardInfo.setCardVersion(new CardInfoType.CardVersion());
    cardInfo.setCardHandle("SMB123");
    cardInfo.setCardHolderName("Arztpraxis Unit Test Olé");
    cardInfo.setIccsn("80271282350235250218");
    cardInfo.setCtId("CT1");
    cardInfo.setSlotId(BigInteger.valueOf(1));

    return cardInfo;
  }

  public static GetCardsResponse getCardsSmbResponse() {
    var getCardsResponse = new GetCardsResponse();
    getCardsResponse.setStatus(getStatusOk());
    getCardsResponse.setCards(new Cards());
    getCardsResponse.getCards().getCard().add(cardInfoSmb());

    return getCardsResponse;
  }

  public static GetPinStatusResponse getPinStatusResponse(PinStatusEnum pinStatus) {
    BigInteger leftTries = new BigInteger("3");
    return new GetPinStatusResponse()
        .withStatus(getStatusOk())
        .withPinStatus(pinStatus)
        .withLeftTries(leftTries);
  }
}
