package com.omprakashyadav.arrange.photos;

import picocli.CommandLine;

@CommandLine.Command(
  name = "sync-photos",
  mixinStandardHelpOptions = true,
  description = "Syncs photos between two directories."
)
public class PhotoSyncCli implements Runnable {

  @CommandLine.Option(
    names = {"-s", "--source"},
    description = "Source directory containing photos.",
    required = true
  )
  String sourceDir;

  @CommandLine.Option(
    names = {"-d", "--destination"},
    description = "Destination directory for syncing photos.",
    required = true
  )
  String destinationDir;

  @Override
  public void run() {
    System.out.println("Coming soon");
    // Add your sync logic here
  }
}