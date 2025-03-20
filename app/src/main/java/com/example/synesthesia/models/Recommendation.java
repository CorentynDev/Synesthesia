package com.example.synesthesia.models;

import java.util.List;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

public class Recommendation {

    private String title;
    private String coverUrl;
    private String userId;
    private List<String> likedBy;
    private Timestamp timestamp;
    private String type;
    private String userNote;
    private String articleId;
    private String externalLink;

    // Empty constructor required for Firebase
    public Recommendation() {
    }

    public Recommendation(String title, String date, String coverUrl, String userId, String username, List<Comment> comments, Timestamp timestamp, String type, String userNote, String articleId) {
        this.title = title;
        this.coverUrl = coverUrl;
        this.userId = userId;
        this.timestamp = timestamp;
        this.type = type;
        this.userNote = userNote;
        this.articleId = articleId;
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

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public String getUserNote() {
        return userNote;
    }
    public void setUserNote(String userNote) {
        this.userNote = userNote;
    }

    public String getArticleId() {
        return articleId;
    }
    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public String getExternalLink() {
        return externalLink;
    }

    public void setExternalLink(String externalLink) {
        this.externalLink = externalLink;
    }
}