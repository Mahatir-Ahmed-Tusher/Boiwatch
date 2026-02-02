package com.tusher.boiwatch.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class BelongsToCollection implements Serializable {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("backdrop_path")
    private String backdropPath;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPosterPath() { return "https://image.tmdb.org/t/p/w500" + posterPath; }
    public String getBackdropPath() { return "https://image.tmdb.org/t/p/w1280" + backdropPath; }
}
