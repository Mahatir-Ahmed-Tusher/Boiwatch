package com.tusher.boiwatch.models;

public class ShortItem {
    private String id;
    private String title;
    private String description;
    private String channel;

    public ShortItem(String id, String title, String description, String channel) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.channel = channel;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getChannel() { return channel; }
    
    public String getEmbedUrl() {
        return "https://www.youtube.com/embed/" + id + "?autoplay=1&mute=0&rel=0&modestbranding=1&controls=0&showinfo=0&loop=1&playlist=" + id + "&enablejsapi=1&origin=https://www.youtube.com";
    }
}
