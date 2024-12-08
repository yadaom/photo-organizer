package com.omprakashyadav.arrange.photos;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptUtil {
  private final static MessageDigest digest;

  static {
    try {
      digest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  private static String calculateFileHash(Path filePath) throws Exception {

    try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
      final byte[] byteArray = new byte[1024];
      int bytesCount;
      while ((bytesCount = fis.read(byteArray)) != -1) {
        digest.update(byteArray, 0, bytesCount);
      }
    }
    // Convert the byte array to a hexadecimal string
    byte[] bytes = digest.digest();
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
}
