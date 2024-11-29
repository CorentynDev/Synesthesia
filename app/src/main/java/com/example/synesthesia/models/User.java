package com.example.synesthesia.models;

public class User {
    private String pseudo;
    private String profileImageUrl;
    private String id;

    // Constructeur
    public User(String pseudo, String profileImageUrl, String id) {
        this.pseudo = pseudo;
        this.profileImageUrl = profileImageUrl;
        this.id = id;
    }

    // Getters
    public String getPseudo() {
        return pseudo;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getId() {
        return id;
    }
}

