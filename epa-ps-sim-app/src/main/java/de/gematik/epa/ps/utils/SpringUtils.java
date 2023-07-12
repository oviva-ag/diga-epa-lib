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

import de.gematik.epa.utils.ThrowingPredicate;
import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ResourceUtils;

/** Collection of static methods dealing with Spring specific functionality */
@UtilityClass
public class SpringUtils {

  /**
   * Find a resource, using a Spring ResourceLoader, and being a bit smart about it.<br>
   * Looks for the resource using the given path as it is, within the classpath and finally by
   * comparing it to all resources in the classpath and picking the first one, whose path ends on
   * the given path.<br>
   * In case no readable resource is found with the given path, throws a {@link
   * MissingResourceException}.
   *
   * @param resourceLoader the Spring {@link ResourceLoader} to use
   * @param resourcePath the path of the resource
   * @return the resource, if it is readable
   */
  public static Resource findReadableResource(
      @NonNull ResourceLoader resourceLoader, @NonNull String resourcePath) {
    return Optional.ofNullable(getReadableFileResource(resourceLoader, resourcePath))
        .or(
            () ->
                Optional.ofNullable(
                    getReadableFileResource(
                        resourceLoader, ResourceUtils.CLASSPATH_URL_PREFIX + resourcePath)))
        .or(() -> Optional.ofNullable(findReadableFileResource(resourcePath)))
        .orElseThrow(
            () ->
                new MissingResourceException(
                    "Resource " + resourcePath + " could not be found",
                    resourcePath,
                    resourcePath));
  }

  private static Resource getReadableFileResource(
      ResourceLoader resourceLoader, String resourcePath) {
    var resource = resourceLoader.getResource(resourcePath);
    if (resource.exists() && resource.isReadable()) {
      return resource;
    } else {
      return null;
    }
  }

  @SneakyThrows
  private static Resource findReadableFileResource(String resourcePath) {
    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource[] resources = resolver.getResources(ResourceUtils.CLASSPATH_URL_PREFIX + "**");
    return Arrays.stream(resources)
        .filter(resource -> Objects.nonNull(resource.getFilename()))
        .filter(
            (ThrowingPredicate<Resource>)
                resource -> resource.getURL().toString().endsWith(resourcePath))
        .filter(Resource::isReadable)
        .findFirst()
        .orElse(null);
  }
}
