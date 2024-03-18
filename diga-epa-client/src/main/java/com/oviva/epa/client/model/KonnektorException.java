package com.oviva.epa.client.model;

public class KonnektorException extends RuntimeException {
  public KonnektorException(String message) {
    super(message);
  }

  public KonnektorException(String message, Throwable cause) {
    super(message, cause);
  }
}
