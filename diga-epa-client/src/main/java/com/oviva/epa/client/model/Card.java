package com.oviva.epa.client.model;

public record Card(String handle, String holderName, CardType type) {

  public enum CardType {
    UNKNOWN,
    SMC_B,
    SMC_KT,
  }
}
