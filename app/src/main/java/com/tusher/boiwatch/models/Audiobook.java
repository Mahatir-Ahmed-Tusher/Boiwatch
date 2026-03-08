package com.tusher.boiwatch.models;

import java.io.Serializable;
import java.util.List;

public class Audiobook implements Serializable {
    private String videoId;
    private String title;
    private String author;
    private String thumbnailUrl;
    private String duration;
    private long durationSeconds;
    private String audioStreamUrl;
    private String description;
    private List<AudiobookChapter> chapters;

    public Audiobook() {}

    public Audiobook(String videoId, String title, String author, String thumbnailUrl, String duration, long durationSeconds) {
        this.videoId = videoId;
        this.title = title;
        this.author = author;
        this.thumbnailUrl = thumbnailUrl;
        this.duration = duration;
        this.durationSeconds = durationSeconds;
    }

    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }

    public String getAudioStreamUrl() { return audioStreamUrl; }
    public void setAudioStreamUrl(String audioStreamUrl) { this.audioStreamUrl = audioStreamUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<AudiobookChapter> getChapters() { return chapters; }
    public void setChapters(List<AudiobookChapter> chapters) { this.chapters = chapters; }
}
