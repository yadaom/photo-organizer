package com.omprakashyadav.arrange.photos;

public class EnumUtil {

  public static <T extends Enum<T>> T toEnum(T[] enums, String s) {
    return toEnum(enums, s, null);
  }

  public static <T extends Enum<T>> T toEnum(T[] enums, String s, T defaultValue) {
    if (s == null) {
      return null;
    }
    String string = s.toLowerCase();
    for (T anEnum : enums) {
      if (anEnum.name().toLowerCase().equals(string)) {
        return anEnum;
      }
    }
    return defaultValue;
  }
}
