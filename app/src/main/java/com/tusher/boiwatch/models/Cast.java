package com.tusher.boiwatch.models;

public class Cast {
    private String name;
    private String profile_path;
    private String character;

    public String getName() { return name; }
    public String getProfilePath() { return "https://image.tmdb.org/t/p/w200" + profile_path; }
    public String getCharacter() { return character; }
}
