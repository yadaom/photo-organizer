package com.omprakashyadav.arrange.photos;

import io.quarkus.runtime.QuarkusApplication;
import jakarta.inject.Inject;
import picocli.CommandLine;

public class PicocliRunner implements QuarkusApplication {

  @Inject
  CommandLine.IFactory factory; // Picocli factory provided by Quarkus

  @Override
  public int run(String... args) {
    return new CommandLine(new TopLevelCommand(), factory).execute(args);
  }
}