package com.example.synesthesia.models;

import com.google.gson.annotations.SerializedName;

public class Artist {
    private String id;
    private String name;
    private String picture;

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getImageUrl() { return picture; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setImageUrl(String picture) { this.picture = picture; }
}
