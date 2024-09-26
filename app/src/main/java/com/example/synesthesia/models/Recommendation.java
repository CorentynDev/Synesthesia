package com.example.synesthesia.models;

import java.util.List;
import com.google.firebase.Timestamp;

public class Recommendation {

    private String title;
    private String coverUrl;
    private String userId;
    private List<String> likedBy;
    private Timestamp timestamp;
    private String type;

    // Empty constructor required for Firebase
    public Recommendation() {
    }

    public Recommendation(String title, String date, String coverUrl, String userId, String username, List<Comment> comments, Timestamp timestamp, String type) {
        this.title = title;
        this.coverUrl = coverUrl;
        this.userId = userId;
        this.timestamp = timestamp;
        this.type = type;
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

    public List<String> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(List<String> likedBy) {
        this.likedBy = likedBy;
    }

    public List<String> getMarkedBy() {
        return likedBy;
    }
    public void setMarkedBy(List<String> likedBy) {
        this.likedBy = likedBy;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }
}