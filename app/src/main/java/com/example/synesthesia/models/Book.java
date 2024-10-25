package com.example.synesthesia.models;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

public class Book implements Parcelable {
    private final VolumeInfo volumeInfo;

    protected Book(Parcel in) {
        volumeInfo = in.readParcelable(VolumeInfo.class.getClassLoader());
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(volumeInfo, i);
    }

    public VolumeInfo getVolumeInfo() {
        return volumeInfo;
    }

    public static class VolumeInfo implements Parcelable {
        private final String title;
        private final List<String> authors;
        private final String publishedDate;
        private final ImageLinks imageLinks;
        private final String description;

        protected VolumeInfo(Parcel in) {
            title = in.readString();
            authors = in.createStringArrayList();
            publishedDate = in.readString();
            imageLinks = in.readParcelable(ImageLinks.class.getClassLoader());
            description = in.readString();
        }

        public static final Creator<VolumeInfo> CREATOR = new Creator<VolumeInfo>() {
            @Override
            public VolumeInfo createFromParcel(Parcel in) {
                return new VolumeInfo(in);
            }

            @Override
            public VolumeInfo[] newArray(int size) {
                return new VolumeInfo[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(title);
            parcel.writeStringList(authors);
            parcel.writeString(publishedDate);
            parcel.writeParcelable(imageLinks, i);
            parcel.writeString(description);
        }

        public String getTitle() {
            return title;
        }

        public List<String> getAuthors() {
            return authors;
        }

        public String getPublishedDate() {
            return publishedDate;
        }

        public ImageLinks getImageLinks() {
            return imageLinks;
        }

        public String getDescription() {
            return description;
        }

    }

    public static class ImageLinks implements Parcelable {
        private final String thumbnail;

        protected ImageLinks(Parcel in) {
            thumbnail = in.readString();
        }

        public static final Creator<ImageLinks> CREATOR = new Creator<ImageLinks>() {
            @Override
            public ImageLinks createFromParcel(Parcel in) {
                return new ImageLinks(in);
            }

            @Override
            public ImageLinks[] newArray(int size) {
                return new ImageLinks[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(thumbnail);
        }

        public String getThumbnail() {
            return thumbnail;
        }

    }
}
