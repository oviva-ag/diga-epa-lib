package com.oviva.epa.client.svc.phr;

import com.oviva.epa.client.svc.model.KonnektorContext;
import com.oviva.epa.client.svc.phr.model.RecordIdentifier;
import telematik.ws.conn.connectorcontext.xsd.v2_0.ContextType;
import telematik.ws.conn.phrs.phrservice.xsd.v2_0.ContextHeader;
import telematik.ws.fd.phr.phrcommon.xsd.v1_1.InsurantIdType;
import telematik.ws.fd.phr.phrcommon.xsd.v1_1.RecordIdentifierType;

public class ContextHeaderBuilder {

  private KonnektorContext konnektorContext;
  private RecordIdentifier recordIdentifier;

  private ContextHeaderBuilder(KonnektorContext konnektorContext) {
    this.konnektorContext = konnektorContext;
  }

  public static ContextHeaderBuilder fromKonnektorContext(KonnektorContext ctx) {
    return new ContextHeaderBuilder(ctx);
  }

  private static InsurantIdType fromKvnr(String kvnr) {
    return new InsurantIdType().withExtension(kvnr).withRoot(new InsurantIdType().getRoot());
  }

  private static ContextType buildKonnektorContext(KonnektorContext ctx) {
    return new ContextType()
        .withClientSystemId(ctx.clientSystemId())
        .withMandantId(ctx.mandantId())
        .withUserId(ctx.userId())
        .withWorkplaceId(ctx.workplaceId());
  }

  public ContextHeaderBuilder recordIdentifier(RecordIdentifier recordIdentifier) {
    this.recordIdentifier = recordIdentifier;
    return this;
  }

  public ContextHeader build() {
    var h = new ContextHeader();

    if (konnektorContext != null) {
      h.setContext(buildKonnektorContext(konnektorContext));
    }

    if (recordIdentifier != null) {
      h.setRecordIdentifier(buildRecordIdentifier(recordIdentifier));
    }
    return h;
  }

  RecordIdentifierType buildRecordIdentifier(RecordIdentifier ctx) {
    return new RecordIdentifierType()
        .withInsurantId(fromKvnr(ctx.kvnr()))
        .withHomeCommunityId(ctx.homeCommunityId());
  }
}
