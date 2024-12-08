package com.omprakashyadav.arrange.photos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VideoMetadataReader {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static Date getCreationDateFromFFmpeg(Path videoFilePath) throws Exception {
    String[] strings = {"ffprobe", "-i", videoFilePath.toAbsolutePath().toString(), "-show_format", "-show_streams", "-print_format", "json"};
    System.out.println("Running command: " + String.join(" ", strings));
    ProcessBuilder processBuilder = new ProcessBuilder(
      strings
    );

    // Start the FFmpeg process
    Process process = processBuilder.start();

    // Capture FFmpeg's JSON output
    StringBuilder jsonOutput = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        jsonOutput.append(line);
      }
    }

    int exitCode = process.waitFor();
    if (exitCode != 0) {
      throw new RuntimeException("FFmpeg process failed with exit code: " + exitCode);
    }

    // Parse the JSON output to extract creation_time
    return parseCreationTimeFromJson(jsonOutput.toString());
  }

  private static Date parseCreationTimeFromJson(String json) throws ParseException {

    try {
      JsonNode rootNode = objectMapper.readTree(json);

      // Look for creation_time in format or streams
      JsonNode streams = rootNode.path("streams");
      for (JsonNode stream : streams) {
        String creationTime = stream.path("tags").path("creation_time").asText(null);
        if (creationTime != null) {
          return parseDate(creationTime);
        }
      }

      JsonNode format = rootNode.path("format").path("tags").path("creation_time");
      if (!format.isMissingNode()) {
        return parseDate(format.asText());
      }

    } catch (Exception e) {
      throw new RuntimeException("Failed to parse JSON metadata: " + e.getMessage(), e);
    }

    return null;
  }

  private static Date parseDate(String creationTime) throws ParseException {
    SimpleDateFormat ffmpegDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    return ffmpegDateFormat.parse(creationTime);
  }


}