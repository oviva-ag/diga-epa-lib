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

package com.oviva.epa.client.konn.internal;

import static jakarta.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING;
import static jakarta.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING;
import static jakarta.xml.ws.soap.SOAPBinding.SOAP12HTTP_MTOM_BINDING;

import com.oviva.epa.client.konn.KonnektorConnection;
import com.oviva.epa.client.konn.KonnektorConnectionFactory;
import com.oviva.epa.client.konn.internal.interceptors.HomeCommunityBlockOutInterceptor;
import com.oviva.epa.client.konn.internal.interceptors.MtomConfigOutInterceptor;
import com.oviva.epa.client.konn.internal.util.XmlUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.ext.logging.event.EventType;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.cxf.ext.logging.slf4j.Slf4jEventSender;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.slf4j.event.Level;
import telematik.ws.conn.SdsApi;
import telematik.ws.conn.cardservice.wsdl.v8_1.CardService;
import telematik.ws.conn.cardservice.wsdl.v8_1.CardServicePortType;
import telematik.ws.conn.certificateservice.wsdl.v6_0.CertificateService;
import telematik.ws.conn.certificateservice.wsdl.v6_0.CertificateServicePortType;
import telematik.ws.conn.eventservice.wsdl.v6_1.EventService;
import telematik.ws.conn.eventservice.wsdl.v6_1.EventServicePortType;
import telematik.ws.conn.phrs.phrmanagementservice.wsdl.v2_5.PHRManagementService;
import telematik.ws.conn.phrs.phrmanagementservice.wsdl.v2_5.PHRManagementServicePortType;
import telematik.ws.conn.phrs.phrservice.wsdl.v2_0.PHRService;
import telematik.ws.conn.phrs.phrservice.wsdl.v2_0.PHRServicePortType;
import telematik.ws.conn.plus.ObjectFactory;
import telematik.ws.conn.servicedirectory.xsd.v3_1.ConnectorServices;
import telematik.ws.conn.serviceinformation.xsd.v2_0.EndpointType;
import telematik.ws.conn.signatureservice.wsdl.v7_5.SignatureService;
import telematik.ws.conn.signatureservice.wsdl.v7_5.SignatureServicePortType;
import telematik.ws.conn.vsds.vsdservice.wsdl.v5_2.VSDService;
import telematik.ws.conn.vsds.vsdservice.wsdl.v5_2.VSDServicePortType;

/** Uses Apache CXF to generate client implementations for the Konnektor web services. */
public class KonnektorConnectionFactoryImpl implements KonnektorConnectionFactory {

  public static final String HTTPS_PROTOCOL = "https";
  private final LoggingFeature loggingFeature = newLoggingFeature();
  private final KonnektorConnectionConfiguration configuration;
  boolean isTlsPreferred = true;

  public KonnektorConnectionFactoryImpl(KonnektorConnectionConfiguration config) {
    this.configuration = config;
    this.isTlsPreferred = determineIfTlsPreferred();
  }

  @Override
  public KonnektorConnection connect() {

    var connectorServices = sdsApi().getConnectorSds();

    var phrService = createPhrService(connectorServices);
    var phrManagementService = createPHRManagementService(connectorServices);
    var eventService = createEventService(connectorServices);
    var cardService = createCardService(connectorServices);
    var certificateService = createCertificateService(connectorServices);
    var signatureService = createSignatureService(connectorServices);
    var vsdService = createVSDService(connectorServices);

    return new KonnektorConnectionImpl(
        phrService,
        phrManagementService,
        eventService,
        cardService,
        certificateService,
        signatureService,
        vsdService);
  }

