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

package de.gematik.epa.ps;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/** Main class of the epa-ps-sim-app. Starts the Spring Boot context. */
@SpringBootApplication
public class EpaPsSimApplication {

  public static void main(final String[] args) {
    SpringApplication.run(EpaPsSimApplication.class, args);
  }

  /**
   * Create the JsonProvider as Bean, which is then used to serialize and deserialize the data,
   * which are processed at the implemented API interfaces.
   *
   * @return {@link JacksonJsonProvider}
   */
  @Bean
  public JacksonJsonProvider jsonProvider() {
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setSerializationInclusion(Include.NON_ABSENT);
    objectMapper.registerModule(new JavaTimeModule());

    final JacksonJsonProvider provider = new JacksonJsonProvider();
    provider.setMapper(objectMapper);
    provider.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
    provider.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return provider;
  }
}
