package com.tusher.boiwatch.models;

public class PlayerSource {
    private String name;
    private String url;
    private boolean isPremium;
    private boolean isFast;
    private boolean hasAds;
    private boolean supportsSubtitles;

    public PlayerSource(String name, String url, boolean isPremium, boolean isFast, boolean hasAds, boolean supportsSubtitles) {
        this.name = name;
        this.url = url;
        this.isPremium = isPremium;
        this.isFast = isFast;
        this.hasAds = hasAds;
        this.supportsSubtitles = supportsSubtitles;
    }

    public String getName() { return name; }
    public String getUrl() { return url; }
    public boolean isPremium() { return isPremium; }
    public boolean isFast() { return isFast; }
    public boolean hasAds() { return hasAds; }
    public boolean isSupportsSubtitles() { return supportsSubtitles; }
}
