
package com.omprakashyadav.arrange.photos;

import picocli.CommandLine;

@CommandLine.Command(
  name = "photo-tool",
  mixinStandardHelpOptions = true,
  description = "A tool to manage and organize photos.",
  subcommands = {
    PhotoOrganizerCli.class, // Add the existing PhotoOrganizerCli as a subcommand
    PhotoSyncCli.class,       // Add the new PhotoSyncCli as a subcommand
    DuplicateFileDetectCli.class // Add the new DuplicateFileDetectCli as a subcommand
  }
)
public class TopLevelCommand {
}