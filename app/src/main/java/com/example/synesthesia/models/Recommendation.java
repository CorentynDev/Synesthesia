package com.example.synesthesia.models;

import java.util.List;

import com.google.firebase.Timestamp;

public class Recommendation {

    private String id;
    private String title;
    private String date;
    private String coverUrl;
    private String userId;
    private String username;  // Champ pour le pseudo de l'utilisateur
    private List<Comment> comments; // Liste des commentaires
    private int likesCount;
    private List<String> likedBy;
    private Timestamp timestamp;

    // Constructeur par défaut (nécessaire pour Firebase)
    public Recommendation() {
    }

    // Constructeur avec tous les champs
    public Recommendation(String id, String title, String date, String coverUrl, String userId, String username, List<Comment> comments, Timestamp timestamp) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.coverUrl = coverUrl;
        this.userId = userId;
        this.username = username;
        this.comments = comments;
        this.timestamp = timestamp;
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

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public List<String> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(List<String> likedBy) {
        this.likedBy = likedBy;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
