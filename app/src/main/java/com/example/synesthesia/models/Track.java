package com.example.synesthesia.models;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

public class Track {
    private String id;
    private String title;
    private Artist artist;
    private String albumTitle;
    private Album album;
    @SerializedName("preview") // Assurez-vous que le nom correspond à celui de la réponse API
    private String previewUrl;

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Artist getArtistName() { return artist; }
    public void setArtistName(String artistName) { this.artist = artist; }

    public String getAlbumTitle() { return albumTitle; }
    public void setAlbumTitle(String albumTitle) { this.albumTitle = albumTitle; }

    public Album getAlbum() { return album; }
    public void setCover(String cover) { this.album = album; }

    public String getPreviewUrl() {
        Log.d("Track", "Preview URL: " + previewUrl);
        return previewUrl;
    }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }
}
