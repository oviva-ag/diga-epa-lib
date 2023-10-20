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

package de.gematik.epa.konnektor.conversion;

import de.gematik.epa.utils.XmlUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import telematik.ws.conn.vsds.vsdservice.xsd.v5_2.ReadVSDResponse;
import telematik.ws.fa.vsds.pruefungsnachweis.xsd.v1_0.PN;

@UtilityClass
@Slf4j
public class VSDServiceUtils {

  public static final int UPDATES_SUCCESSFUL = 1;
  public static final int NO_UPDATES = 2;

  public static int getResultOfOnlineCheckEGK(ReadVSDResponse response) throws IOException {
    var pruefungsnachweisAsBase64 =
        Base64.getEncoder().encodeToString(response.getPruefungsnachweis());
    byte[] decodedData = decodeData(pruefungsnachweisAsBase64);
    String xmlString = unzipDecodedData(decodedData);
    return getE(xmlString).intValue();
  }

  public static byte[] decodeData(String pruefungsnachweisAsBase64) {
    return Base64.getDecoder().decode(pruefungsnachweisAsBase64.getBytes());
  }

  public static String unzipDecodedData(byte[] compressedData) throws IOException {
    val ret = new StringBuilder();
    try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(compressedData))) {
      ret.append(new String(in.readAllBytes()));
    }
    return ret.toString();
  }

  public static BigInteger getE(String xmlString) {
    PN pn =
        XmlUtils.unmarshal(
            PN.class, new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8)));
    BigInteger e = pn.getE();
    log.info("Result of online check EGK: " + e);
    return e;
  }

  public boolean isResultSuccessful(int result) {
    if (result == UPDATES_SUCCESSFUL) {
      log.info(
          "Result of online check EGK is {}. VSD update on eGK completed successfully", result);
      return true;
    } else if (result == NO_UPDATES) {
      log.info("Result of online check EGK is {}. No need to update VSD on eGK", result);
      return true;
    } else {
      log.error(
          "Unknown result of online check EGK is {}. Please check logs manually via Konnektor management interface",
          result);
      return false;
    }
  }
}
