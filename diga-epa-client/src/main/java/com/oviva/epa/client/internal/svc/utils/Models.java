package com.oviva.epa.client.internal.svc.utils;

import telematik.ws.fd.phr.phrcommon.xsd.v1_1.InsurantIdType;

public class Models {

  private Models() {}

  public static InsurantIdType fromKvnr(String kvnr) {
    return new InsurantIdType().withExtension(kvnr).withRoot(new InsurantIdType().getRoot());
  }
}
