package com.tusher.boiwatch.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class CollectionResponse implements Serializable {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("overview")
    private String overview;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("backdrop_path")
    private String backdropPath;
    @SerializedName("parts")
    private List<Movie> parts;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getOverview() { return overview; }
    public String getPosterPath() { return "https://image.tmdb.org/t/p/w500" + posterPath; }
    public String getBackdropPath() { return "https://image.tmdb.org/t/p/w1280" + backdropPath; }
    public List<Movie> getParts() { return parts; }
}
