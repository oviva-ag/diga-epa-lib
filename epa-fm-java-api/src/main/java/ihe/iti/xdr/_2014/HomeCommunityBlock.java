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

package ihe.iti.xdr._2014;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * HomeCommunityBlock XML type to be added to the SOAP headers of the provideAndRegister operation.
 * <br>
 * The actual schema is not yet available, so the class had to be implemented manually.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "homeCommunityBlock", namespace = "urn:ihe:iti:xdr:2014")
public class HomeCommunityBlock {

  @XmlElement(name = "homeCommunityId", namespace = "urn:ihe:iti:xdr:2014")
  private String homeCommunityId;

  public String getHomeCommunityId() {
    return homeCommunityId;
  }

  public void setHomeCommunityId(String homeCommunityId) {
    this.homeCommunityId = homeCommunityId;
  }
}
