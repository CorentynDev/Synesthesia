package com.example.synesthesia.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class musicDansAlbum implements Parcelable {
    @SerializedName("md5_image")
    private String md5Image;

    protected musicDansAlbum(Parcel in) {
        md5Image = in.readString();
    }

    public static final Creator<musicDansAlbum> CREATOR = new Creator<musicDansAlbum>() {
        @Override
        public musicDansAlbum createFromParcel(Parcel in) {
            return new musicDansAlbum(in);
        }

        @Override
        public musicDansAlbum[] newArray(int size) {
            return new musicDansAlbum[size];
        }
    };

    public String getImageMd5() {
        return md5Image;
    }

    public String getImageUrl() {
        return "https://e-cdns-images.dzcdn.net/images/cover/" + getImageMd5() + "/1000x1000-000000-80-0-0.jpg";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(md5Image);
    }
}