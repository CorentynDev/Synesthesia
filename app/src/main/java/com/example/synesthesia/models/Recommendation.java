package com.example.synesthesia.models;

public class Recommendation {

    private String id;
    private String title;
    private String date;
    private String coverUrl;
    private String userId;

    // Constructeur sans argument (obligatoire pour Firestore)
    public Recommendation() {
        // Constructeur par défaut requis pour Firestore
    }

    // Constructeur avec arguments
    public Recommendation(String id, String title, String date, String coverUrl, String userId) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.coverUrl = coverUrl;
        this.userId = userId;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
