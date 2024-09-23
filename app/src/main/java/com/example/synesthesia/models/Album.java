package com.example.synesthesia.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Album implements Parcelable {

    @SerializedName("id")
    private String id;
    @SerializedName("title")
    private String title;
    @SerializedName("cover")
    private String cover;
    @SerializedName("artist")
    private Artist artist;

    // Constructeur par défaut
    public Album() {}

    // Constructeur pour Parcelable
    protected Album(Parcel in) {
        id = in.readString();
        title = in.readString();
        cover = in.readString();
        artist = in.readParcelable(Artist.class.getClassLoader());
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCoverUrl() { return cover; }
    public Artist getArtist() { return artist; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCover(String cover) { this.cover = cover; }
    public void setArtist(Artist artist) { this.artist = artist; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(cover);
        parcel.writeParcelable(artist, flags);
    }
}

