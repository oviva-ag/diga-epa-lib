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

import de.gematik.epa.data.AuthorInstitutionConfiguration;
import de.gematik.epa.data.AuthorPerson;
import de.gematik.epa.data.SubmissionSetAuthorConfiguration;
import de.gematik.epa.ihe.model.Author;
import de.gematik.epa.ihe.model.document.DocumentInterface;
import de.gematik.epa.ihe.model.document.DocumentMetadata;
import de.gematik.epa.ihe.model.simple.AuthorInstitution;
import de.gematik.epa.konnektor.SmbInformationProvider;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;

@Accessors(fluent = true)
public class DefaultdataProvider implements DefaultdataInterface {

  @Getter(lazy = true)
  private static final DefaultdataProvider defaultInstance = new DefaultdataProvider();

  @Getter @Setter @Delegate private DefaultdataInterface defaultdata;

  public Author getSubmissionSetAuthor(List<? extends DocumentInterface> documents) {
    var firstDocumentAuthor = getFirstValidAuthorFromDocuments(documents);
    if (Optional.ofNullable(submissionSetAuthorConfiguration())
        .map(SubmissionSetAuthorConfiguration::useFirstDocumentAuthor)
        .orElse(true)) {
      return firstDocumentAuthor;
    } else {
      return getSubmissionSetAuthorFromConfig(firstDocumentAuthor);
    }
  }

  private Author getSubmissionSetAuthorFromConfig(Author firstDocumentAuthor) {
    var authorInstitution = getAuthorInstitutionFromConfig();
    var authorPerson =
        Optional.ofNullable(submissionSetAuthorConfiguration())
            .map(SubmissionSetAuthorConfiguration::authorPerson)
            .orElse(new AuthorPerson(null, null, null, null, null, null));
    var authorRole =
        Optional.ofNullable(firstDocumentAuthor).map(Author::authorRole).stream()
            .flatMap(Collection::stream)
            .findFirst()
            .or(
                () ->
                    Optional.ofNullable(submissionSetAuthorConfiguration())
                        .map(SubmissionSetAuthorConfiguration::authorRoleDefault))
            .orElse(null);

    return new Author(
        authorPerson.identifier(),
        authorPerson.familyName(),
        authorPerson.givenName(),
        authorPerson.otherName(),
        authorPerson.nameAffix(),
        authorPerson.title(),
        Collections.singletonList(authorInstitution),
        Collections.singletonList(authorRole),
        null,
        null);
  }

  private AuthorInstitution getAuthorInstitutionFromConfig() {
    if (Optional.ofNullable(submissionSetAuthorConfiguration())
        .map(SubmissionSetAuthorConfiguration::authorInstitutionConfiguration)
        .map(AuthorInstitutionConfiguration::retrieveFromSmb)
        .orElse(true)) {
      return SmbInformationProvider.defaultInstance().getOneAuthorInstitution();
    } else {
      return Optional.ofNullable(submissionSetAuthorConfiguration())
          .map(SubmissionSetAuthorConfiguration::authorInstitutionConfiguration)
          .map(AuthorInstitutionConfiguration::authorInstitution)
          .orElse(null);
    }
  }

  private Author getFirstValidAuthorFromDocuments(
      @NonNull List<? extends DocumentInterface> documents) {
    return documents.stream()
        .map(DocumentInterface::documentMetadata)
        .map(DocumentMetadata::author)
        .flatMap(Collection::stream)
        .findFirst()
        .orElse(null);
  }
}
