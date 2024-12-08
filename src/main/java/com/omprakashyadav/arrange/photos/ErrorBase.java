package com.omprakashyadav.arrange.photos;


public class ErrorBase {
  public static void exit(String message, Object... args) {
    Print.println(message, args);
    System.exit(1);
  }
}
