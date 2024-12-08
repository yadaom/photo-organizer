package com.omprakashyadav.arrange.photos;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateParser {

  public static Date parseExifDate(String exifDate) {
    try {
      // Define the date format in the EXIF string
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

      // Parse the string to LocalDateTime
      LocalDateTime localDateTime = LocalDateTime.parse(exifDate, formatter);

      // Convert LocalDateTime to java.util.Date
      return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalArgumentException("Invalid date format: " + exifDate);
    }
  }

}