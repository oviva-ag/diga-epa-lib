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

package de.gematik.epa.ps.config;

import de.gematik.epa.config.DefaultdataProvider;
import de.gematik.epa.konnektor.KonnektorContextProvider;
import de.gematik.epa.konnektor.KonnektorInterfaceProvider;
import de.gematik.epa.konnektor.SmbInformationProvider;
import de.gematik.epa.konnektor.client.CertificateServiceClient;
import de.gematik.epa.konnektor.client.EventServiceClient;
import de.gematik.epa.konnektor.client.PhrManagementClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import telematik.ws.conn.phrs.phrmanagementservice.wsdl.v2_0.PHRManagementServicePortType;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class EpaPsSimConfiguration {

  protected final DefaultdataConfig defaultdata;

  @Bean
  public DefaultdataProvider defaultdataProvider() {
    log.debug("Defaultdata: {}", defaultdata);
    return DefaultdataProvider.defaultInstance().defaultdata(defaultdata);
  }

  @Bean
  public KonnektorContextProvider konnektorContextProvider() {
    return KonnektorContextProvider.defaultInstance();
  }

  @Bean
  public KonnektorInterfaceProvider konnektorInterfaceProvider() {
    return KonnektorInterfaceProvider.defaultInstance();
  }

  @Bean
  @Lazy
  public CertificateServiceClient certificateServiceClient() {
    return new CertificateServiceClient();
  }

  @Bean
  @Lazy
  public EventServiceClient eventServiceClient() {
    return new EventServiceClient();
  }

  @Bean
  @Lazy
  public SmbInformationProvider smbInformationProvider() {
    return SmbInformationProvider.defaultInstance();
  }

  @Bean
  @Lazy
  @Autowired
  public PhrManagementClient phrManagementClient(
      PHRManagementServicePortType phrManagementService) {
    return new PhrManagementClient(phrManagementService);
  }
}
