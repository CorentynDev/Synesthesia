package com.example.synesthesia.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class Artist implements Parcelable {
    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("picture")
    private String picture;

    // Constructeur par défaut
    public Artist() {}

    // Constructeur pour Parcelable
    protected Artist(Parcel in) {
        id = in.readString();
        name = in.readString();
        picture = in.readString();
    }

    public static final Creator<Artist> CREATOR = new Creator<Artist>() {
        @Override
        public Artist createFromParcel(Parcel in) {
            return new Artist(in);
        }

        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getImageUrl() { return picture; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setImageUrl(String picture) { this.picture = picture; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(picture);
    }
}
