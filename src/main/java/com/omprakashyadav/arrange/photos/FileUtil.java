package com.omprakashyadav.arrange.photos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FileUtil {

  /**
   * Walks through the files in the given directory and its subdirectories.
   *
   * @param inputDir           The root directory to start walking from.
   * @param acceptedExtensions A list of accepted file extensions (case-insensitive).
   * @param onEachFile         A consumer callback function to execute on each matching file.
   * @return
   * @throws IOException If an I/O error occurs.
   */
  public static int walkFiles(Path inputDir, List<String> acceptedExtensions, BiConsumer<Integer, Path> onEachFile) throws IOException {
    final int[] i = {0};
    walkFiles(inputDir, acceptedExtensions, path -> onEachFile.accept(++i[0], path));
    return i[0];
  }

  /**
   * Walks through the files in the given directory and its subdirectories.
   *
   * @param inputDir           The root directory to start walking from.
   * @param acceptedExtensions A list of accepted file extensions (case-insensitive).
   * @param onEachFile         A consumer callback function to execute on each matching file.
   * @throws IOException If an I/O error occurs.
   */
  public static void walkFiles(Path inputDir, List<String> acceptedExtensions, Consumer<Path> onEachFile) throws IOException {
    try (Stream<Path> paths = Files.walk(inputDir)) {
      paths.filter(Files::isRegularFile) // Only regular files
           .filter(FileUtil::isNotHidden)
           .filter(FileUtil::isNotEmpty)
           .filter(path -> hasAcceptedExtension(path, acceptedExtensions)) // Filter by extensions
           .forEach(onEachFile); // Apply the callback function
    }
  }

  private static boolean isNotEmpty(Path path) {
    try {
      return Files.size(path) > 0;
    } catch (IOException e) {
      // Handle the exception as needed
      throw new RuntimeException(e);
    }
  }

  private static boolean isNotHidden(Path path) {
    try {
      return !Files.isHidden(path) && !path.getFileName().startsWith(".");
    } catch (IOException e) {
      // Handle the exception as needed
      throw new RuntimeException(e);
    }
  }

  /**
   * Checks if a file has an accepted extension.
   *
   * @param path               The file path.
   * @param acceptedExtensions A list of accepted file extensions (case-insensitive).
   * @return True if the file has one of the accepted extensions, otherwise false.
   */
  private static boolean hasAcceptedExtension(Path path, List<String> acceptedExtensions) {
    String fileName = path.getFileName().toString().toLowerCase();
    return acceptedExtensions.stream()
                             .anyMatch(ext -> fileName.endsWith(ext.toLowerCase()));
  }

  public static String appendBeforeExtension(String path, String suffixBeforeExtension) {
    final String extension = getExtension(path);
    if (!extension.isEmpty()) {
      final int lastIndexOf = path.lastIndexOf(extension);
      return path.substring(0, lastIndexOf - 1) + suffixBeforeExtension + "." + extension;
    } else {
      return path + suffixBeforeExtension;
    }
  }

  public static String getExtension(Path path) {
    return getExtension(path.getFileName().toString());
  }

  public static String getExtension(String path) {
    if (path != null) {
      final int i = path.lastIndexOf('.');
      return (i > 0) ? path.substring(i + 1) : "";
    }
    return "";
  }
}