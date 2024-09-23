package com.example.synesthesia.models;

import com.google.firebase.Timestamp;

public class Comment {
    private String userId;
    private String commentText;
    private Timestamp timestamp;

    // Empty constructor required for Firebase
    public Comment() {
    }

    public Comment(String userId, String commentText, Timestamp timestamp) {
        this.userId = userId;
        this.commentText = commentText;
        this.timestamp = timestamp;
    }

    public String getCommentText() {
        return commentText;
    }

}
