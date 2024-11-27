package com.oviva.epa.client;

import com.oviva.epa.client.model.*;
import de.gematik.epa.ihe.model.document.Document;
import de.gematik.epa.ihe.model.simple.AuthorInstitution;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

public interface KonnektorService {
  @NonNull
  List<AuthorInstitution> getAuthorInstitutions();

  @NonNull
  List<Card> getCardsInfo();

  @NonNull
  X509Certificate readAuthenticationCertificateForCard(@NonNull String cardHandle);

  /**
   * Uses the AuthSignatureServiceBinding::ExternalAuthenticate method of a specified card to sign
   * arbitrary bytes.
   *
   * <p><b>IMPORTANT:</b> The signature algorithms are severely limited, this only supports ECC
   * cryptography with the brainpoolP256r1 curve</b>
   *
   * @param cardHandle the handle of the card used to sign
   * @param bytesToSign arbitrary bytes to sign
   * @return the signed bytes in the <a
   *     href="https://datatracker.ietf.org/doc/html/rfc7518#section-3.4">concatenated form</a> with
   *     the algorithm
   *     <pre>CONCAT(SIGN(SHA256(bytesToSign)))</pre>
   */
  @NonNull
  byte[] authSign(@NonNull String cardHandle, byte[] bytesToSign);

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