  /**
   * Get the client implementation of the {@link SdsApi}, for the retrieval of the connection
   * information of the Konnektor web services.
   */
  private SdsApi sdsApi() {
    var factoryBean = new JAXRSClientFactoryBean();
    factoryBean.setServiceClass(SdsApi.class);
    factoryBean.getFeatures().add(loggingFeature);
    factoryBean.setAddress(configuration.uri().toString());

    final SdsApi sdsApi = factoryBean.create(SdsApi.class);

    WebClient.client(sdsApi).accept("text/xml", "application/xml");

    var sdsHttpConduit = WebClient.getConfig(sdsApi).getHttpConduit();

    if (isTlsPreferred) {
      configureTls(sdsHttpConduit);
    }

    configureBasicAuthenticationIfEnabled(sdsHttpConduit);

    configureProxyIfEnabled(sdsHttpConduit);

    return sdsApi;
  }

  /**
   * Create the client implementation for the Konnektor Webservices using a {@link
   * JaxWsProxyFactoryBean}.<br>
   * If the way the client implementations are created, is to be changed, e.g. for testing purposes,
   * this method can be overwritten.
   *
   * @param portType the class type, for which the client implementation is to be created
   * @param soapBinding the SOAP Binding to use (see {@link jakarta.xml.ws.soap.SOAPBinding} for
   *     possible values)
   * @param endpointAddress endpoint address of the Konnektor service with which to communicate
   * @param addConf If additional interceptors, features, et cetera are to be configured for the
   *     client, a consumer can be provided here, with the code which does. If this is not
   *     necessary, simply pass null.
   * @param <T> type of the Konnektor service, for which the client implementation is to be created
   * @return T returns the created client implementation of the given class type
   */
  private <T> T getClientProxyImpl(
      @NonNull final Class<T> portType,
      @NonNull final String soapBinding,
      @NonNull final URI endpointAddress,
      final Consumer<JaxWsProxyFactoryBean> addConf) {
    final JaxWsProxyFactoryBean jaxWsProxyFactory = new JaxWsProxyFactoryBean();
    jaxWsProxyFactory.setBindingId(soapBinding);
    jaxWsProxyFactory.setServiceClass(portType);
    jaxWsProxyFactory.setAddress(endpointAddress.toString());
    jaxWsProxyFactory.getFeatures().add(loggingFeature);

    if (Objects.nonNull(addConf)) {
      addConf.accept(jaxWsProxyFactory);
    }

    final T proxy = jaxWsProxyFactory.create(portType);

    var httpConduit = (HTTPConduit) ClientProxy.getClient(proxy).getConduit();

    if (isTlsPreferred) {
      configureTls(httpConduit);
    }

    configureBasicAuthenticationIfEnabled(httpConduit);

    configureProxyIfEnabled(httpConduit);

    return proxy;
  }

  protected TLSClientParameters tlsClientParameters() {
    final TLSClientParameters tlsParams = new TLSClientParameters();
    tlsParams.setDisableCNCheck(true);

    tlsParams.setTrustManagers(
        configuration.tlsConfig().trustManagers().toArray(new TrustManager[0]));

    return tlsParams;
  }

  private <T> T getClientProxyImpl(final Class<T> portType, final URI endpointAddress) {
    return getClientProxyImpl(portType, SOAP11HTTP_BINDING, endpointAddress, null);
  }

  /**
   * Creates the actual client implementation of the Konnektors {@link PHRServicePortType}
   * interface.<br>
   * Endpoint of the Konnektor Service to talk to is retrieved from the information in the {@link
   * ConnectorServices} bean.
   *
   * @return PHRServicePortType implementation
   */
  private PHRServicePortType createPhrService(ConnectorServices connectorServices) {
    return getClientProxyImpl(
        PHRServicePortType.class,
        SOAP12HTTP_MTOM_BINDING,
        readServiceEndpoint(connectorServices, PHRService.SERVICE.getLocalPart(), "2.0.2", "2"),
        jaxWsProxyFactory -> {
          jaxWsProxyFactory.getFeatures().add(new WSAddressingFeature());
          jaxWsProxyFactory.getOutInterceptors().add(new HomeCommunityBlockOutInterceptor());
          jaxWsProxyFactory.getOutInterceptors().add(new MtomConfigOutInterceptor());
        });
  }

