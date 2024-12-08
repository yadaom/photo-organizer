package com.omprakashyadav.arrange.photos;

import jakarta.inject.Inject;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;

@CommandLine.Command(
  name = "photo-organizer",
  mixinStandardHelpOptions = true,
  description = "Organizes photos by date using EXIF metadata. Organize photos in folders. For example: YEAR/MONTH/DATE or YEAR/MONTH."
)
public class PhotoOrganizerCli implements Runnable {

  @CommandLine.Option(
    names = {"-i", "--input"},
    description = "Input directory containing photos.",
    required = true
  )
  String inputDir;

  @CommandLine.Option(
    names = {"-o", "--output"},
    description = "Output directory where organized photos will be stored.",
    required = true
  )
  String outputDir;

  @CommandLine.Option(
    names = {"-l", "--layout"},
    description = "Directory structure layout on the disk. Possible values: YYYY/MMM/DD, YYY/MMM",
    required = false,
    defaultValue = "YYYY/MMM"
  )
  String layout;

  @Inject
  GeoLocationFetcher geoLocationFetcher;

  @Override
  public void run() {
    try {
      final Path input = Path.of(this.inputDir);
      if (!Files.exists(input)) {
        ErrorBase.exit("Input dir: \"{0}\" doesn't exist.", this.inputDir);
        return;
      }
      if (!Files.isDirectory(input)) {
        ErrorBase.exit("Input dir: \"{0}\" is not a directory.", this.inputDir);
        return;
      }
      if (!Files.isReadable(input)) {
        ErrorBase.exit("Input dir: \"{0}\" is not readable.", this.inputDir);
        return;
      }
      Path output = Path.of(this.outputDir);
      if (input.toAbsolutePath().startsWith(output.toAbsolutePath())) {
        ErrorBase.exit("Input dir: \"{0}\" and output dir: \"{1}\" can't be same.", this.inputDir, this.outputDir);
        return;
      }
      DirectoryStructureLayout directoryStructureLayout = DirectoryStructureLayout.fromString(layout);
      System.out.println("Organizing photos with layout: " + directoryStructureLayout.getLayout() + " ...");
      ImageOrganizerWithGeo organizer = new ImageOrganizerWithGeo(directoryStructureLayout, new StatsCollector());
      organizer.organizeImages(input, Path.of(outputDir));
      System.out.println("Photo organization completed!");
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
    }
  }
}