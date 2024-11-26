package com.example.synesthesia.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GenreResponse {
    @SerializedName("genres")
    private List<Genre> genres;

    public List<Genre> getGenres() {
        return genres;
    }
}
