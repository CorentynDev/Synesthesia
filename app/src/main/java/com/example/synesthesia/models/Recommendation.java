package com.example.synesthesia.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class Recommendation implements Parcelable {

    private String id;
    private String title;
    private String coverUrl;
    private String userId;
    private List<String> likedBy;
    private List<String> markedBy;
    private Timestamp timestamp;

    // Empty constructor required for Firebase
    public Recommendation() {
    }

    public Recommendation(String id, String title, String coverUrl, String userId, String username, List<Comment> comments, Timestamp timestamp) {
        this.id = id; // Ajoutez cet id si vous souhaitez stocker un identifiant pour la recommandation
        this.title = title;
        this.coverUrl = coverUrl;
        this.userId = userId;
        this.likedBy = new ArrayList<>(); // Initialisez si besoin
        this.markedBy = new ArrayList<>(); // Initialisez si besoin
        this.timestamp = timestamp;
        // Vous pouvez aussi ajouter une logique pour stocker les commentaires, si nécessaire
    }


    // Parcelable implementation
    protected Recommendation(Parcel in) {
        id = in.readString();
        title = in.readString();
        coverUrl = in.readString();
        userId = in.readString();
        likedBy = in.createStringArrayList();
        markedBy = in.createStringArrayList();
        timestamp = in.readParcelable(Timestamp.class.getClassLoader());
    }

    public static final Creator<Recommendation> CREATOR = new Creator<Recommendation>() {
        @Override
        public Recommendation createFromParcel(Parcel in) {
            return new Recommendation(in);
        }

        @Override
        public Recommendation[] newArray(int size) {
            return new Recommendation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0; // Généralement retourne 0
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(coverUrl);
        dest.writeString(userId);
        dest.writeStringList(likedBy);
        dest.writeStringList(markedBy);
        dest.writeParcelable(timestamp, flags);
    }

    // Getters et Setters
    public String getId() {
        return id;
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
        return markedBy;
    }

    public void setMarkedBy(List<String> markedBy) {
        this.markedBy = markedBy;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public int getLikesCount() {
        return likedBy != null ? likedBy.size() : 0;
    }
}
