package com.omprakashyadav.arrange.photos;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Date;

public enum DirectoryStructureLayout {
  //YYYY/MMM/DD, YYY/MMM
  YEAR_MONTH_DAY("YYYY/MMM/DD"),
  YEAR_MONTH("YYYY/MMM");

  private final String layout;

  DirectoryStructureLayout(String layout) {
    this.layout = layout;
  }

  public String getLayout() {
    return layout;
  }

  public static DirectoryStructureLayout fromString(String v) {
    //Trim v
    if (v != null) {
      v = v.trim();
      for (DirectoryStructureLayout layout : DirectoryStructureLayout.values()) {
        if (layout.layout.equalsIgnoreCase(v)) {
          return layout;
        }
        if (layout.name().equalsIgnoreCase(v)) {
          return layout;
        }
        if (layout.name().replace("_", "/").equalsIgnoreCase(v)) {
          return layout;
        }

      }
    }
    return YEAR_MONTH;
  }

  public String getFormat(Date date) {
    final Instant instant = date.toInstant();
    final ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
    final int year = zonedDateTime.getYear();
    final String monthLine = zonedDateTime.getMonth().toString().substring(0, 3);
    final int day = zonedDateTime.getDayOfMonth();
    return switch (this) {
      case YEAR_MONTH -> String.format("%s/%s", year, monthLine);
      case YEAR_MONTH_DAY -> String.format("%s/%s/%s", year, monthLine, day);
      default -> String.format("%s/%s/%s", year, monthLine, day);
    };

  }
}
