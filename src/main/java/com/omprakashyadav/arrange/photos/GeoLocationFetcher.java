package com.omprakashyadav.arrange.photos;

import com.google.gson.Gson;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class GeoLocationFetcher {

  private static final String CACHE_FILE = "location_cache.json";
  private static final String CACHE_FILE_BAK = "location_cache.json.bak";

  @Inject
  GoogleGeoLocationFetcher googleGeoLocationFetcher;
  private final Map<String, String> locationCache;
  private final Gson gson;

  public GeoLocationFetcher() {
    this.gson = new Gson();
    locationCache = loadCache();
  }

  public String getLocation(double latitude, double longitude) {
    String coordinatesKey = String.format("%.6f_%.6f", latitude, longitude);
    if (locationCache.containsKey(coordinatesKey)) {
      return locationCache.get(coordinatesKey);
    }
    String location = fetchLocationFromApi(latitude, longitude);
    locationCache.put(coordinatesKey, location);
    saveCache();
    return location;
  }

  private String fetchLocationFromApi(double latitude, double longitude) {
    return this.googleGeoLocationFetcher.fetchLocationFromApi(latitude, longitude);
  }

  private Map<String, String> loadCache() {
    Path path = Path.of(CACHE_FILE);
    try {
      if (Files.exists(path) && Files.size(path) > 0) {
        try (FileReader fileReader = new FileReader(CACHE_FILE)) {
          return this.gson.fromJson(fileReader, Map.class);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return new ConcurrentHashMap<>();
  }

  private void saveCache() {
    String json = gson.toJson(locationCache);
    try {
      Path cacheFile = Path.of(CACHE_FILE);
      if (Files.exists(cacheFile))
        Files.move(cacheFile, Path.of(CACHE_FILE_BAK), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      Files.writeString(cacheFile, json);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}