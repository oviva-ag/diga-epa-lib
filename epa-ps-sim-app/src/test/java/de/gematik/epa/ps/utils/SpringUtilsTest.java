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

package de.gematik.epa.ps.utils;

import static org.junit.jupiter.api.Assertions.*;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;

class SpringUtilsTest {

  private static final String WORKING_DIR = System.getProperty("user.dir");
  private static final String LOCAL_FILE_PATH =
      ResourceUtils.FILE_URL_PREFIX + WORKING_DIR + "/pom.xml";
  private static final String CLASSPATH_FILE_PATH =
      SpringUtils.class.getCanonicalName().replace(".", "/").concat(".class");
  public static final String CONNECTOR_SDS_PATH = "connector.sds";
  private static final String INCOMPLETE_FILE_PATH = CONNECTOR_SDS_PATH.substring(3);

  private final ResourceLoader resourceLoader = new DefaultResourceLoader();

  @SneakyThrows
  @Test
  void findReadableResourceLocalTest() {
    var loadedPom =
        assertDoesNotThrow(() -> SpringUtils.findReadableResource(resourceLoader, LOCAL_FILE_PATH));

    assertNotNull(loadedPom);

    assertTrue(loadedPom.contentLength() > 0);
  }

  @SneakyThrows
  @Test
  void findReadableResourceClasspathTest() {
    var classpathResource =
        assertDoesNotThrow(
            () -> SpringUtils.findReadableResource(resourceLoader, CLASSPATH_FILE_PATH));

    assertNotNull(classpathResource);

    assertTrue(classpathResource.contentLength() > 0);
  }

  @SneakyThrows
  @Test
  void findReadableResourceIncompletePathTest() {
    var incompletePathResource =
        assertDoesNotThrow(
            () -> SpringUtils.findReadableResource(resourceLoader, INCOMPLETE_FILE_PATH));

    assertNotNull(incompletePathResource);

    assertTrue(incompletePathResource.contentLength() > 0);
  }
}
