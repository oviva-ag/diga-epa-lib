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

package com.oviva.epa.client.konn.internal.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Optional;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.junit.jupiter.api.Test;
import telematik.ws.conn.plus.ObjectFactory;

class XmlUtilsTest {

  @Test
  void registerObjectFactoryTest() {
    var factoryBean = new JaxWsProxyFactoryBean();

    assertDoesNotThrow(() -> XmlUtils.registerObjectFactory(factoryBean, ObjectFactory.class));

    assertNotNull(factoryBean.getProperties(), "No properties in factory bean");
    assertTrue(
        factoryBean
            .getProperties()
            .containsKey(XmlUtils.JAXB_ADDITIONAL_CONTEXT_CLASSES_PROPERTY_KEY),
        "Property missing in factory bean");
    assertTrue(
        Optional.ofNullable(
                factoryBean
                    .getProperties()
                    .get(XmlUtils.JAXB_ADDITIONAL_CONTEXT_CLASSES_PROPERTY_KEY))
            .filter(value -> value instanceof Class[])
            .stream()
            .flatMap(value -> Arrays.stream(((Class<?>[]) value)))
            .anyMatch(ObjectFactory.class::equals));
  }

  @Test
  void registerObjectFactoryInExistingPropsTest() {
    var factoryBean = new JaxWsProxyFactoryBean();
    factoryBean.setProperties(new LinkedHashMap<>());

    assertDoesNotThrow(() -> XmlUtils.registerObjectFactory(factoryBean, ObjectFactory.class));

    assertTrue(
        factoryBean
            .getProperties()
            .containsKey(XmlUtils.JAXB_ADDITIONAL_CONTEXT_CLASSES_PROPERTY_KEY),
        "Property missing in factory bean");
  }
}
