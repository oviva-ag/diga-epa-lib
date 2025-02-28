package com.oviva.epa.client.internal.svc.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Digest {
  private Digest() {}

  public static byte[] sha256(byte[] bytesToHash) {
    try {
      var md = MessageDigest.getInstance("SHA-256");
      return md.digest(bytesToHash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
