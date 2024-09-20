package com.example.synesthesia.api;

import com.example.synesthesia.models.AlbumResponse;
import com.example.synesthesia.models.ArtistResponse;
import com.example.synesthesia.models.TrackResponse; // Ajouter cette importation

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DeezerApi {

    @GET("/2.0/search/artist")
    Call<ArtistResponse> searchArtists(@Query("q") String query);

    @GET("/2.0/search/album")
    Call<AlbumResponse> searchAlbums(@Query("q") String query);

    @GET("/2.0/search/track")
    Call<TrackResponse> searchTracks(@Query("q") String query);
}
