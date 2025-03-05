package com.oviva.epa.client;

import com.oviva.epa.client.model.*;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;

public interface KonnektorService {

  @NonNull
  List<SmcbCard> listSmcbCards();

  /**
   * Uses the AuthSignatureServiceBinding::ExternalAuthenticate method of a specified card to sign
   * arbitrary bytes.
   *
   * <p><b>IMPORTANT:</b>The actual algorithms/public keys can be read from the card certificate</b>
   *
   * @param cardHandle the handle of the card used to sign
   * @param bytesToSign arbitrary bytes to sign
   * @return the signed bytes with the algorithm
   *     <pre>RSA_SIGN(SHA256(bytesToSign))</pre>
   */
  @NonNull
  byte[] authSignRsaPss(@NonNull String cardHandle, byte[] bytesToSign);

  /**
   * Uses the AuthSignatureServiceBinding::ExternalAuthenticate method of a specified card to sign
   * arbitrary bytes.
   *
   * <p><b>IMPORTANT:</b>The actual algorithms/public keys can be read from the card certificate</b>
   *
   * @param cardHandle the handle of the card used to sign
   * @param bytesToSign arbitrary bytes to sign
   * @return the signed bytes in the <a
   *     href="https://datatracker.ietf.org/doc/html/rfc7518#section-3.4">concatenated form</a> with
   *     the algorithm
   *     <pre>CONCAT(ECDSA_SIGN(SHA256(bytesToSign)))</pre>
   */
  @NonNull
  byte[] authSignEcdsa(@NonNull String cardHandle, byte[] bytesToSign);

  @NonNull
  PinStatus verifySmcPin(@NonNull String cardHandle);
}
