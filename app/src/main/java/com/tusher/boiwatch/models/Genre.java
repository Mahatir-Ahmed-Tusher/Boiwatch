package com.tusher.boiwatch.models;

public class Genre {
    private int id;
    private String name;
    private int iconRes;

    public Genre(int id, String name, int iconRes) {
        this.id = id;
        this.name = name;
        this.iconRes = iconRes;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getIconRes() { return iconRes; }
}
