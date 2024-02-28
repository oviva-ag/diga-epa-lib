package com.oviva.epa.client.svc;

import com.oviva.epa.client.svc.model.KonnektorContext;
import telematik.ws.conn.connectorcontext.xsd.v2_0.ContextType;
import telematik.ws.conn.phrs.phrmanagementservice.wsdl.v2_5.PHRManagementServicePortType;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.ObjectFactory;
import telematik.ws.fd.phr.phrcommon.xsd.v1_1.InsurantIdType;

public class PhrManagementServiceClient {

  private final PHRManagementServicePortType phrManagementService;
  private final KonnektorContext konnektorContext;

  public PhrManagementServiceClient(
      PHRManagementServicePortType phrManagementService, KonnektorContext konnektorContext) {
    this.phrManagementService = phrManagementService;
    this.konnektorContext = konnektorContext;
  }

  public String getHomeCommunityID(String kvnr) {

    var context =
        new ContextType()
            .withClientSystemId(konnektorContext.clientSystemId())
            .withMandantId(konnektorContext.mandantId())
            .withUserId(konnektorContext.userId())
            .withWorkplaceId(konnektorContext.workplaceId());

    var insurantId =
        new InsurantIdType().withExtension(kvnr).withRoot(new InsurantIdType().getRoot());

    var getHomeCommunityIDRequest =
        new ObjectFactory()
            .createGetHomeCommunityID()
            .withInsurantID(insurantId)
            .withContext(context);

    return phrManagementService.getHomeCommunityID(getHomeCommunityIDRequest).getHomeCommunityID();
  }
}
