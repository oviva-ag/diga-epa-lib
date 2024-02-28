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

package com.oviva.epa.client.config;

import com.oviva.epa.client.svc.phr.model.AuthorPerson;
import com.oviva.epa.client.svc.phr.model.SubmissionSetAuthorConfiguration;
import de.gematik.epa.ihe.model.Author;
import de.gematik.epa.ihe.model.document.DocumentInterface;
import de.gematik.epa.ihe.model.document.DocumentMetadata;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DefaultdataProvider {

  private DefaultdataInterface defaultdata;

  public DefaultdataProvider(DefaultdataInterface defaultdata) {
    this.defaultdata = defaultdata;
  }

  public Author getSubmissionSetAuthorFromDocuments(List<? extends DocumentInterface> documents) {
    return getFirstValidAuthorFromDocuments(documents);
  }

  public Author getSubmissionSetAuthorFromConfig(
      AuthorInstitutionProvider authorInstitutionProvider) {
    var authorInstitution = authorInstitutionProvider.getAuthorInstitution();
    var authorPerson =
        Optional.ofNullable(defaultdata.submissionSetAuthorConfiguration())
            .map(SubmissionSetAuthorConfiguration::authorPerson)
            .orElse(new AuthorPerson(null, null, null, null, null, null, null));
    var authorRole = defaultdata.submissionSetAuthorConfiguration().authorRoleDefault();

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
