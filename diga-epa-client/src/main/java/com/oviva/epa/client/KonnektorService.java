package com.oviva.epa.client;

import com.oviva.epa.client.model.Card;
import com.oviva.epa.client.model.PinStatus;
import com.oviva.epa.client.model.RecordIdentifier;
import com.oviva.epa.client.model.WriteDocumentResponse;
import de.gematik.epa.ihe.model.document.Document;
import de.gematik.epa.ihe.model.simple.AuthorInstitution;
import java.util.List;

public interface KonnektorService {
  List<AuthorInstitution> getAuthorInstitutions();

  List<Card> getCardsInfo();

  PinStatus verifySmcPin(String cardHandle);

  WriteDocumentResponse writeDocument(RecordIdentifier recordIdentifier, Document document);

  String getHomeCommunityID(String kvnr);
}
