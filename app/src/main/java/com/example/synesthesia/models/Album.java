package com.example.synesthesia.models;

import com.google.gson.annotations.SerializedName;

public class Album {
    private String id;
    private String title;
    private String cover;
    private Artist artist;

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCoverUrl() { return cover; }
    public Artist getArtist() { return artist; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCover(String cover) { this.cover = cover; }
    public void setArtist(Artist artist) { this.artist = artist; }
}

