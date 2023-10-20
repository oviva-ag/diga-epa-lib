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

package de.gematik.epa.ps.konnektor;

import de.gematik.epa.konnektor.KonnektorConfigurationProvider;
import de.gematik.epa.konnektor.KonnektorContextProvider;
import de.gematik.epa.konnektor.cxf.KonnektorInterfacesCxfImpl;
import de.gematik.epa.ps.konnektor.config.KonnektorConfigurationData;
import de.gematik.epa.ps.utils.SpringUtils;
import java.util.Optional;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;

/**
 * This is the Konnektor specific configuration class for the epa-ps-sim-app.<br>
 * It initializes all objects, which are required to use Konnektor Webservice operations. It is
 * written such, that it should also be usable outside a Spring context (in normal Java so to speak)
 */
@Configuration
@Slf4j
@Accessors(fluent = true)
@Profile("!test")
@EnableConfigurationProperties(KonnektorConfigurationData.class)
public class KonnektorClientConfiguration {

  @Getter protected KonnektorConfigurationData konnektorConfiguration;

  @Getter protected final ResourceLoader resourceLoader;

  private KonnektorConfigurationProvider konnektorConfigurationProvider;

  private KonnektorInterfacesCxfImpl konnektorInterfaceAssembly;

  private KonnektorContextProvider konnektorContextProvider;

  @Autowired
  public KonnektorClientConfiguration(
      KonnektorConfigurationData konnektorConfiguration, ResourceLoader resourceLoader) {
    this.konnektorConfiguration = konnektorConfiguration;
    this.resourceLoader = resourceLoader;
  }

  @Bean
  public KonnektorConfigurationProvider konnektorConfigurationProvider() {
    return Optional.ofNullable(konnektorConfigurationProvider)
        .orElse(
            konnektorConfigurationProvider =
                new KonnektorConfigurationProvider(konnektorConfiguration));
  }

  @Bean(name = "konnektorInterfaceAssembly")
  public KonnektorInterfacesCxfImpl konnektorInterfaceAssembly() {
    return Optional.ofNullable(konnektorInterfaceAssembly)
        .orElse(konnektorInterfaceAssembly = createNewKonnektorInterfaceAssembly());
  }

  @Bean
  public KonnektorContextProvider konnektorContextProvider() {
    return Optional.ofNullable(konnektorContextProvider)
        .orElse(
            konnektorContextProvider =
                new KonnektorContextProvider(
                    konnektorConfigurationProvider(), konnektorInterfaceAssembly()));
  }

  protected KonnektorInterfacesCxfImpl createNewKonnektorInterfaceAssembly() {
    return new KonnektorInterfacesCxfImpl(
            filePath -> SpringUtils.findReadableResource(resourceLoader, filePath).getInputStream())
        .update(konnektorConfiguration.connection());
  }
}
