package com.example.synesthesia.api;

import com.example.synesthesia.models.Game;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IgdbService {
    @POST("games")
    Call<List<Game>> getGames(@Body String query);  // Utilisez un modèle pour les réponses des jeux
}
