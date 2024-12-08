package com.omprakashyadav.arrange.photos;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageOrganizerWithGeo {

  private static final String UNKNOWN_DATE = "Unknown_Date";
  private final DirectoryStructureLayout directoryStructureLayout;
  private final StatsCollector statsCollector;

  public ImageOrganizerWithGeo(DirectoryStructureLayout directoryStructureLayout, StatsCollector statsCollector) {
    this.directoryStructureLayout = directoryStructureLayout;
    this.statsCollector = statsCollector;
  }

  public void organizeImages(Path inputDirectory, Path outputDirectory) throws IOException {
    final int[] count = {0};
    FileUtil.walkFiles(inputDirectory, List.of(".jpg", ".jpeg", ".png", ".mp4"), path -> {
      try {
        System.out.printf("[%d] Processing: %s\n", ++count[0], path.toAbsolutePath());
        Metadata imageMetadata;
        MediaMetadata mediaMetadata = new MediaMetadata(null, UNKNOWN_DATE);

        if (isImageFile(path)) {
          statsCollector.incrementTotalImages();
          long fileSize = Files.size(path);
          statsCollector.addImageSize(fileSize);
          imageMetadata = ImageMetadataReader.readMetadata(path.toFile());
          mediaMetadata = getMediaMetadataFromImageMetadata(imageMetadata);

          if (mediaMetadata.date() == null) {
            statsCollector.incrementUnknownDateImages();
          }
        } else if (isVideoFile(path)) {
          statsCollector.incrementTotalVideos();
          long fileSize = Files.size(path);
          statsCollector.addVideoSize(fileSize);
          Date creationDateFromFFmpeg = VideoMetadataReader.getCreationDateFromFFmpeg(path);
          if (creationDateFromFFmpeg == null) {
            statsCollector.incrementUnknownDateVideos();
          }
          mediaMetadata = getMediaMetadataFromVideoMetadata(creationDateFromFFmpeg);
        }

        copyFile(outputDirectory, mediaMetadata, path);
      } catch (Exception e) {
        System.err.println("Error processing file: " + path.getFileName() + " -> " + e.getMessage());
        statsCollector.incrementErrors();
      }
    });

    // Print final stats
    statsCollector.printStats();
  }

  private Map<String, Boolean> dirExists = new HashMap<>();

  private void copyFile(Path outputDirectory, MediaMetadata mediaMetadata, Path file) throws IOException {
    final Path outputPath = outputDirectory.resolve(mediaMetadata.layout());
    createDir(outputPath);
    Path outputFilePath = outputPath.resolve(file.getFileName().toString());
    if (Files.exists(outputFilePath)) {
      final boolean identical = FileComparator.areFilesIdentical(file, outputFilePath);
      if (!identical) {
        statsCollector.incrementDuplicateFiles();
        final String other = FileUtil.appendBeforeExtension(file.getFileName().toString(), "_similar_" + System.currentTimeMillis());
        outputFilePath = outputPath.resolve(other);
      } else {
        return;
      }
    }
    Files.copy(file, outputFilePath, StandardCopyOption.REPLACE_EXISTING);
    Instant instant;
    if (mediaMetadata.date() == null) {
      BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
      instant = attr.creationTime().toInstant();
    } else {
      instant = mediaMetadata.date().toInstant();
    }
    setFileTime(instant, outputFilePath);
  }

  private void createDir(Path outputPath) {
    dirExists.computeIfAbsent(outputPath.toAbsolutePath().toString(), a -> {
      try {
        Files.createDirectories(outputPath);
        return true;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  private MediaMetadata getMediaMetadataFromImageMetadata(Metadata metadata) {
    ExifSubIFDDirectory exifDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
    final Date date = exifDirectory != null ? exifDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL) : null;
    final String layout = date != null ? this.directoryStructureLayout.getFormat(date) : UNKNOWN_DATE;
    return new MediaMetadata(date, layout);
  }

  private MediaMetadata getMediaMetadataFromVideoMetadata(Date date) {
    final String layout = date != null ? this.directoryStructureLayout.getFormat(date) : UNKNOWN_DATE;
    return new MediaMetadata(date, layout);
  }

  private static void setFileTime(Instant creationDate, Path outputFilePath) throws IOException {
    if (creationDate != null) {
      final FileTime fileTime = FileTime.from(creationDate);
      Files.setAttribute(outputFilePath, "creationTime", fileTime);
      Files.setAttribute(outputFilePath, "lastModifiedTime", fileTime);
      Files.setAttribute(outputFilePath, "lastAccessTime", fileTime);
    }
  }

  private boolean isImageFile(Path path) {
    String extension = getFileExtension(path);
    return List.of(".jpg", ".jpeg", ".png").contains(extension.toLowerCase());
  }

  private boolean isVideoFile(Path path) {
    String extension = getFileExtension(path);
    return List.of(".mp4").contains(extension.toLowerCase());
  }

  private String getFileExtension(Path path) {
    String fileName = path.getFileName().toString();
    int lastDot = fileName.lastIndexOf('.');
    return (lastDot == -1) ? "" : fileName.substring(lastDot);
  }

  private record MediaMetadata(Date date, String layout) {
  }
}