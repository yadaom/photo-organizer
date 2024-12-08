package com.omprakashyadav.arrange.photos;

import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class Startup {
  public static void main(String... args) {
    System.out.println("Running main method");
    io.quarkus.runtime.Quarkus.run(PicocliRunner.class, args);
  }
}