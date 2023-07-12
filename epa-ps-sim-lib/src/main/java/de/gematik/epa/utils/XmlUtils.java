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

package de.gematik.epa.utils;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.XMLInputFactory;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;

@UtilityClass
@Accessors(fluent = true)
public class XmlUtils {

  private static final String OBJECT_FACTORY_CLASS_NAME = "ObjectFactory";

  @Getter(lazy = true)
  private static final XMLInputFactory xmlInputFactory = newXmlInputFactory();

  @Getter(lazy = true)
  private static final DatatypeFactory datatypeFactory = DatatypeFactory.newDefaultInstance();

  /**
   * Marshall an object into XML.<br>
   * This can only work if the object is marked as an XML Schema type e.g. through the annotation
   * {@link jakarta.xml.bind.annotation.XmlType}. If the class is not marked as {@link
   * jakarta.xml.bind.annotation.XmlRootElement}, it is tried to create a {@link JAXBElement} for
   * the object from a factory method, which is guessed based on the typical name and location of
   * such object factory methods. If this fails, no marshalling can be performed.<br>
   * Then there's always the option to create the {@link JAXBElement} for the object beforehand and
   * use it as input parameter to this function. That way it should always work.<br>
   *
   * @param xmlObject the object to marshal into an XML
   * @return {@code byte[]} the created XML as byte array
   */
  @SneakyThrows
  public static byte[] marshal(@NonNull Object xmlObject) {
    var jaxbCtx = JAXBContext.newInstance(xmlObject.getClass());
    var jaxbMarshaller = jaxbCtx.createMarshaller();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    if (jaxbCtx.createJAXBIntrospector().isElement(xmlObject)) {
      jaxbMarshaller.marshal(xmlObject, outputStream);
    } else {
      jaxbMarshaller.marshal(toJaxbElement(xmlObject), outputStream);
    }
    return outputStream.toByteArray();
  }

  /**
   * Unmarshal an XML into a Java object of the given class.<br>
   * This can only work if the class is marked as an XML Schema type e.g. through the annotation
   * {@link jakarta.xml.bind.annotation.XmlType}.
   *
   * @param objectType the class type of which an object shall be created with the content of the
   *     given xml
   * @param marshalledObject the XML as InputStream
   * @return the created object
   * @param <T> the type of the object which is to be created from the XML
   */
  @SneakyThrows
  public static <T> T unmarshal(
      @NonNull Class<T> objectType, @NonNull InputStream marshalledObject) {
    var jaxbUnmarshaller = JAXBContext.newInstance(objectType).createUnmarshaller();
    return jaxbUnmarshaller
        .unmarshal(xmlInputFactory().createXMLStreamReader(marshalledObject), objectType)
        .getValue();
  }

  /**
   * Unmarshal an XML into a Java object of the given class.<br>
   * This can only work if the class is marked as an XML Schema type e.g. through the annotation
   * {@link jakarta.xml.bind.annotation.XmlType}.
   *
   * @param objectType the class type of which an object shall be created with the content of the
   *     given xml
   * @param marshalledObject the XML as byte array
   * @return the created object
   * @param <T> the type of the object which is to be created from the XML
   */
  @SneakyThrows
  public static <T> T unmarshal(@NonNull Class<T> objectType, byte[] marshalledObject) {
    return unmarshal(objectType, new ByteArrayInputStream(marshalledObject));
  }

  /**
   * Convert a date in the {@link LocalDate} format into the {@link XMLGregorianCalendar} format.
   * <br>
   * For the time the start of the day is used, and the system default for the timezone.
   *
   * @param date {@link LocalDate} date to convert
   * @return {@link XMLGregorianCalendar} converted date or null, if null was given as parameter
   */
  public static XMLGregorianCalendar fromLocalDate(LocalDate date) {
    return Optional.ofNullable(date)
        .map(d -> d.atStartOfDay(ZoneId.systemDefault()))
        .map(GregorianCalendar::from)
        .map(gc -> datatypeFactory().newXMLGregorianCalendar(gc))
        .orElse(null);
  }

  // region private

  private static XMLInputFactory newXmlInputFactory() {
    XMLInputFactory factory = XMLInputFactory.newInstance();
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    return factory;
  }

  /**
   * This method creates a {@link JAXBElement} for an object. This is necessary for marshalling the
   * object, if its class does not have the {@link jakarta.xml.bind.annotation.XmlRootElement}
   * annotation.<br>
   * There is a {@code @SuppressWarnings("unchecked")} annotation at this method. However, the cast
   * can't fail, as there are proper checks implemented, but these are just not recognized by the
   * IDE.
   *
   * @param xmlObject the object for which the JAXBElement is created
   * @return {@link JAXBElement} to the object
   * @param <T> class type of the object
   */
  @SneakyThrows
  @SuppressWarnings("unchecked")
  private static <T> JAXBElement<T> toJaxbElement(@NonNull T xmlObject) {
    Class<T> objClass = (Class<T>) xmlObject.getClass();
    var pckg = objClass.getPackageName();
    var objFac =
        objClass
            .getClassLoader()
            .loadClass(pckg + "." + OBJECT_FACTORY_CLASS_NAME)
            .getConstructor()
            .newInstance();

    var jaxbElementMethod =
        Arrays.stream(objFac.getClass().getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(XmlElementDecl.class))
            .filter(
                method -> {
                  if (method.getGenericReturnType() instanceof ParameterizedType returnType) {
                    return returnType.getRawType().equals(JAXBElement.class)
                        && returnType.getActualTypeArguments().length == 1
                        && Arrays.stream(returnType.getActualTypeArguments())
                            .allMatch(typeArg -> typeArg.equals(objClass));
                  }
                  return false;
                })
            .filter(
                method -> {
                  var prmTypes = method.getParameterTypes();
                  return prmTypes.length == 1
                      && Arrays.stream(prmTypes).allMatch(prmType -> prmType.equals(objClass));
                })
            .findFirst()
            .orElseThrow(
                () ->
                    new NoSuchElementException(
                        "No JAXBElement factory method found for class " + objClass));

    var result = jaxbElementMethod.invoke(objFac, xmlObject);

    return (JAXBElement<T>) result;
  }

  // endregion private
}
