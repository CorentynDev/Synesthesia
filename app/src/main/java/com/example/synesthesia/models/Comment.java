package com.example.synesthesia.models;

import com.google.firebase.Timestamp; // Assurez-vous d'importer la bonne classe Timestamp

public class Comment {
    private String userId;
    private String commentText;
    private Timestamp timestamp; // Utiliser Firebase Timestamp

    // Constructeur vide requis pour Firebase
    public Comment() {
    }

    public Comment(String userId, String commentText, Timestamp timestamp) {
        this.userId = userId;
        this.commentText = commentText;
        this.timestamp = timestamp;
    }

    // Getters et Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
