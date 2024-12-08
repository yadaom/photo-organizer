package com.omprakashyadav.arrange.photos;

import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.omprakashyadav.arrange.photos.FileFormats.COMMON_MEDIA;

@CommandLine.Command(
  name = "detect-duplicate",
  aliases = {"dd", "detect-duplicates"},
  mixinStandardHelpOptions = true,
  description = "Detect duplicate files."
)
public class DuplicateFileDetectCli implements Runnable {

  @CommandLine.Option(
    names = {"-i", "--input"},
    description = "Input directory in which to detect duplicate files.",
    required = true
  )
  String inputDir;

  @CommandLine.Option(
    names = {"-a", "--action"},
    description = "Possible values: move, delete, none. In case of move, duplicate files are moved to the duplicates directory.",
    defaultValue = "none"
  )
  String action;

  @Override
  public void run() {
    System.out.println("Detecting duplicates...");
    try {
      System.out.println("Scanning files...");
      final int filesScanned = FileUtil.walkFiles(Path.of(this.inputDir), COMMON_MEDIA, this::onEachFile);
      System.out.print("Scanned: " + filesScanned);
      for (Map.Entry<Long, List<Path>> entry : fileLengthToPaths.entrySet()) {
        final List<Path> value = entry.getValue();
        if (value.size() > 1) {
          detectDuplicates(entry.getKey(), value);
        }
      }
      System.out.println("Duplicate files detect: " + this.fileLengthToDuplicates.size());
      int i = 1;
      Path duplicateFilesDest = Path.of("Duplicate files");
      if (!fileLengthToDuplicates.isEmpty()) {
        Files.createDirectories(duplicateFilesDest);
      }
      for (Map.Entry<Long, Set<String>> longSetEntry : fileLengthToDuplicates.entrySet()) {
        System.out.println("Duplicates files batch: " + i++ + " of file size: " + longSetEntry.getKey());
        final List<String> duplicateFiles = longSetEntry.getValue().stream().sorted(Comparator.comparingInt(String::length).thenComparing(o -> o)).toList();
        if (action.equalsIgnoreCase("move")) {
          List<String> duplicatesFilesSkippingFirst = duplicateFiles.stream().skip(1).toList();
          for (String duplicateFile : duplicatesFilesSkippingFirst) {
            Path duplicateFilePath = Path.of(duplicateFile);
            final String fileName = duplicateFilePath.getFileName().toString();
            Path destinationFile = duplicateFilesDest.resolve(fileName);
            if (Files.exists(destinationFile)) {
              destinationFile = duplicateFilesDest.resolve(FileUtil.appendBeforeExtension(fileName, "_duplicate_" + System.currentTimeMillis()));
            }
            Files.copy(duplicateFilePath, destinationFile);
            Files.delete(duplicateFilePath);
            System.out.println("\tMoved: " + duplicateFile + " to: " + destinationFile);
          }
        } else {
          duplicateFiles.forEach(s -> System.out.println("\t Duplicate: " + s));
        }
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Map<Long, Set<String>> fileLengthToDuplicates = new ConcurrentHashMap<>();

  private void detectDuplicates(Long length, List<Path> paths) {
    for (int i = 0; i < paths.size(); i++) {
      for (int j = i + 1; j < paths.size(); j++) {
        try {
          Path p1 = paths.get(i);
          Path p2 = paths.get(j);
          if (FileComparator.areFilesIdentical(p1, p2)) {
            Set<String> duplicates = fileLengthToDuplicates.computeIfAbsent(length, new Function<Long, Set<String>>() {
              @Override
              public Set<String> apply(Long aLong) {
                return new HashSet<>();
              }
            });
            duplicates.add(p1.toAbsolutePath().toString());
            duplicates.add(p2.toAbsolutePath().toString());

          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  private final Map<Long, List<Path>> fileLengthToPaths = new ConcurrentHashMap<>();

  private void onEachFile(Integer index, Path path) {
    try {
      fileLengthToPaths.compute(Files.size(path), (size, paths) -> {
        if (paths == null) {
          paths = new ArrayList<>(List.of(path));
        } else {
          paths.add(path);
        }
        return paths;
      });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}