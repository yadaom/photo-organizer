package com.omprakashyadav.arrange.photos;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@Singleton
public class GoogleGeoLocationFetcher {

  private static final String GOOGLE_GEOCODE_API_URL = "https://maps.googleapis.com/maps/api/geocode/json";
  @ConfigProperty(name = "google.geocoding_api_key", defaultValue = "APIKEY")
  private String geocodingApiKey;

  private final HttpClient httpClient = HttpClient.newBuilder()
                                                  .version(HttpClient.Version.HTTP_2)
                                                  .build();


  /**
   * Fetches the human-readable location for the given latitude and longitude.
   *
   * @param latitude  The latitude coordinate.
   * @param longitude The longitude coordinate.
   * @return A human-readable location string, or "Unknown_Location" if not found.
   */
  public String fetchLocationFromApi(double latitude, double longitude) {
    try {
      URI apiUri = buildApiUri(latitude, longitude);
      Optional<String> response = sendRequest(apiUri);
      return parseLocationFromResponse(response.orElseThrow(() -> new RuntimeException("No response received")));
    } catch (Exception e) {
      System.err.println("Error fetching location: " + e.getMessage());
      return "Unknown_Location";
    }
  }

  /**
   * Builds the URI for the Google Geocoding API request.
   *
   * @param latitude  The latitude coordinate.
   * @param longitude The longitude coordinate.
   * @return The URI for the API request.
   */
  private URI buildApiUri(double latitude, double longitude) {
    String query = String.format("?latlng=%.6f,%.6f&key=%s", latitude, longitude, geocodingApiKey);
    return URI.create(GOOGLE_GEOCODE_API_URL + query);
  }

  /**
   * Sends an HTTP GET request to the given URI.
   *
   * @param uri The URI to send the request to.
   * @return The response body as an Optional string.
   * @throws Exception If an error occurs while sending the request.
   */
  private Optional<String> sendRequest(URI uri) throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
                                     .uri(uri)
                                     .GET()
                                     .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() == 200) {
      return Optional.of(response.body());
    } else {
      System.err.println("API Request failed with status code: " + response.statusCode() + " body: " + response.body());
      return Optional.empty();
    }
  }

  /**
   * Parses the formatted address from the API response.
   *
   * @param jsonResponse The JSON response as a string.
   * @return The formatted address or "Unknown_Location" if not found.
   */
  private String parseLocationFromResponse(String jsonResponse) {
    JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
    if (jsonObject.has("results") && !jsonObject.getAsJsonArray("results").isEmpty()) {
      return jsonObject.getAsJsonArray("results")
                       .get(0)
                       .getAsJsonObject()
                       .get("formatted_address")
                       .getAsString();
    } else {
      System.err.println("No location found in API response.");
      return "Unknown_Location";
    }
  }
}