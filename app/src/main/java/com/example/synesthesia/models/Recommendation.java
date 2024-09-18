package com.example.synesthesia.models;

import java.util.List;

public class Recommendation {

    private String id;
    private String title;
    private String date;
    private String coverUrl;
    private String userId;
    private String username;  // Nouveau champ pour le pseudo de l'utilisateur
    private String comment;   // Nouveau champ pour le commentaire
    private int likesCount;
    private List<String> likedBy;

    // Constructeur par défaut (nécessaire pour Firebase)
    public Recommendation() {
    }

    // Constructeur avec tous les champs
    public Recommendation(String id, String title, String date, String coverUrl, String userId, String username, String comment) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.coverUrl = coverUrl;
        this.userId = userId;
        this.username = username;  // Initialiser le pseudo de l'utilisateur
        this.comment = comment;    // Initialiser le commentaire
    }

    // Getters et setters pour tous les champs

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    // Getter pour likesCount
    public int getLikesCount() {
        return likesCount;
    }

    // Setter pour likesCount
    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    // Getter pour likedBy
    public List<String> getLikedBy() {
        return likedBy;
    }

    // Setter pour likedBy
    public void setLikedBy(List<String> likedBy) {
        this.likedBy = likedBy;
    }
}
