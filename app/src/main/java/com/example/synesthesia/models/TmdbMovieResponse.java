package com.example.synesthesia.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TmdbMovieResponse {
    @SerializedName("results")
    private List<TmdbMovie> movies;

    public List<TmdbMovie> getMovies() {
        return movies;
    }
}
