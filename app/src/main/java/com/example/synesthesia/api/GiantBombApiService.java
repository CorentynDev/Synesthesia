package com.example.synesthesia.api;

import com.example.synesthesia.models.GiantBombResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GiantBombApiService {
    // Requête pour rechercher des jeux vidéo
    @GET("search/")
    Call<GiantBombResponse> searchGames(
            @Query("api_key") String apiKey,
            @Query("format") String format,
            @Query("query") String query,
            @Query("resources") String resources // Specify "game" to focus on games
    );
}
