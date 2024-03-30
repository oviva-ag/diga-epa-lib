package com.oviva.epa.client.internal.svc;

import com.oviva.epa.client.internal.svc.model.KonnektorContext;
import com.oviva.epa.client.internal.svc.utils.Models;
import telematik.ws.conn.connectorcontext.xsd.v2_0.ContextType;
import telematik.ws.conn.phrs.phrmanagementservice.wsdl.v2_5.PHRManagementServicePortType;
import telematik.ws.conn.phrs.phrmanagementservice.xsd.v2_5.*;
import telematik.ws.fd.phr.phrcommon.xsd.v1_1.InsurantIdType;
import telematik.ws.fd.phr.phrcommon.xsd.v1_1.RecordIdentifierType;

public class PhrManagementServiceClient {

  private final PHRManagementServicePortType phrManagementService;
  private final KonnektorContext konnektorContext;

  public PhrManagementServiceClient(
      PHRManagementServicePortType phrManagementService, KonnektorContext konnektorContext) {
    this.phrManagementService = phrManagementService;
    this.konnektorContext = konnektorContext;
  }

  public GetAuthorizationStateResponse getAuthorizationState(String knvr, String homeCommunityId) {
    var context = getContext();

    var req =
        new GetAuthorizationState()
            .withContext(context)
            .withUserAgent("PS_123/V1.1.0/gematik") // TODO where does that come from?
            .withRecordIdentifier(
                new RecordIdentifierType()
                    .withInsurantId(Models.fromKvnr(knvr))
                    .withHomeCommunityId(homeCommunityId));

    return phrManagementService.getAuthorizationState(req);
  }

  public GetAuthorizationListResponse getAuthorizationList() {

    var context = getContext();
    var req = new GetAuthorizationList().withContext(context);
    return phrManagementService.getAuthorizationList(req);
  }

  public String getHomeCommunityID(String kvnr) {

    var context = getContext();

    var insurantId =
        new InsurantIdType().withExtension(kvnr).withRoot(new InsurantIdType().getRoot());

    var getHomeCommunityIDRequest =
        new ObjectFactory()
            .createGetHomeCommunityID()
            .withInsurantID(insurantId)
            .withContext(context);

    return phrManagementService.getHomeCommunityID(getHomeCommunityIDRequest).getHomeCommunityID();
  }

  private ContextType getContext() {
    return new ContextType()
        .withClientSystemId(konnektorContext.clientSystemId())
        .withMandantId(konnektorContext.mandantId())
        .withUserId(konnektorContext.userId())
        .withWorkplaceId(konnektorContext.workplaceId());
  }
}
