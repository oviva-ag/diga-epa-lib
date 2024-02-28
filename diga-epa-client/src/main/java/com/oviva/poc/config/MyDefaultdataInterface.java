package com.oviva.poc.config;

import com.oviva.poc.svc.phr.model.AuthorInstitutionConfiguration;
import com.oviva.poc.svc.phr.model.AuthorPerson;
import com.oviva.poc.svc.phr.model.SubmissionSetAuthorConfiguration;
import de.gematik.epa.ihe.model.simple.AuthorInstitution;

public class MyDefaultdataInterface implements DefaultdataInterface {

  @Override
  public SubmissionSetAuthorConfiguration submissionSetAuthorConfiguration() {
    return new SubmissionSetAuthorConfiguration(
        true,
        new AuthorPerson(
            "identifier", "familyName", "givenName", "otherName", "nameAffix", "title", "oid"),
        new AuthorInstitutionConfiguration(
            true,
            new AuthorInstitution(
                "DiGA-Hersteller und Anbieter Prof. Dr. Tina Gräfin CesaTEST-ONLY",
                "1-SMC-B-Testkarte-80276883110000145356")),
        "11^^^&amp;1.3.6.1.4.1.19376.3.276.1.5.13&amp;ISO");
  }
}
