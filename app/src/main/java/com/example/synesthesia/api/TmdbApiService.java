package com.example.synesthesia.api;

import com.example.synesthesia.models.TmdbMovieResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TmdbApiService {
    // Requête pour rechercher des films
    @GET("search/movie")
    Call<TmdbMovieResponse> searchMovies(
            @Query("api_key") String apiKey,
            @Query("query") String query
    );
}
