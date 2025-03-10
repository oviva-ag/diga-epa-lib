package com.oviva.epa.client.internal;

import com.oviva.epa.client.KonnektorService;
import com.oviva.epa.client.model.*;
import de.gematik.epa.ihe.model.document.Document;
import de.gematik.epa.ihe.model.simple.AuthorInstitution;
import edu.umd.cs.findbugs.annotations.NonNull;
import jakarta.xml.ws.WebServiceException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;
import telematik.ws.conn.exception.FaultMessageException;

/**
 * Simple decorator for a KonnektorService to wrap underlying {@link
 * telematik.ws.conn.exception.FaultMessageException} into {@link KonnektorException}
 */
public class ExceptionMappedKonnektorService implements KonnektorService {

  private final KonnektorService delegate;

  public ExceptionMappedKonnektorService(KonnektorService delegate) {
    this.delegate = delegate;
  }

  @NonNull
  @Override
  public List<AuthorInstitution> getAuthorInstitutions() {
    return wrap(delegate::getAuthorInstitutions);
  }

  @NonNull
  @Override
  public List<Card> getCardsInfo() {
    return wrap(delegate::getCardsInfo);
  }

  @NonNull
  @Override
  public X509Certificate readRsaAuthenticationCertificateForCard(@NonNull String cardHandle) {
    return wrap(() -> delegate.readRsaAuthenticationCertificateForCard(cardHandle));
  }

  @NonNull
  @Override
  public X509Certificate readEccAuthenticationCertificateForCard(@NonNull String cardHandle) {
    return wrap(() -> delegate.readEccAuthenticationCertificateForCard(cardHandle));
  }

  @NonNull
  @Override
  public byte[] authSignRsaPss(@NonNull String cardHandle, byte[] bytesToSign) {
    return wrap(() -> delegate.authSignRsaPss(cardHandle, bytesToSign));
  }

  @NonNull
  @Override
  public byte[] authSignEcdsa(@NonNull String cardHandle, byte[] bytesToSign) {
    return wrap(() -> delegate.authSignEcdsa(cardHandle, bytesToSign));
  }

  @Override
  public @NonNull PinStatus verifySmcPin(@NonNull String cardHandle) {
    return wrap(() -> delegate.verifySmcPin(cardHandle));
  }

  @NonNull
  @Override
  public List<AuthorizedApplication> getAuthorizationState(
      @NonNull RecordIdentifier recordIdentifier) {
    return wrap(() -> delegate.getAuthorizationState(recordIdentifier));
  }

  @NonNull
  @Override
  public List<AuthorizationEntry> getAuthorizationList() {
    return wrap(delegate::getAuthorizationList);
  }

  @Override
  public @NonNull WriteDocumentResponse writeDocument(
      @NonNull RecordIdentifier recordIdentifier, @NonNull Document document) {
    return wrap(() -> delegate.writeDocument(recordIdentifier, document));
  }

  @Override
  public @NonNull WriteDocumentResponse replaceDocument(
      @NonNull RecordIdentifier recordIdentifier,
      @NonNull Document document,
      @NonNull UUID documentToReplaceId) {
    return wrap(() -> delegate.replaceDocument(recordIdentifier, document, documentToReplaceId));
  }

  @Override
  public @NonNull String getHomeCommunityID(@NonNull String kvnr) {
    return wrap(() -> delegate.getHomeCommunityID(kvnr));
  }

  private static <T> T wrap(WebServiceExecutor<T> requestor) {
    try {
      return requestor.execute();
    } catch (WebServiceException e) {
      throw new KonnektorException("request failed: " + e.getMessage(), e);
    }
  }

  interface WebServiceExecutor<T> {
    T execute() throws FaultMessageException;
  }
}
