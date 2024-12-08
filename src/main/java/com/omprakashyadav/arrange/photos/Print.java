package com.omprakashyadav.arrange.photos;

import java.text.MessageFormat;

public class Print {
  public static void println(String message, Object... args) {
    System.out.println(MessageFormat.format(message, args));
  }
}
