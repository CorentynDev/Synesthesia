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

    @SerializedName("cover_small")
    private String coverSmall;

    @SerializedName("cover_medium")
    private String coverMedium;

    @SerializedName("cover_big")
    private String coverBig;

    @SerializedName("cover_xl")
    private String coverXl;

    @SerializedName("tracklist")
    private String tracklist;

    @SerializedName("nb_tracks")
    private int nbTracks;

    @SerializedName("artist")
    private Artist artist;

    // Constructeur par défaut
    public Album() {}

    // Constructeur pour Parcelable
    protected Album(Parcel in) {
        id = in.readString();
        title = in.readString();
        cover = in.readString();
        coverSmall = in.readString();
        coverMedium = in.readString();
        coverBig = in.readString();
        coverXl = in.readString();
        tracklist = in.readString();
        nbTracks = in.readInt();
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

    // Getters et Setters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCoverUrl() { return cover; }
    public String getCoverSmall() { return coverSmall; }
    public String getCoverMedium() { return coverMedium; }
    public String getCoverBig() { return coverBig; }
    public String getCoverXl() { return coverXl; }
    public String getTracklist() { return tracklist; }
    public int getNbTracks() { return nbTracks; }
    public Artist getArtist() { return artist; }

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCover(String cover) { this.cover = cover; }
    public void setCoverSmall(String coverSmall) { this.coverSmall = coverSmall; }
    public void setCoverMedium(String coverMedium) { this.coverMedium = coverMedium; }
    public void setCoverBig(String coverBig) { this.coverBig = coverBig; }
    public void setCoverXl(String coverXl) { this.coverXl = coverXl; }
    public void setTracklist(String tracklist) { this.tracklist = tracklist; }
    public void setNbTracks(int nbTracks) { this.nbTracks = nbTracks; }
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
        parcel.writeString(coverSmall);
        parcel.writeString(coverMedium);
        parcel.writeString(coverBig);
        parcel.writeString(coverXl);
        parcel.writeString(tracklist);
        parcel.writeInt(nbTracks);
        parcel.writeParcelable(artist, flags);
    }
}