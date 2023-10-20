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

package de.gematik.epa.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.gematik.epa.ihe.model.simple.ByteArray;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

/**
 * Class to wrap information of a file, with the two choices of referring to it via path or
 * containing it as byte array.
 */
@Schema(description = "Datei, repräsentiert entweder als Dateipfad oder direkt durch ihren Inhalt")
public final class FileInfo {

  @JsonProperty private String filePath;

  @JsonProperty private ByteArray fileContent;

  FileInfo() {}

  public FileInfo(String filePath) {
    this();
    setFilePath(filePath);
  }

  public FileInfo(ByteArray fileContent) {
    this();
    setFileContent(fileContent);
  }

  public String getFilePath() {
    return filePath;
  }

  public ByteArray getFileContent() {
    return fileContent;
  }

  void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  void setFileContent(ByteArray fileContent) {
    this.fileContent = fileContent;
  }

  @JsonIgnore
  public boolean isFilePath() {
    return Objects.nonNull(filePath);
  }

  @JsonIgnore
  public boolean isFileContent() {
    return Objects.nonNull(fileContent);
  }

  @Override
  public String toString() {
    return "filePath: "
        + Objects.toString(filePath, "null")
        + ", fileContent: "
        + Objects.toString(fileContent, "null");
  }

  @Override
  public int hashCode() {
    return 19 + Objects.hash(filePath, fileContent);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof FileInfo fileInfoObj) {
      return this.hashCode() == fileInfoObj.hashCode();
    }
    return false;
  }
}
