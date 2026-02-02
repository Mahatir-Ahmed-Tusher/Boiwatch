package com.tusher.boiwatch.models;

public class PlayerSource {
    private String title;
    private String sourceUrl;
    private boolean recommended;
    private boolean fast;
    private boolean ads;
    private String note;

    public PlayerSource(String title, String sourceUrl, boolean recommended, boolean fast, boolean ads, String note) {
        this.title = title;
        this.sourceUrl = sourceUrl;
        this.recommended = recommended;
        this.fast = fast;
        this.ads = ads;
        this.note = note;
    }

    public String getTitle() { return title; }
    
    // Alias for getTitle() used in PlayerActivity
    public String getName() { return title; }

    public String getSourceUrl() { return sourceUrl; }
    
    // Alias for getSourceUrl() used in PlayerActivity
    public String getUrl() { return sourceUrl; }

    public boolean isRecommended() { return recommended; }
    public boolean isFast() { return fast; }
    public boolean isAds() { return ads; }
    public String getNote() { return note; }
}
