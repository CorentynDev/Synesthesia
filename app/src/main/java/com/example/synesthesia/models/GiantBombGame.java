package com.example.synesthesia.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class GiantBombGame implements Parcelable {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("deck")
    private String description;

    @SerializedName("image")
    private GiantBombImage image;

    protected GiantBombGame(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
    }

    public static final Creator<GiantBombGame> CREATOR = new Creator<GiantBombGame>() {
        @Override
        public GiantBombGame createFromParcel(Parcel in) {
            return new GiantBombGame(in);
        }

        @Override
        public GiantBombGame[] newArray(int size) {
            return new GiantBombGame[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public GiantBombImage getImage() {
        return image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(description);
    }

    public static class GiantBombImage {
        @SerializedName("medium_url")
        private String mediumUrl;

        public String getMediumUrl() {
            return mediumUrl;
        }
    }
}
