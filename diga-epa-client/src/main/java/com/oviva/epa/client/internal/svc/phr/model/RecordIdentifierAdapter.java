package com.oviva.epa.client.internal.svc.phr.model;

import de.gematik.epa.ihe.model.simple.InsurantId;
import telematik.ws.fd.phr.phrcommon.xsd.v1_1.InsurantIdType;

/** adapts a {@link RecordIdentifier} to the IHE model */
public record RecordIdentifierAdapter(RecordIdentifier recordIdentifier)
    implements de.gematik.epa.ihe.model.simple.RecordIdentifier {

  private static final String INSURANT_ID_ROOT = new InsurantIdType().getRoot();

  @Override
  public InsurantId getInsurantId() {
    return new InsurantIdAdapter(recordIdentifier.kvnr());
  }

  @Override
  public String getHomeCommunityId() {
    return recordIdentifier.homeCommunityId();
  }

  record InsurantIdAdapter(String kvnr) implements InsurantId {

    @Override
    public String getRoot() {
      return INSURANT_ID_ROOT;
    }

    @Override
    public String getExtension() {
      return kvnr;
    }
  }
}
