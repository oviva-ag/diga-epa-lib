package com.oviva.epa.client;

import com.oviva.epa.client.model.*;
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

  /** Determines the authorization state for a given electronic health record. */
  @NonNull
  List<AuthorizedApplication> getAuthorizationState(@NonNull RecordIdentifier recordIdentifier);

  /**
   * Returns a list of all electronic health records the Konnektor has access to. <br>
   * <b>IMPORTANT: There is strict rate-limiting on this endpoint. Currently once per day.</b>
   *
   * <p>See also <b>gemILF_PS_ePA_V1.4.3</b>
   *
   * <p><i>"A_19008-01 - Einschränkung der Häufigkeit der Abfrage getAuthorizationList Das PS DARF
   * den Request getAuthorizationList NICHT öfter als einmal pro Tag stellen"</i>
   */
  @NonNull
  List<AuthorizationEntry> getAuthorizationList();

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
