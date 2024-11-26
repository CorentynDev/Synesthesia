package com.example.synesthesia.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TmdbMovie implements Parcelable {

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("overview")
    private String overview;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("release_date")
    private String releaseDate; // Date de sortie du film

    @SerializedName("genre_ids")
    private List<Integer> genreIds; // IDs des genres retournés par l'API

    private List<String> genres; // Noms des genres (résolus dynamiquement)

    private List<String> actors; // Liste des acteurs principaux

    @SerializedName("trailer_url")
    private String trailerUrl; // URL de la bande-annonce, résolue dynamiquement

    @SerializedName("director")
    private String director;

    // Constructeur pour Parcelable
    public TmdbMovie(Parcel in) {
        id = in.readString();
        title = in.readString();
        overview = in.readString();
        posterPath = in.readString();
        releaseDate = in.readString();
        genreIds = in.readArrayList(Integer.class.getClassLoader());
        genres = in.createStringArrayList();
        actors = in.createStringArrayList();
        trailerUrl = in.readString();
        director = in.readString();
    }

    // Méthodes pour Parcelable
    public static final Creator<TmdbMovie> CREATOR = new Creator<TmdbMovie>() {
        @Override
        public TmdbMovie createFromParcel(Parcel in) {
            return new TmdbMovie(in);
        }

        @Override
        public TmdbMovie[] newArray(int size) {
            return new TmdbMovie[size];
        }
    };

    // Getters et Setters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public List<Integer> getGenreIds() {
        return genreIds;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public List<String> getActors() {
        return actors;
    }

    public void setActors(List<String> actors) {
        this.actors = actors;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    // Méthodes Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(overview);
        parcel.writeString(posterPath);
        parcel.writeString(releaseDate);
        parcel.writeList(genreIds);
        parcel.writeStringList(genres);
        parcel.writeStringList(actors);
        parcel.writeString(trailerUrl);
        parcel.writeString(director);
    }
}
