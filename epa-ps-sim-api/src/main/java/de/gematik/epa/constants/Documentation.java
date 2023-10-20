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

package de.gematik.epa.constants;

/** Collection of constants to be used in the documentation of the API */
public class Documentation {

  private Documentation() {}

  public static final String SUCCESS_DESCRIPTION =
      "Information, ob die Operation erfolgreich (true) oder nicht (false) ausgeführt werden konnte";

  public static final String STATUS_MSG_DESCRIPTION =
      "Detailinformation zur Ausführung der Operation. Im Fehlerfall Details zu aufgetretenen Fehlern.";

  public static final String KVNR_DESCRIPTION =
      "Identifikationsnummer (KVNR) des Aktenkonto. Für die Herausgeberidentifikation (Root) wird immer der Wert 1.2.276.0.76.4.8 verwendet";

  public static final String AS_UPDATE_DESCRIPTION =
      "Schalter, ob die übergebenen Daten die bereits vorhandenen aktualisieren (true) oder überschreiben (false) sollen. "
          + "Der Unterschied besteht darin, dass Felder, die nicht übergeben werden (null), beim Aktualisieren ignoriert werden, sprich bereits gesetzte Werte bleiben erhalten, beim Überschreiben den bisherigen Wert überschreiben, sprich bereits gesetzte Werte werden durch null überschrieben. "
          + "Dies gilt nur für die Elemente der gleichen Ebene. Subelemente eines gesetzten Elements werden immer übernommen auch wenn das Subelement nicht gesetzt ist (also den Wert null hat) und der Schalter auf true steht.";
}
