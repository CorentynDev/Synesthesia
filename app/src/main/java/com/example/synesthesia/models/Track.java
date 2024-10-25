package com.example.synesthesia.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Track implements Parcelable {

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("preview")
    private String previewUrl;

    @SerializedName("duration")
    private int duration;

    @SerializedName("artist")
    private Artist artist;

    @SerializedName("album")
    private Album album;

    // Empty constructor required for Firestore
    public Track() {}

    protected Track(Parcel in) {
        id = in.readString();
        title = in.readString();
        previewUrl = in.readString();
        duration = in.readInt();
        artist = in.readParcelable(Artist.class.getClassLoader());
        album = in.readParcelable(Album.class.getClassLoader());
    }

    public static final Creator<Track> CREATOR = new Creator<Track>() {
        @Override
        public Track createFromParcel(Parcel in) {
            return new Track(in);
        }

        @Override
        public Track[] newArray(int size) {
            return new Track[size];
        }
    };

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getPreviewUrl() { return previewUrl; }
    public int getDuration() { return duration; }
    public Artist getArtist() { return artist; }
    public Album getAlbum() { return album; }

    public void setId(String id) { this.id = id; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(previewUrl);
        parcel.writeInt(duration);
        parcel.writeParcelable(artist, flags);
        parcel.writeParcelable(album, flags);
    }
}