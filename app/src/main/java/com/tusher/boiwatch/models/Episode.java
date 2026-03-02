package com.tusher.boiwatch.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Episode implements Serializable {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("overview")
    private String overview;
    @SerializedName("still_path")
    private String stillPath;
    @SerializedName("episode_number")
    private int episodeNumber;
    @SerializedName("season_number")
    private int seasonNumber;
    @SerializedName("air_date")
    private String airDate;
    @SerializedName("runtime")
    private Integer runtime;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getOverview() { return overview; }
    public String getStillPath() { return "https://image.tmdb.org/t/p/w300" + stillPath; }
    public int getEpisodeNumber() { return episodeNumber; }
    public int getSeasonNumber() { return seasonNumber; }
    public String getAirDate() { return airDate; }
    public Integer getRuntime() { return runtime; }
}
