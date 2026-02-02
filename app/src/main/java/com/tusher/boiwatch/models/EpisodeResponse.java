package com.tusher.boiwatch.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EpisodeResponse {
    @SerializedName("episodes")
    private List<Episode> episodes;

    public List<Episode> getEpisodes() { return episodes; }
}
