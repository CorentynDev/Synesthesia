package com.example.synesthesia.api;

import com.example.synesthesia.models.AlbumResponse;
import com.example.synesthesia.models.ArtistResponse;
import com.example.synesthesia.models.TrackResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface DeezerApi {

    // Exemple pour rechercher des artistes
    @GET("search/artist")
    Call<ArtistResponse> searchArtists(@Query("q") String query);

    // Exemple pour rechercher des albums
    @GET("search/album")
    Call<AlbumResponse> searchAlbums(@Query("q") String query);

    // Exemple pour rechercher des pistes
    @GET("search/track")
    Call<TrackResponse> searchTracks(@Query("q") String query);

    // Méthode pour obtenir les pistes d'un album en utilisant l'URL relative
    @GET
    Call<TrackResponse> getAlbumTracks(@Url String url);

    // Autres méthodes API si nécessaire
}
