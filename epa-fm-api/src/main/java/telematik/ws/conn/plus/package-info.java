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

/**
 * Package to define Java classes, which must be serializable into XML elements or attributes and
 * are required in the communication with Konnektor services. To get these classes into the XML
 * game, add create functions of the sort
 *
 * <pre>{@code
 * @XmlElementDecl(name = "XmlElement")
 * public JaxbElement<XmlClass> createXmlClassElement(XmlClass);
 * }</pre>
 *
 * in the class {@link telematik.ws.conn.plus.ObjectFactory} and make this ObjectFactory known to
 * the Jaxb context.
 */
package telematik.ws.conn.plus;
