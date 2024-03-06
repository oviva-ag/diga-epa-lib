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

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.LinkedHashMap;
import java.util.Optional;
import org.apache.cxf.frontend.ClientProxyFactoryBean;

public class XmlUtils {

  public static final String JAXB_ADDITIONAL_CONTEXT_CLASSES_PROPERTY_KEY =
      "jaxb.additionalContextClasses";

  /**
   * Register ObjectFactories for the given ClientProxyFactoryBean.<br>
   * Beware, only classes, annotated with the {@link jakarta.xml.bind.annotation.XmlRegistry}
   * annotation, are valid ObjectFactories.
   *
   * @param factoryBean {@link ClientProxyFactoryBean}, for which object factories need to be
   *     registered
   * @param objectFactoryClasses classes, which shall be used as object factories by the factoryBean
   */
  public static void registerObjectFactory(
      @NonNull ClientProxyFactoryBean factoryBean, @NonNull Class<?>... objectFactoryClasses) {
    Optional.ofNullable(factoryBean.getProperties())
        .orElseGet(
            () -> {
              factoryBean.setProperties(new LinkedHashMap<>());
              return factoryBean.getProperties();
            })
        .put(JAXB_ADDITIONAL_CONTEXT_CLASSES_PROPERTY_KEY, objectFactoryClasses);
  }
}
