package com.example.synesthesia.models;

import com.google.firebase.Timestamp;

public class Comment {
    private String userId;
    private String commentText;
    private Timestamp timestamp;
    private String username; // Champ pour le nom d'utilisateur
    private String profileImageUrl; // Champ pour l'URL de l'image de profil

    // Constructeur par défaut
    public Comment() {
    }

    // Constructeur avec paramètres
    public Comment(String userId, String commentText, Timestamp timestamp, String username, String profileImageUrl) {
        this.userId = userId;
        this.commentText = commentText;
        this.timestamp = timestamp;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
    }

    public Comment(String userId, String commentText, Timestamp timestamp) {
    }

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    // Méthode pour obtenir le temps écoulé depuis le timestamp
    public String getTimeAgo() {
        if (timestamp == null) {
            return "";
        }

        long timeInMillis = timestamp.toDate().getTime();
        long now = System.currentTimeMillis();
        long diff = now - timeInMillis;

        if (diff < 60000) {
            return "Maintenant"; // Moins d'une minute
        } else if (diff < 3600000) {
            return (diff / 60000) + " minutes ago"; // Moins d'une heure
        } else if (diff < 86400000) {
            return (diff / 3600000) + " heures ago"; // Moins d'un jour
        } else {
            return (diff / 86400000) + " jours ago"; // Plus d'un jour
        }
    }
}
