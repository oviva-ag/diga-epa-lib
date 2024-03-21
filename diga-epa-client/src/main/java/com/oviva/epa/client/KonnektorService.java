package com.oviva.epa.client;

import com.oviva.epa.client.model.Card;
import com.oviva.epa.client.model.PinStatus;
import com.oviva.epa.client.model.RecordIdentifier;
import com.oviva.epa.client.model.WriteDocumentResponse;
import de.gematik.epa.ihe.model.document.Document;
import de.gematik.epa.ihe.model.simple.AuthorInstitution;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import java.util.UUID;

public interface KonnektorService {
  @NonNull
  List<AuthorInstitution> getAuthorInstitutions();

  @NonNull
  List<Card> getCardsInfo();

  @NonNull
  PinStatus verifySmcPin(@NonNull String cardHandle);

  @NonNull
  WriteDocumentResponse writeDocument(
      @NonNull RecordIdentifier recordIdentifier, @NonNull Document document);

  @NonNull
  WriteDocumentResponse replaceDocument(
      @NonNull RecordIdentifier recordIdentifier,
      @NonNull Document document,
      @NonNull UUID documentToReplaceId);

  @NonNull
  String getHomeCommunityID(@NonNull String kvnr);
}