  /**
   * Creates the actual client implementation of the Konnektors {@link PHRManagementServicePortType}
   * interface.<br>
   * Endpoint of the Konnektor Service to talk to is retrieved from the information in the {@link
   * ConnectorServices} bean.
   *
   * @return PHRManagementServicePortType_V2_0_1 implementation
   */
  private PHRManagementServicePortType createPHRManagementService(
      ConnectorServices connectorServices) {
    return getClientProxyImpl(
        PHRManagementServicePortType.class,
        SOAP12HTTP_BINDING,
        readServiceEndpoint(
            connectorServices,
            PHRManagementService.SERVICE.getLocalPart(),
            "2.5.2",
            "2.5.3",
            "2.5"),
        jaxWsProxyFactory -> jaxWsProxyFactory.getFeatures().add(new WSAddressingFeature()));
  }

  /**
   * Creates the actual client implementation of the Konnektors {@link SignatureServicePortType}
   * interface.<br>
   * Endpoint of the Konnektor Service to talk to is retrieved from the information in the {@link
   * ConnectorServices} bean.
   *
   * @return SignatureServicePortType implementation
   */
  private SignatureServicePortType createSignatureService(ConnectorServices connectorServices) {
    return getClientProxyImpl(
        SignatureServicePortType.class,
        SOAP11HTTP_BINDING,
        readServiceEndpoint(connectorServices, SignatureService.SERVICE.getLocalPart(), "7.5", "7"),
        jaxWsProxyFactory ->
            XmlUtils.registerObjectFactory(jaxWsProxyFactory, ObjectFactory.class));
  }

  /**
   * Creates the actual client implementation of the Konnektors {@link EventServicePortType}
   * interface.<br>
   * Endpoint of the Konnektor Service to talk to is retrieved from the information in the {@link
   * ConnectorServices} bean.
   *
   * @return EventServicePortType implementation
   */
  private EventServicePortType createEventService(ConnectorServices connectorServices) {
    return getClientProxyImpl(
        EventServicePortType.class,
        readServiceEndpoint(connectorServices, EventService.SERVICE.getLocalPart(), ""));
  }

  /**
   * Creates the actual client implementation of the Konnektors {@link VSDServicePortType}
   * interface.<br>
   * Endpoint of the Konnektor Service to talk to is retrieved from the information in the {@link
   * ConnectorServices} bean.
   *
   * @return VSDServicePortType implementation
   */
  private VSDServicePortType createVSDService(ConnectorServices connectorServices) {
    return getClientProxyImpl(
        VSDServicePortType.class,
        readServiceEndpoint(connectorServices, VSDService.SERVICE.getLocalPart(), ""));
  }

  /**
   * Creates the actual client implementation of the Konnektors {@link CertificateServicePortType}
   * interface.<br>
   * Endpoint of the Konnektor Service to talk to is retrieved from the information in the {@link
   * ConnectorServices} bean.
   *
   * @return CertificateServicePortType implementation
   */
  private CertificateServicePortType createCertificateService(ConnectorServices connectorServices) {
    return getClientProxyImpl(
        CertificateServicePortType.class,
        readServiceEndpoint(
            connectorServices, CertificateService.SERVICE.getLocalPart(), "6.0.1", "6"));
  }

  /**
   * Creates the actual client implementation of the Konnektors {@link CardServicePortType}
   * interface.<br>
   * Endpoint of the Konnektor Service to talk to is retrieved from the information in the {@link
   * ConnectorServices} bean.
   *
   * @return CardServicePortType implementation
   */
  private CardServicePortType createCardService(ConnectorServices connectorServices) {
    return getClientProxyImpl(
        CardServicePortType.class,
        readServiceEndpoint(
            connectorServices, CardService.SERVICE.getLocalPart(), "8.1.2", "8.1", "8"));
  }

  private URI readServiceEndpoint(
      ConnectorServices connectorServices, String serviceName, String... serviceVersionStartsWith) {
    for (String svsw : serviceVersionStartsWith) {
      var endpoint = readSingleServiceEndpoint(connectorServices, serviceName, svsw);
      if (Objects.nonNull(endpoint)) {
        // TODO: HACK Patch endpoint to go through the local address
        var s = endpoint.toString();
        s = s.replace("10.156.145.103:443", "localhost:4443");
        return URI.create(s);
      }
    }

    throw new IllegalArgumentException(
        String.format(
            "No usable service endpoint configuration found for service %s in version %s",
            serviceName, Arrays.toString(serviceVersionStartsWith)));
  }

