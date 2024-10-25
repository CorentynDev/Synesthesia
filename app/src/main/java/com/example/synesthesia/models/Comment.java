package com.example.synesthesia.models;

import com.google.firebase.Timestamp;

public class Comment {
    private String userId;
    private String commentText;
    private Timestamp timestamp;
    private String username;

    // Empty constructor required for Firebase
    public Comment() {
    }

    public Comment(String userId, String commentText, Timestamp timestamp, String username) {
        this.userId = userId;
        this.commentText = commentText;
        this.timestamp = timestamp;
        this.username = username;
    }

    public Comment(String userId, String commentText, Timestamp timestamp) {
        this.userId = userId;
        this.commentText = commentText;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public String getCommentText() {
        return commentText;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
