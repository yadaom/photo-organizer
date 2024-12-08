package com.omprakashyadav.arrange.photos;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;

public class FileComparator {

  private static final int LARGE_BUFFER_SIZE = 1024 * 1024; // 1 MB for partial hash

  /**
   * Compares two files by size and hash. Uses partial hash for fast checks before full hash comparison.
   *
   * @param path1 Path to the first file.
   * @param path2 Path to the second file.
   * @return true if the files are identical, false otherwise.
   * @throws IOException if an I/O error occurs while reading the files.
   */
  public static boolean areFilesIdentical(Path path1, Path path2) throws IOException {
    // Check file sizes
    long size1 = Files.size(path1);
    long size2 = Files.size(path2);
    if (size1 != size2) {
      return false; // Different sizes mean files are not identical
    }

    // Compare partial hashes (1 MB)
    String partialHash1 = calculateFileHash(path1, LARGE_BUFFER_SIZE);
    String partialHash2 = calculateFileHash(path2, LARGE_BUFFER_SIZE);
    if (!partialHash1.equals(partialHash2)) {
      return false; // Partial hashes differ
    }

    // If partial hashes match, verify using a full hash comparison
    return calculateFileHash(path1, -1).equals(calculateFileHash(path2, -1));
  }

  private static final ByteBuffer BUFFER = ByteBuffer.allocateDirect(LARGE_BUFFER_SIZE); // Direct buffer

  /**
   * Calculates the hash of a file using SHA-256.
   *
   * @param filePath Path to the file.
   * @param maxBytes Maximum bytes to read for the hash (-1 for full file).
   * @return The SHA-256 hash as a hexadecimal string.
   * @throws IOException if an I/O error occurs while reading the file.
   */
  private static String calculateFileHash(Path filePath, long maxBytes) throws IOException {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ)) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(LARGE_BUFFER_SIZE); // Direct buffer
        long bytesProcessed = 0;
        int bytesRead;

        while ((bytesRead = fileChannel.read(buffer)) != -1) {
          buffer.flip(); // Prepare buffer for reading

          if (maxBytes > 0 && bytesProcessed + bytesRead > maxBytes) {
            // Adjust the buffer's limit to only process the remaining bytes
            int remainingBytes = (int) (maxBytes - bytesProcessed);
            buffer.limit(buffer.position() + remainingBytes);
          }

          digest.update(buffer); // Efficiently update digest with the buffer content
          bytesProcessed += bytesRead;

          if (maxBytes > 0 && bytesProcessed >= maxBytes) {
            break; // Stop reading after reaching the limit
          }

          buffer.clear(); // Prepare buffer for the next read
        }
      }

      // Convert the digest to a hexadecimal string
      StringBuilder sb = new StringBuilder();
      for (byte b : digest.digest()) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      throw new IOException("Error calculating file hash", e);
    }
  }
}