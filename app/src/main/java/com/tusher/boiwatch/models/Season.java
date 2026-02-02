package com.tusher.boiwatch.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Season implements Serializable {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("overview")
    private String overview;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("season_number")
    private int seasonNumber;
    @SerializedName("episode_count")
    private int episodeCount;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getOverview() { return overview; }
    public String getPosterPath() { return "https://image.tmdb.org/t/p/w342" + posterPath; }
    public int getSeasonNumber() { return seasonNumber; }
    public int getEpisodeCount() { return episodeCount; }
}
