package com.example.synesthesia.models;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

public class Book implements Parcelable {
    private VolumeInfo volumeInfo;

    public Book(VolumeInfo volumeInfo) {
        this.volumeInfo = volumeInfo;
    }

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

    public void setVolumeInfo(VolumeInfo volumeInfo) {
        this.volumeInfo = volumeInfo;
    }

    // Classe VolumeInfo
    public static class VolumeInfo implements Parcelable {
        private String title;
        private List<String> authors;
        private String publishedDate;
        private ImageLinks imageLinks;
        private String description;  // Ajouté pour la description du livre

        protected VolumeInfo(Parcel in) {
            title = in.readString();
            authors = in.createStringArrayList();
            publishedDate = in.readString();
            imageLinks = in.readParcelable(ImageLinks.class.getClassLoader());
            description = in.readString();  // Ajouté pour lire la description
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
            parcel.writeString(description);  // Ajouté pour écrire la description
        }

        // Getters et Setters
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<String> getAuthors() {
            return authors;
        }

        public void setAuthors(List<String> authors) {
            this.authors = authors;
        }

        public String getPublishedDate() {
            return publishedDate;
        }

        public void setPublishedDate(String publishedDate) {
            this.publishedDate = publishedDate;
        }

        public ImageLinks getImageLinks() {
            return imageLinks;
        }

        public void setImageLinks(ImageLinks imageLinks) {
            this.imageLinks = imageLinks;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    // Classe ImageLinks
    public static class ImageLinks implements Parcelable {
        private String thumbnail;

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

        // Getters et Setters
        public String getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
        }
    }
}
