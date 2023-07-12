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

package de.gematik.epa.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Objects;

public enum FolderCode {
  EGA,
  CHILDSRECORD,
  MOTHERSRECORD,
  EMP,
  NFD,
  EAB,
  DENTALRECORD,
  VACCINATION,
  PATIENTDOC,
  CARE,
  PRESCRIPTION,
  RECEIPT,
  EAU,
  OTHER,
  PRACTITIONER,
  HOSPITAL,
  LABORATORY,
  PHYSIOTHERAPY,
  PSYCHOTHERAPY,
  DERMATOLOGY,
  GYNAECOLOGY_UROLOGY,
  DENTISTRY_OMS,
  OTHER_MEDICAL,
  OTHER_NON_MEDICAL;

  @JsonCreator
  public static FolderCode fromValue(String value) {
    return Arrays.stream(FolderCode.values())
        .filter(v -> v.getName().equalsIgnoreCase(value))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "No folder known with name " + Objects.toString(value, "null")));
  }

  @JsonValue
  public String getName() {
    return this.name().toLowerCase();
  }
}
