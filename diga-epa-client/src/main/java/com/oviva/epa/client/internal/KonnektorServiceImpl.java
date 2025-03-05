package com.oviva.epa.client.internal;

import com.oviva.epa.client.*;
import com.oviva.epa.client.internal.svc.*;
import com.oviva.epa.client.internal.svc.model.KonnektorContext;
import com.oviva.epa.client.internal.svc.utils.Digest;
import com.oviva.epa.client.konn.KonnektorConnection;
import com.oviva.epa.client.model.*;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.*;
import telematik.ws.conn.cardservice.xsd.v8_1.CardInfoType;
import telematik.ws.conn.cardservice.xsd.v8_1.PinStatusEnum;
import telematik.ws.conn.cardservicecommon.xsd.v2_0.CardTypeType;

public class KonnektorServiceImpl implements KonnektorService {

  private final String userAgent; // A_22470-05
  private final EventServiceClient eventServiceClient;
  private final CardServiceClient cardServiceClient;
  private final CertificateServiceClient certificateServiceClient;
  private final AuthSignatureServiceClient authSignatureServiceClient;

  public KonnektorServiceImpl(
      String userAgent, KonnektorConnection connection, KonnektorContext konnektorContext) {
    this.userAgent = userAgent;

    eventServiceClient = new EventServiceClient(connection.eventService(), konnektorContext);

    cardServiceClient = new CardServiceClient(connection.cardService(), konnektorContext);

    certificateServiceClient =
        new CertificateServiceClient(connection.certificateService(), konnektorContext);

    authSignatureServiceClient =
        new AuthSignatureServiceClient(connection.authSignatureService(), konnektorContext);
  }

  @NonNull
  @Override
  public List<SmcbCard> listSmcbCards() {
    return eventServiceClient.getSmbInfo().getCards().getCard().stream()
        .filter(c -> c.getCardType() == CardTypeType.SMC_B)
        .map(this::getCardDetails)
        .toList();
  }

  private SmcbCard getCardDetails(CardInfoType cardInfo) {

    var cardHandle = cardInfo.getCardHandle();
    var authRsaCertificate =
        certificateServiceClient.readRsaAuthenticationCertificateForCard(cardHandle);
    var authEccCertificate =
        certificateServiceClient.readEccAuthenticationCertificateForCard(cardHandle);

    var pinVerified =
        cardServiceClient.getPinStatusResponse(cardHandle, "PIN.SMC").getPinStatus()
            == PinStatusEnum.VERIFIED;

    var telematikId = certificateServiceClient.getTelematikIdForCard(cardHandle);

    return new SmcbCard(
        cardHandle,
        telematikId,
        cardInfo.getCardHolderName(),
        authRsaCertificate,
        authEccCertificate,
        pinVerified);
  }

  @Override
  public @NonNull PinStatus verifySmcPin(@NonNull String cardHandle) {
    var response = cardServiceClient.getPinStatusResponse(cardHandle, "PIN.SMC");
    return PinStatus.valueOf(response.getPinStatus().name());
  }

  @NonNull
  @Override
  public byte[] authSignRsaPss(@NonNull String cardHandle, byte[] bytesToSign) {
    // https://gemspec.gematik.de/docs/gemSpec/gemSpec_Kon/gemSpec_Kon_V5.24.0/#4.1.13.1.1

    var hash = Digest.sha256(bytesToSign);
    return authSignatureServiceClient.signAuthHashRsaPss(cardHandle, hash);
  }

  @NonNull
  @Override
  public byte[] authSignEcdsa(@NonNull String cardHandle, byte[] bytesToSign) {
    // https://gemspec.gematik.de/docs/gemSpec/gemSpec_Kon/gemSpec_Kon_V5.24.0/#4.1.13.1.1

    var hash = Digest.sha256(bytesToSign);
    return authSignatureServiceClient.signAuthHashEcdsa(cardHandle, hash);
  }
}
