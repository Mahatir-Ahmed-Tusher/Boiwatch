package com.tusher.boiwatch.model;

import java.io.Serializable;

public class PlayerSource implements Serializable {
    private String name;
    private String url;
    private boolean isDirect;
    private boolean hasSubtitles;
    private boolean isAutoPlay;
    private boolean isOfficial;

    public PlayerSource(String name, String url, boolean isDirect, boolean hasSubtitles, boolean isAutoPlay, boolean isOfficial) {
        this.name = name;
        this.url = url;
        this.isDirect = isDirect;
        this.hasSubtitles = hasSubtitles;
        this.isAutoPlay = isAutoPlay;
        this.isOfficial = isOfficial;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public boolean isDirect() {
        return isDirect;
    }

    public boolean hasSubtitles() {
        return hasSubtitles;
    }

    public boolean isAutoPlay() {
        return isAutoPlay;
    }

    public boolean isOfficial() {
        return isOfficial;
    }
}
