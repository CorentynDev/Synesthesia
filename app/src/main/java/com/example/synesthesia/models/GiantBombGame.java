package com.example.synesthesia.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GiantBombGame implements Parcelable {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("deck")
    private String description;

    @SerializedName("image")
    private GiantBombImage image;

    @SerializedName("developers")
    private List<String> developers;

    @SerializedName("original_release_date")
    private String originalReleaseDate;

    // Constructeur pour le Parcelable
    protected GiantBombGame(@NonNull Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        image = in.readParcelable(GiantBombImage.class.getClassLoader());
        developers = in.createStringArrayList(); // Récupère la liste des développeurs
        originalReleaseDate = in.readString();   // Récupère la date de sortie
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

    // Getters
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

    public List<String> getDevelopers() {
        return developers;
    }

    public String getOriginalReleaseDate() {
        return originalReleaseDate;
    }

    // Méthodes Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeParcelable(image, flags); // On inclut GiantBombImage
        parcel.writeStringList(developers); // Ajout des développeurs
        parcel.writeString(originalReleaseDate); // Ajout de la date
    }

    // Classe interne pour gérer les images
    public static class GiantBombImage implements Parcelable {
        @SerializedName("medium_url")
        private String mediumUrl;

        public String getMediumUrl() {
            return mediumUrl;
        }

        // Constructeur Parcelable
        protected GiantBombImage(Parcel in) {
            mediumUrl = in.readString();
        }

        public static final Creator<GiantBombImage> CREATOR = new Creator<GiantBombImage>() {
            @Override
            public GiantBombImage createFromParcel(Parcel in) {
                return new GiantBombImage(in);
            }

            @Override
            public GiantBombImage[] newArray(int size) {
                return new GiantBombImage[size];
            }
        };

        @Override
        public void writeToParcel(@NonNull Parcel parcel, int flags) {
            parcel.writeString(mediumUrl);
        }

        @Override
        public int describeContents() {
            return 0;
        }
    }
}
