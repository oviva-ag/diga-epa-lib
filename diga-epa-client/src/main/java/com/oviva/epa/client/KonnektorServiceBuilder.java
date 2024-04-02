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
  private String userAgent = "DiGA-Lib-Test/0.0.1";

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

  /** user agent according to A_22470-05 */
  public KonnektorServiceBuilder userAgent(String userAgent) {
    this.userAgent = userAgent;
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
    var svc = new KonnektorServiceImpl(userAgent, connection, ctx);
    return new ExceptionMappedKonnektorService(svc);
  }
}
