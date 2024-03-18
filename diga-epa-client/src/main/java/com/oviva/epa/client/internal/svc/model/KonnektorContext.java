package com.oviva.epa.client.internal.svc.model;

import telematik.ws.conn.connectorcontext.xsd.v2_0.ContextType;

public record KonnektorContext(
    String mandantId, String clientSystemId, String workplaceId, String userId) {

  public ContextType toContext() {
    return new ContextType()
        .withClientSystemId(clientSystemId)
        .withMandantId(mandantId)
        .withUserId(userId)
        .withWorkplaceId(workplaceId);
  }
}
