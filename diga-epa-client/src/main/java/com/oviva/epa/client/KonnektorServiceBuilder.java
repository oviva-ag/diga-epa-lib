package com.oviva.epa.client;

import com.oviva.epa.client.internal.ExceptionMappedKonnektorService;
import com.oviva.epa.client.internal.KonnektorServiceImpl;
import com.oviva.epa.client.internal.svc.model.KonnektorContext;
import com.oviva.epa.client.konn.KonnektorConnection;

public class KonnektorServiceBuilder {

  private KonnektorConnection connection;
  private String workplaceId = "a";
  private String mandantId = "m";
  private String clientSystemId = "c";
  private String userId = "admin";

  private KonnektorServiceBuilder() {}

  public static KonnektorServiceBuilder newBuilder() {
    return new KonnektorServiceBuilder();
  }

  public KonnektorServiceBuilder connection(KonnektorConnection connection) {
    this.connection = connection;
    return this;
  }

  public KonnektorServiceBuilder workplaceId(String workplace) {
    this.workplaceId = workplace;
    return this;
  }

  public KonnektorServiceBuilder mandantId(String mandantId) {
    this.mandantId = mandantId;
    return this;
  }

  public KonnektorServiceBuilder clientSystemId(String clientSystemId) {
    this.clientSystemId = clientSystemId;
    return this;
  }

  public KonnektorServiceBuilder userId(String userId) {
    this.userId = userId;
    return this;
  }

  public KonnektorService build() {

    if (connection == null) {
      throw new IllegalArgumentException("konnektor connection required");
    }

    if (workplaceId == null) {
      throw new IllegalArgumentException("workplaceId required");
    }

    if (mandantId == null) {
      throw new IllegalArgumentException("mandantId required");
    }

    if (clientSystemId == null) {
      throw new IllegalArgumentException("clientSystemId required");
    }

    var ctx = new KonnektorContext(mandantId, clientSystemId, workplaceId, userId);
    var svc = new KonnektorServiceImpl(connection, ctx);
    return new ExceptionMappedKonnektorService(svc);
  }
}
