package com.example.synesthesia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.api.DeezerApi;
import com.example.synesthesia.models.Album;
import com.example.synesthesia.models.AlbumResponse;
import com.example.synesthesia.models.Artist;
import com.example.synesthesia.models.ArtistResponse;
import com.example.synesthesia.models.Track;
import com.example.synesthesia.models.TrackResponse;
import com.example.synesthesia.utilities.FooterUtils;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class SearchMusicActivity extends AppCompatActivity {

    private MusicAdapter musicAdapter;
    private EditText searchField;
    private RadioGroup searchTypeGroup;
    private DeezerApi deezerApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_music);

        FooterUtils.setupFooter(this, R.id.createRecommendationButton);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.deezer.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        deezerApi = retrofit.create(DeezerApi.class);

        RecyclerView resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        searchField = findViewById(R.id.searchField);
        Button searchButton = findViewById(R.id.searchButton);
        searchTypeGroup = findViewById(R.id.searchTypeGroup);

        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        musicAdapter = new MusicAdapter(new ArrayList<>(), this);
        resultsRecyclerView.setAdapter(musicAdapter);

        // Mise en focus automatique et affichage du clavier
        searchField.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);


        musicAdapter.setOnItemClickListener(item -> {
            if (item instanceof Artist) {
                if (item instanceof Artist) {
                    Artist artist = (Artist) item;
                    Log.d("SearchMusic", "Clic sur l'artiste : " + artist.getName());
                    Intent intent = new Intent(SearchMusicActivity.this, ArtistDetailsActivity.class);
                    intent.putExtra("artist", artist);
                    startActivity(intent);
                }
            } else if (item instanceof Album) {
                Album album = (Album) item;
                Log.d("SearchMusic", "Clicked on album: " + album.getTitle());
                Intent intent = new Intent(SearchMusicActivity.this, AlbumDetailsActivity.class);
                intent.putExtra("album", album);
                startActivity(intent);
            } else if (item instanceof Track) {
                Track track = (Track) item;
                Log.d("SearchMusic", "Clicked on track: " + track.getTitle());
            }
        });

        searchField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) { // Vérifie si l'action est "Done" (OK)
                performSearch(); // Appelle la recherche
                return true; // Action gérée
            }
            return false;
        });

        searchButton.setOnClickListener(v -> performSearch());
    }

    private void performSearch() {
        musicAdapter.resetPlayer();

        String query = searchField.getText().toString().trim();
        if (query.isEmpty()) {
            return;
        }

        int selectedSearchTypeId = searchTypeGroup.getCheckedRadioButtonId();
        if (selectedSearchTypeId == R.id.searchArtistRadioButton) {
            searchArtists(query);
        } else if (selectedSearchTypeId == R.id.searchAlbumRadioButton) {
            searchAlbums(query);
        } else if (selectedSearchTypeId == R.id.searchTrackRadioButton) {
            searchTracks(query);
        }
    }

    private void searchArtists(String query) {
        Call<ArtistResponse> call = deezerApi.searchArtists(query);
        call.enqueue(new Callback<ArtistResponse>() {
            @Override
            public void onResponse(@NonNull Call<ArtistResponse> call, @NonNull Response<ArtistResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    musicAdapter.updateArtists(response.body().getData());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ArtistResponse> call, @NonNull Throwable t) {
                Log.e("SearchMusic", "Failed to fetch artists", t);
            }
        });
    }

    private void searchAlbums(String query) {
        Call<AlbumResponse> call = deezerApi.searchAlbums(query);
        call.enqueue(new Callback<AlbumResponse>() {
            @Override
            public void onResponse(@NonNull Call<AlbumResponse> call, @NonNull Response<AlbumResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    musicAdapter.updateAlbums(response.body().getData());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AlbumResponse> call, @NonNull Throwable t) {
                Log.e("SearchMusic", "Failed to fetch albums", t);
            }
        });
    }

    private void searchTracks(String query) {
        Call<TrackResponse> call = deezerApi.searchTracks(query);
        call.enqueue(new Callback<TrackResponse>() {
            @Override
            public void onResponse(@NonNull Call<TrackResponse> call, @NonNull Response<TrackResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    musicAdapter.updateTracks(response.body().getData());
                }
            }

            @Override
            public void onFailure(@NonNull Call<TrackResponse> call, @NonNull Throwable t) {
                Log.e("SearchMusic", "Failed to fetch tracks", t);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (musicAdapter != null) {
            musicAdapter.resetPlayer(); // Arrêtez le lecteur audio
        }
    }
}
