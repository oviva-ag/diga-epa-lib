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

package telematik.ws.conn.plus;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import telematik.ws.conn.signatureservice.xsd.v7_5.SignDocument;

/**
 * Java representation of the CMSAttribute element, which can be submitted for additional signed and
 * unsigned CMS attributes as part of the {@link
 * telematik.ws.conn.signatureservice.wsdl.v7_5.SignatureServicePortType#signDocument(SignDocument)}
 * operation.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = CMSAttribute.CMS_ATTRIBUTE_NAME)
@XmlRootElement(name = CMSAttribute.CMS_ATTRIBUTE_NAME)
public class CMSAttribute {

  public static final String CMS_ATTRIBUTE_NAME = "CMSAttribute";
  @XmlValue protected byte[] value;

  public byte[] getValue() {
    return value;
  }

  public void setValue(byte[] value) {
    this.value = value;
  }
}
