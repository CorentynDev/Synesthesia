package com.example.synesthesia.api;

import com.example.synesthesia.models.CreditsResponse;
import com.example.synesthesia.models.GenreResponse;
import com.example.synesthesia.models.TmdbMovieResponse;
import com.example.synesthesia.models.VideoResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TmdbApiService {
    @GET("search/movie")
    Call<TmdbMovieResponse> searchMovies(
            @Query("api_key") String apiKey,
            @Query("query") String query,
            @Query("language") String language
    );

    @GET("genre/movie/list")
    Call<GenreResponse> getGenres(
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    @GET("movie/{movie_id}/videos")
    Call<VideoResponse> getMovieVideos(
            @Path("movie_id") String movieId,
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    @GET("movie/{movie_id}/credits")
    Call<CreditsResponse> getMovieCredits(
            @Path("movie_id") String movieId,
            @Query("api_key") String apiKey,
            @Query("language") String language
    );
}
