package com.example.synesthesia.models;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

public class Book implements Parcelable {
    private final String id; // ID au niveau supérieur
    private final VolumeInfo volumeInfo;

    // Constructeur pour Parcelable
    protected Book(Parcel in) {
        id = in.readString(); // Lire l'ID au niveau supérieur
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
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id); // Écrire l'ID
        parcel.writeParcelable(volumeInfo, flags);
    }

    // Getter pour l'ID
    public String getId() {
        return id;
    }

    // Getter pour VolumeInfo
    public VolumeInfo getVolumeInfo() {
        return volumeInfo;
    }

    public static class VolumeInfo implements Parcelable {
        private final String title;
        private final List<String> authors;
        private final String publishedDate;
        private final ImageLinks imageLinks;
        private final String description;

        // Constructeur pour Parcelable
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
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeString(title);
            parcel.writeStringList(authors);
            parcel.writeString(publishedDate);
            parcel.writeParcelable(imageLinks, flags);
            parcel.writeString(description);
        }

        // Getters pour chaque champ
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

        // Constructeur pour Parcelable
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
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeString(thumbnail);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        // Getter pour Thumbnail
        public String getThumbnail() {
            return thumbnail;
        }
    }
}
