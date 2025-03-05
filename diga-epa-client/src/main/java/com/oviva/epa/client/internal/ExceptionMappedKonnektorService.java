package com.oviva.epa.client.internal;

import com.oviva.epa.client.KonnektorService;
import com.oviva.epa.client.model.*;
import edu.umd.cs.findbugs.annotations.NonNull;
import jakarta.xml.ws.WebServiceException;
import java.util.List;
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
  public List<SmcbCard> listSmcbCards() {
    return wrap(delegate::listSmcbCards);
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
