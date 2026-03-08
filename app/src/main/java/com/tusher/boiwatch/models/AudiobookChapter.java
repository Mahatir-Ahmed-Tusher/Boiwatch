package com.tusher.boiwatch.models;

import java.io.Serializable;

public class AudiobookChapter implements Serializable {
    private String title;
    private long startTimeSeconds;

    public AudiobookChapter() {}

    public AudiobookChapter(String title, long startTimeSeconds) {
        this.title = title;
        this.startTimeSeconds = startTimeSeconds;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public long getStartTimeSeconds() { return startTimeSeconds; }
    public void setStartTimeSeconds(long startTimeSeconds) { this.startTimeSeconds = startTimeSeconds; }

    public String getFormattedTime() {
        long hours = startTimeSeconds / 3600;
        long minutes = (startTimeSeconds % 3600) / 60;
        long seconds = startTimeSeconds % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%d:%02d", minutes, seconds);
    }
}
