package com.example.synesthesia.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.google.gson.annotations.SerializedName;

public class Track implements Parcelable {

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("artist")
    private Artist artist;

    @SerializedName("album")
    private Album album;

    @SerializedName("preview")
    private String previewUrl;

    @SerializedName("duration")
    private int duration;

    // Constructeur par défaut
    public Track() {}

    // Constructeur pour Parcelable
    protected Track(Parcel in) {
        id = in.readString();
        title = in.readString();
        artist = in.readParcelable(Artist.class.getClassLoader());
        album = in.readParcelable(Album.class.getClassLoader());
        previewUrl = in.readString();
        duration = in.readInt();
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

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtistName() {
        return artist != null ? artist.getName() : null;
    }

    public Artist getArtist() {  // Assurez-vous que cette méthode existe et est publique
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public Album getAlbum() {
        return album ;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public String getPreviewUrl() {
        Log.d("Track", "Preview URL: " + previewUrl);
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    // Méthode pour obtenir la durée formatée (minutes:secondes)
    public String getFormattedDuration() {
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%d:%02d", minutes, seconds); // Format MM:SS
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeParcelable(artist, flags);
        parcel.writeParcelable(album, flags);
        parcel.writeString(previewUrl);
        parcel.writeInt(duration);
    }
}
