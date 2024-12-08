package com.omprakashyadav.arrange.photos;

import com.drew.lang.Rational;
import com.drew.metadata.exif.GpsDirectory;

public class GpsUtils {

  /**
   * Checks if the GPS data in the GpsDirectory is valid.
   *
   * @param gpsDirectory The GpsDirectory containing GPS metadata.
   * @return True if the GPS data is valid, false otherwise.
   */
  public static boolean isValidGpsData(GpsDirectory gpsDirectory) {
    if (gpsDirectory == null) {
      return false; // No GPS data
    }

    // Get raw latitude and longitude data
    Rational[] latData = gpsDirectory.getRationalArray(GpsDirectory.TAG_LATITUDE);
    Rational[] lonData = gpsDirectory.getRationalArray(GpsDirectory.TAG_LONGITUDE);
    String latRef = gpsDirectory.getString(GpsDirectory.TAG_LATITUDE_REF);
    String lonRef = gpsDirectory.getString(GpsDirectory.TAG_LONGITUDE_REF);

    // Validate presence of all required components
    if (latData == null || lonData == null || latRef == null || lonRef == null) {
      return false; // Incomplete GPS data
    }

    // Convert to decimal degrees for validation
    try {
      double latitude = convertToDecimalDegrees(latData, latRef);
      double longitude = convertToDecimalDegrees(lonData, lonRef);

      // Check valid ranges
      return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    } catch (Exception e) {
      return false; // Conversion error indicates corrupt data
    }
  }

  /**
   * Extracts and converts latitude and longitude from a GpsDirectory.
   *
   * @param gpsDirectory The GpsDirectory containing GPS metadata.
   * @return A double array with latitude and longitude in decimal degrees, or null if data is unavailable.
   */
  public static double[] extractCoordinates(GpsDirectory gpsDirectory) {
    if (gpsDirectory == null) {
      return null; // No GPS data available
    }

    // Get the GPS data as Rational arrays
    Rational[] latData = gpsDirectory.getRationalArray(GpsDirectory.TAG_LATITUDE);
    Rational[] lonData = gpsDirectory.getRationalArray(GpsDirectory.TAG_LONGITUDE);
    String latRef = gpsDirectory.getString(GpsDirectory.TAG_LATITUDE_REF);
    String lonRef = gpsDirectory.getString(GpsDirectory.TAG_LONGITUDE_REF);

    if (latData == null || lonData == null || latRef == null || lonRef == null) {
      return null; // Incomplete GPS data
    }

    // Convert DMS to Decimal Degrees
    double latitude = convertToDecimalDegrees(latData, latRef);
    double longitude = convertToDecimalDegrees(lonData, lonRef);
    // Handle scaling if values are unexpectedly large
    if (latitude > 90 || longitude > 180) {
      latitude = latitude / 1e7; // Adjust scaling factor as needed
      longitude = longitude / 1e7;
    }
    return new double[]{latitude, longitude};
  }

  /**
   * Converts GPS data from degrees, minutes, and seconds (DMS) to decimal degrees (DD).
   *
   * @param gpsData The GPS data as a Rational array representing [degrees, minutes, seconds].
   * @param ref     The reference direction (N, S, E, W).
   * @return The decimal degrees value.
   */
  private static double convertToDecimalDegrees(Rational[] gpsData, String ref) {
    if (gpsData.length < 3) {
      throw new IllegalArgumentException("Invalid GPS data format.");
    }

    double degrees = gpsData[0].doubleValue();
    double minutes = gpsData[1].doubleValue();
    double seconds = gpsData[2].doubleValue();

    // Decimal degrees formula
    double decimalDegrees = degrees + (minutes / 60.0) + (seconds / 3600.0);

    // Apply hemisphere correction
    if ("S".equalsIgnoreCase(ref) || "W".equalsIgnoreCase(ref)) {
      decimalDegrees *= -1;
    }

    return decimalDegrees;
  }
}