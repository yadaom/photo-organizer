package com.omprakashyadav.arrange.photos;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class StatsCollector {
  private final AtomicInteger totalImages = new AtomicInteger();
  private final AtomicInteger totalVideos = new AtomicInteger();
  private final AtomicInteger unknownDateImages = new AtomicInteger();
  private final AtomicInteger unknownDateVideos = new AtomicInteger();
  private final AtomicInteger duplicateFiles = new AtomicInteger();
  private final AtomicInteger errors = new AtomicInteger();
  private final AtomicLong imageSize = new AtomicLong();
  private final AtomicLong videoSize = new AtomicLong();

  public void incrementTotalImages() {
    totalImages.incrementAndGet();
  }

  public void incrementTotalVideos() {
    totalVideos.incrementAndGet();
  }

  public void incrementUnknownDateImages() {
    unknownDateImages.incrementAndGet();
  }

  public void incrementUnknownDateVideos() {
    unknownDateVideos.incrementAndGet();
  }

  public void incrementDuplicateFiles() {
    duplicateFiles.incrementAndGet();
  }

  public void incrementErrors() {
    errors.incrementAndGet();
  }

  public void addImageSize(long size) {
    imageSize.addAndGet(size);
  }

  public void addVideoSize(long size) {
    videoSize.addAndGet(size);
  }

  public void printStats() {
    System.out.println("\n--- Statistics ---");
    System.out.println("Total Images: " + totalImages.get());
    System.out.println("Total Videos: " + totalVideos.get());
    System.out.println("Images with Unknown Dates: " + unknownDateImages.get());
    System.out.println("Videos with Unknown Dates: " + unknownDateVideos.get());
    System.out.println("Duplicate Files: " + duplicateFiles.get());
    System.out.println("Total Errors: " + errors.get());
    System.out.println("Total Image Size: " + humanReadableSize(imageSize.get()));
    System.out.println("Total Video Size: " + humanReadableSize(videoSize.get()));
    System.out.println("Average Image Size: " + humanReadableSize(imageSize.get() / Math.max(1, totalImages.get())));
    System.out.println("Average Video Size: " + humanReadableSize(videoSize.get() / Math.max(1, totalVideos.get())));
    System.out.println("Total Files Processed: " + (totalImages.get() + totalVideos.get()));
  }

  private String humanReadableSize(long bytes) {
    if (bytes < 1024) return bytes + " B";
    int exp = (int) (Math.log(bytes) / Math.log(1024));
    String pre = "KMGTPE".charAt(exp - 1) + "";
    return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
  }
}