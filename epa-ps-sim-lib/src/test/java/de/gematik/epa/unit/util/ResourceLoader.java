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

package de.gematik.epa.unit.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.gematik.epa.dto.request.FindRequestDTO;
import de.gematik.epa.dto.request.PutDocumentsRequestDTO;
import de.gematik.epa.dto.request.ReadVSDRequest;
import de.gematik.epa.dto.request.ReplaceDocumentsRequestDTO;
import de.gematik.epa.dto.request.RetrieveDocumentsRequestDTO;
import de.gematik.epa.dto.request.SignDocumentRequest;
import de.gematik.epa.utils.XmlUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import telematik.ws.conn.servicedirectory.xsd.v3_1.ConnectorServices;

@UtilityClass
@Accessors(fluent = true)
public class ResourceLoader {

  private final String TEST_RESOURCES_PATH = "src/test/resources/";

  public static final String REQUEST_PATH = TEST_RESOURCES_PATH + "requests/";
  public static final String RESPONSE_PATH = TEST_RESOURCES_PATH + "response/";
  public static final String PUT_DOCUMENTS_WITH_FOLDER_METADATA_REQUEST =
      REQUEST_PATH + "putDocumentsWithFolderMetadata.json";
  public static final String RETRIEVE_DOCUMENTS_REQUEST =
      REQUEST_PATH + "retrieveDocumentsRequest.json";

  public static final String PKI_PATH = TEST_RESOURCES_PATH + "pki/";
  public static final String AUT_CERTIFICATE =
      PKI_PATH + "80276883110000117894-C_SMCB_HCI_AUT_E256.crt";
  public static final String OTHER_CERTIFICATE = PKI_PATH + "wiki-gematik-de.crt";
  public static final String TEST_P12 = PKI_PATH + "test.p12";

  public static final String FIND_BY_PATIENT_ID_REQUEST =
      REQUEST_PATH + "findByPatientIdRequest.json";

  public static final String REPLACE_DOCUMENTS_REQUEST = REQUEST_PATH + "replaceDocuments.json";

  public static final String SIGN_DOCUMENT_REQUEST = REQUEST_PATH + "signDocumentRequest.json";
  public static final String READ_VSD_REQUEST = REQUEST_PATH + "readVSDRequest.json";

  public static final String CONNECTOR_SDS_PATH = RESPONSE_PATH + "connector.sds";

  private static ObjectMapper MAPPER;

  @Getter(lazy = true)
  private static final PutDocumentsRequestDTO putDocumentWithFolderMetadataRequest =
      loadDtoFromJsonFile(PutDocumentsRequestDTO.class, PUT_DOCUMENTS_WITH_FOLDER_METADATA_REQUEST);

  @Getter(lazy = true)
  private static final RetrieveDocumentsRequestDTO retrieveDocumentsRequest =
      loadDtoFromJsonFile(RetrieveDocumentsRequestDTO.class, RETRIEVE_DOCUMENTS_REQUEST);

  @Getter(lazy = true)
  private static final ReplaceDocumentsRequestDTO replaceDocumentsRequest =
      loadDtoFromJsonFile(ReplaceDocumentsRequestDTO.class, REPLACE_DOCUMENTS_REQUEST);

  @Getter(lazy = true)
  private static final FindRequestDTO findByPatientIdRequest =
      loadDtoFromJsonFile(FindRequestDTO.class, FIND_BY_PATIENT_ID_REQUEST);

  @Getter(lazy = true)
  private static final SignDocumentRequest signDocumentRequest =
      loadDtoFromJsonFile(SignDocumentRequest.class, SIGN_DOCUMENT_REQUEST);

  @Getter(lazy = true)
  private static final ReadVSDRequest readVSDRequest =
      loadDtoFromJsonFile(ReadVSDRequest.class, READ_VSD_REQUEST);

  @Getter(lazy = true)
  private static final ConnectorServices connectorServices =
      XmlUtils.unmarshal(ConnectorServices.class, readBytesFromResource(CONNECTOR_SDS_PATH));

  public static ObjectMapper getObjectMapper() {
    return Optional.ofNullable(MAPPER)
        .orElseGet(
            () -> {
              MAPPER =
                  new ObjectMapper()
                      .registerModule(new JavaTimeModule())
                      .setSerializationInclusion(Include.NON_ABSENT)
                      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
              return MAPPER;
            });
  }

  public static byte[] autCertificateAsByteArray() {
    return readBytesFromResource(AUT_CERTIFICATE);
  }

  @SneakyThrows
  public static <T> T loadDtoFromJsonFile(final Class<T> dtoClass, final String path) {
    final String fileContent = readFileContentFromResource(path);
    return getObjectMapper().readValue(fileContent, dtoClass);
  }

  public static String readFileContentFromResource(final String filePath) throws IOException {
    var file = toFile(filePath);
    return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
  }

  @SneakyThrows
  public static byte[] readBytesFromResource(final String filePath) {
    var file = toFile(filePath);
    return FileUtils.readFileToByteArray(file);
  }

  /*  public static FindRequestDTO findByPatientIdRequest() {
    return loadDtoFromJsonFile(FindRequestDTO.class, FIND_BY_PATIENT_ID_REQUEST);
  }*/

  private static File toFile(@NonNull String filePath) {
    return FileUtils.getFile(filePath);
  }
}
