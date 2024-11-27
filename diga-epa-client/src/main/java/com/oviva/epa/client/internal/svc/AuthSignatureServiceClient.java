package com.oviva.epa.client.internal.svc;

import com.oviva.epa.client.internal.svc.model.KonnektorContext;
import com.oviva.epa.client.model.KonnektorException;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import oasis.names.tc.dss._1_0.core.schema.Base64Data;
import org.bouncycastle.crypto.signers.StandardDSAEncoding;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import telematik.ws.conn.authsignatureservice.wsdl.v7_4.AuthSignatureServicePortType;
import telematik.ws.conn.signatureservice.xsd.v7_4.ExternalAuthenticate;
import telematik.ws.conn.signatureservice.xsd.v7_4.ObjectFactory;

public class AuthSignatureServiceClient {

  private static final String SIGNATURE_TYPE_ECDSA = "urn:bsi:tr:03111:ecdsa";

  private static final String SIGNATURE_TYPE_RSA = "urn:ietf:rfc:3447";
  private static final String SIGNATURE_SCHEME_RSA_PSS = "RSASSA-PSS";

  private static final ECNamedCurveParameterSpec ECC_CURVE_BP256_SPEC =
      ECNamedCurveTable.getParameterSpec("brainpoolp256r1");

  private final AuthSignatureServicePortType authSignatureService;
  private final KonnektorContext konnektorContext;

  public AuthSignatureServiceClient(
      AuthSignatureServicePortType authSignatureService, KonnektorContext konnektorContext) {
    this.authSignatureService = authSignatureService;
    this.konnektorContext = konnektorContext;
  }

  public byte[] signAuthHashRsaPss(String cardHandle, byte[] hashBytesToSign) {

    // https://datatracker.ietf.org/doc/html/rfc7518#section-3.5

    verifyAuthHash(hashBytesToSign);

    var req =
        buildExternalAuthenticate(
            cardHandle, hashBytesToSign, SIGNATURE_TYPE_RSA, SIGNATURE_SCHEME_RSA_PSS);
    var res = authSignatureService.externalAuthenticate(req);

    var base64signature = res.getSignatureObject().getBase64Signature();
    var signatureType = base64signature.getType();
    if (!SIGNATURE_TYPE_RSA.equals(signatureType)) {
      throw new KonnektorException(
          "signature type unexpected, want %s but got %s"
              .formatted(SIGNATURE_TYPE_RSA, signatureType));
    }

    // https://gemspec.gematik.de/docs/gemILF/gemILF_PS_ePA/gemILF_PS_ePA_V4.0.0/#A_24542
    return res.getSignatureObject().getBase64Signature().getValue();
  }

  /**
   * @param hashBytesToSign 32 bytes of data to sign (e.g. a SHA-256 hash)
   * @return the signed bytes in the <a
   *     href="https://datatracker.ietf.org/doc/html/rfc7518#section-3.4">concatenated form</a>
   */
  public byte[] signAuthHashEcdsa(String cardHandle, byte[] hashBytesToSign) {

    verifyAuthHash(hashBytesToSign);

    var req = buildExternalAuthenticate(cardHandle, hashBytesToSign, SIGNATURE_TYPE_ECDSA, null);
    var res = authSignatureService.externalAuthenticate(req);

    var base64signature = res.getSignatureObject().getBase64Signature();
    var signatureType = base64signature.getType();
    if (!SIGNATURE_TYPE_ECDSA.equals(signatureType)) {
      throw new KonnektorException(
          "signature type unexpected, want %s but got %s, is the connector ECC capable?"
              .formatted(SIGNATURE_TYPE_ECDSA, signatureType));
    }

    // https://gemspec.gematik.de/docs/gemILF/gemILF_PS_ePA/gemILF_PS_ePA_V4.0.0/#A_24540
    var derSignedBytes = res.getSignatureObject().getBase64Signature().getValue();
    return convertDerEcdsaToConcated(derSignedBytes);
  }

  public static byte[] convertDerEcdsaToConcated(byte[] derSignature) {
    // A_26818
    // https://datatracker.ietf.org/doc/html/rfc7518#section-3.4
    // https://openid.net/specs/draft-jones-json-web-signature-04.html#DefiningECDSA
    try {
      var signInt = StandardDSAEncoding.INSTANCE.decode(ECC_CURVE_BP256_SPEC.getN(), derSignature);
      var r = signInt[0];
      var s = signInt[1];

      var buffer = ByteBuffer.allocate(64);
      putBigInt(buffer, r);
      putBigInt(buffer, s);
      return buffer.array();
    } catch (IOException e) {
      throw new IllegalArgumentException("failed converting DER to concatenated signature", e);
    }
  }

  private static void putBigInt(ByteBuffer buffer, BigInteger bi) {
    byte[] rArray = bi.toByteArray();
    if (rArray.length == 32) {
      buffer.put(rArray);
    } else {
      buffer.put(Arrays.copyOfRange(rArray, 1, 33));
    }
  }

  private void verifyAuthHash(byte[] bytesToSign) {
    if (bytesToSign == null || bytesToSign.length != 32) {
      throw new KonnektorException(
          "bytesToSign are not of the expected length, expected %d got %d"
              .formatted(32, bytesToSign != null ? bytesToSign.length : 0));
    }
  }

  private ExternalAuthenticate buildExternalAuthenticate(
      @NonNull String cardHandle,
      @NonNull byte[] bytesToSign,
      String signatureType,
      String signatureScheme) {

    var of = new ObjectFactory();

    var base64ToSign = new Base64Data();
    base64ToSign.setValue(bytesToSign);
    base64ToSign.setMimeType("application/octet-stream");

    var binaryToSign = of.createBinaryDocumentType();
    binaryToSign.setBase64Data(base64ToSign);

    var externalAuthenticate = of.createExternalAuthenticate();
    externalAuthenticate.setContext(konnektorContext.toContext());
    externalAuthenticate.setCardHandle(cardHandle);
    externalAuthenticate.setBinaryString(binaryToSign);

    addSignatureOptions(externalAuthenticate, signatureType, signatureScheme);

    return externalAuthenticate;
  }

  private void addSignatureOptions(
      ExternalAuthenticate req, String signatureType, String signatureScheme) {
    if (signatureType == null && signatureScheme == null) {
      return;
    }

    var of = new ObjectFactory();
    var optionalInputs = of.createExternalAuthenticateOptionalInputs();
    if (signatureType != null) {
      optionalInputs.setSignatureType(signatureType);
    }
    if (signatureScheme != null) {
      optionalInputs.setSignatureSchemes(signatureScheme);
    }

    req.setOptionalInputs(optionalInputs);
  }
}
