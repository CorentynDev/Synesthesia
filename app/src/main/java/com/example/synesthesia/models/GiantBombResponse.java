package com.example.synesthesia.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GiantBombResponse {
    @SerializedName("results")
    private List<GiantBombGame> games;

    public List<GiantBombGame> getGames() {
        return games;
    }
}
