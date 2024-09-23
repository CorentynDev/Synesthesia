package com.example.synesthesia.models;

import java.util.List;

import com.google.firebase.Timestamp;

public class Recommendation {

    private String id;
    private String title;
    private String date;
    private String coverUrl;
    private String userId;
    private String username;
    private List<Comment> comments;
    private int likesCount;
    private List<String> likedBy;
    private Timestamp timestamp;

    // Empty constructor required for Firebase
    public Recommendation() {
    }

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public String getUserId() {
        return userId;
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
}
