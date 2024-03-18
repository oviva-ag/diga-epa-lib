package com.oviva.epa.client.model;

import java.util.List;
import java.util.stream.Collectors;

public class WriteDocumentException extends KonnektorException {

  private final List<Error> errors;

  public WriteDocumentException(String status, List<Error> errors) {
    super(assembleMessage(status, errors));
    this.errors = errors;
  }

  private static String assembleMessage(String status, List<Error> errors) {
    var errorMessages = errors.stream().map(Error::toString).collect(Collectors.joining(","));
    return "failed to write document status='%s', errors='%s'".formatted(status, errorMessages);
  }

  public List<Error> errors() {
    return errors;
  }

  public record Error(
      String value, String codeContext, String errorCode, String severity, String location) {}
}
