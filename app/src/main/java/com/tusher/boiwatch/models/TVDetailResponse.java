package com.tusher.boiwatch.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TVDetailResponse {
    @SerializedName("id")
    private int id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("poster_path")
    private String posterPath;
    
    @SerializedName("vote_average")
    private double voteAverage;
    
    @SerializedName("first_air_date")
    private String firstAirDate;
    
    @SerializedName("seasons")
    private List<Season> seasons;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPosterPath() { return posterPath; }
    public double getVoteAverage() { return voteAverage; }
    public String getFirstAirDate() { return firstAirDate; }
    public List<Season> getSeasons() { return seasons; }
}
