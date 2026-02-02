package com.tusher.boiwatch.models;

import com.google.gson.annotations.SerializedName;

public class Crew {
    private String name;
    @SerializedName("profile_path")
    private String profilePath;
    private String job;
    private String department;

    public String getName() {
        return name;
    }

    public String getProfilePath() {
        return "https://image.tmdb.org/t/p/w200" + profilePath;
    }

    public String getJob() {
        return job;
    }

    public String getDepartment() {
        return department;
    }
}
