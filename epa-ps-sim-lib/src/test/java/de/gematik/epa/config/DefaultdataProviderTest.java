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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.epa.ihe.model.Author;
import de.gematik.epa.ihe.model.document.Document;
import de.gematik.epa.ihe.model.document.DocumentMetadata;
import de.gematik.epa.konnektor.SmbInformationProvider;
import de.gematik.epa.unit.util.ResourceLoader;
import de.gematik.epa.unit.util.TestDataFactory;
import java.util.Collection;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class DefaultdataProviderTest {

  private static final List<Document> documents =
      ResourceLoader.putDocumentWithFolderMetadataRequest().documentSets();

  @SneakyThrows
  @Test
  void getSubmissionSetAuthorFromDocTest() {
    var tstObj = new DefaultdataProvider();
    var testdata = TestDataFactory.defaultdata(true, true);

    assertDoesNotThrow(() -> tstObj.defaultdata(testdata));

    var submissionSetAuthor = assertDoesNotThrow(() -> tstObj.getSubmissionSetAuthor(documents));

    assertNotNull(submissionSetAuthor);
    assertTrue(
        documents.stream()
            .map(Document::documentMetadata)
            .map(DocumentMetadata::author)
            .flatMap(Collection::stream)
            .anyMatch(author -> author.equals(submissionSetAuthor)));
  }

  @SneakyThrows
  @Test
  void getSubmissionSetAuthorFromConfigAndSmbTest() {
    TestDataFactory.initKonnektorTestConfiguration();
    TestDataFactory.setupMocksForSmbInformationProvider();

    var tstObj = new DefaultdataProvider();
    var testdata = TestDataFactory.defaultdata(false, true);
    tstObj.defaultdata(testdata);

    var submissionSetAuthor = assertDoesNotThrow(() -> tstObj.getSubmissionSetAuthor(documents));

    assertNotNull(submissionSetAuthor);
    assertEquals(
        testdata.submissionSetAuthorConfiguration().authorPerson().identifier(),
        submissionSetAuthor.identifier());
    assertEquals(
        testdata.submissionSetAuthorConfiguration().authorPerson().familyName(),
        submissionSetAuthor.familyName());
    assertEquals(
        testdata.submissionSetAuthorConfiguration().authorPerson().givenName(),
        submissionSetAuthor.givenName());
    assertEquals(
        testdata.submissionSetAuthorConfiguration().authorPerson().otherName(),
        submissionSetAuthor.otherName());
    assertEquals(
        testdata.submissionSetAuthorConfiguration().authorPerson().title(),
        submissionSetAuthor.title());
    assertEquals(
        testdata.submissionSetAuthorConfiguration().authorPerson().nameAffix(),
        submissionSetAuthor.nameAffix());

    assertEquals(
        SmbInformationProvider.defaultInstance().getOneAuthorInstitution(),
        submissionSetAuthor.authorInstitution().get(0));

    assertTrue(
        documents.stream()
            .map(Document::documentMetadata)
            .map(DocumentMetadata::author)
            .flatMap(Collection::stream)
            .map(Author::authorRole)
            .flatMap(Collection::stream)
            .toList()
            .containsAll(submissionSetAuthor.authorRole()));
  }

  @Test
  void getSubmissionSetAuthorFromConfigOnlyTest() {
    var tstObj = new DefaultdataProvider();
    var testdata = TestDataFactory.defaultdata(false, false);
    tstObj.defaultdata(testdata);

    var submissionSetAuthor = assertDoesNotThrow(() -> tstObj.getSubmissionSetAuthor(documents));

    assertNotNull(submissionSetAuthor);

    assertEquals(
        testdata
            .submissionSetAuthorConfiguration()
            .authorInstitutionConfiguration()
            .authorInstitution(),
        submissionSetAuthor.authorInstitution().get(0));
  }

  @Test
  void getSubmissionSetAuthorFromConfigAuthorRoleDefaultTest() {
    var tstObj = new DefaultdataProvider();
    var defaultdata = TestDataFactory.defaultdata(false, false);
    tstObj.defaultdata(defaultdata);

    var submissionSetAuthor = assertDoesNotThrow(() -> tstObj.getSubmissionSetAuthor(List.of()));

    assertNotNull(submissionSetAuthor);

    assertEquals(
        defaultdata.submissionSetAuthorConfiguration().authorRoleDefault(),
        submissionSetAuthor.authorRole().get(0));
  }
}