  private URI readSingleServiceEndpoint(
      ConnectorServices connectorServices, String serviceName, String serviceVersionStartsWith) {
    return connectorServices.getServiceInformation().getService().stream()
        .filter(service -> serviceName.equals(service.getName()))
        .flatMap(service -> service.getVersions().getVersion().stream())
        .filter(
            versionedService -> versionedService.getVersion().startsWith(serviceVersionStartsWith))
        .map(
            versionedService ->
                Optional.ofNullable(versionedService.getEndpointTLS())
                    .filter(endpoint -> connectorServices.isTLSMandatory() || isTlsPreferred)
                    .orElse(versionedService.getEndpoint()))
        .map(EndpointType::getLocation)
        .map(URI::create)
        .findFirst()
        .orElse(null);
  }

  private Boolean determineIfTlsPreferred() {
    return Optional.ofNullable(configuration)
        .map(KonnektorConnectionConfiguration::uri)
        .map(URI::getScheme)
        .map(scheme -> scheme.equalsIgnoreCase(HTTPS_PROTOCOL))
        .orElse(Boolean.TRUE);
  }

  private void configureTls(final HTTPConduit httpConduit) {
    var tlsConfig =
        Objects.requireNonNull(
            configuration.tlsConfig(),
            "No configuration data present for TLS connection to the Konnektor");

    var tlsParams = tlsClientParameters();

    tlsParams.setKeyManagers(tlsConfig.keyManagers().toArray(new KeyManager[0]));
    tlsParams.setCipherSuites(tlsConfig.ciphersuites());
    httpConduit.setTlsClientParameters(tlsParams);
  }

  private void configureBasicAuthenticationIfEnabled(HTTPConduit httpConduit) {
    Optional.ofNullable(configuration.basicAuthentication())
        .filter(KonnektorConnectionConfiguration.BasicAuthenticationConfig::enabled)
        .ifPresent(
            ba -> {
              var authorizationPolicy = new AuthorizationPolicy();
              authorizationPolicy.setUserName(Objects.requireNonNull(ba.username()));
              authorizationPolicy.setPassword(Objects.requireNonNull(ba.password()));
              authorizationPolicy.setAuthorizationType("Basic");

              httpConduit.setAuthorization(authorizationPolicy);
            });
  }

  private void configureProxyIfEnabled(HTTPConduit httpConduit) {
    Optional.ofNullable(configuration.proxyAddress())
        .filter(KonnektorConnectionConfiguration.ProxyAddressConfig::enabled)
        .ifPresent(
            pa -> {
              httpConduit.getClient().setProxyServer(Objects.requireNonNull(pa.address()));
              httpConduit.getClient().setProxyServerPort(pa.port());
            });
  }

  private LoggingFeature newLoggingFeature() {
    final var feature = new LoggingFeature();
    final var sender =
        new Slf4jEventSender() {
          @Override
          protected String getLogMessage(LogEvent event) {
            var buf = new StringBuilder().append("\n");
            if (List.of(EventType.REQ_IN, EventType.REQ_OUT).contains(event.getType())) {
              buf.append(event.getHttpMethod()).append(" ").append(event.getAddress()).append("\n");
            } else {
              buf.append(event.getResponseCode())
                  .append(" ")
                  .append(event.getAddress())
                  .append("\n");
            }
            event
                .getHeaders()
                .forEach((key, value) -> buf.append(key).append(": ").append(value).append("\n"));
            return buf.append("\n").append(event.getPayload()).toString();
          }
        };
    sender.setLoggingLevel(Level.DEBUG);
    feature.setSender(sender);
    feature.setPrettyLogging(true);
    feature.setLogBinary(true);
    feature.setLogMultipart(true);

    return feature;
  }
}
