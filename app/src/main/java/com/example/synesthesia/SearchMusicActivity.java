package com.example.synesthesia;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class SearchMusicActivity extends AppCompatActivity {

    private RecyclerView resultsRecyclerView;
    private MusicAdapter musicAdapter;
    private EditText searchField;
    private Button searchButton;
    private RadioGroup searchTypeGroup;
    private DeezerApi deezerApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_music);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.deezer.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        deezerApi = retrofit.create(DeezerApi.class);

        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        searchField = findViewById(R.id.searchField);
        searchButton = findViewById(R.id.searchButton);
        searchTypeGroup = findViewById(R.id.searchTypeGroup);

        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        musicAdapter = new MusicAdapter(new ArrayList<>(), this);
        resultsRecyclerView.setAdapter(musicAdapter);

        musicAdapter.setOnItemClickListener(item -> {
            if (item instanceof Artist) {
                Artist artist = (Artist) item;
                Log.d("SearchMusic", "Clicked on artist: " + artist.getName());
            } else if (item instanceof Album) {
                Album album = (Album) item;
                Log.d("SearchMusic", "Clicked on album: " + album.getTitle());
            } else if (item instanceof Track) {
                Track track = (Track) item;
                Log.d("SearchMusic", "Clicked on track: " + track.getTitle());
            }
        });


        searchButton.setOnClickListener(v -> performSearch());
    }

    private void performSearch() {
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
            public void onResponse(Call<ArtistResponse> call, Response<ArtistResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    musicAdapter.updateArtists(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<ArtistResponse> call, Throwable t) {
                Log.e("SearchMusic", "Failed to fetch artists", t);
            }
        });
    }

    private void searchAlbums(String query) {
        Call<AlbumResponse> call = deezerApi.searchAlbums(query);
        call.enqueue(new Callback<AlbumResponse>() {
            @Override
            public void onResponse(Call<AlbumResponse> call, Response<AlbumResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    musicAdapter.updateAlbums(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<AlbumResponse> call, Throwable t) {
                Log.e("SearchMusic", "Failed to fetch albums", t);
            }
        });
    }

    private void searchTracks(String query) {
        Call<TrackResponse> call = deezerApi.searchTracks(query);
        call.enqueue(new Callback<TrackResponse>() {
            @Override
            public void onResponse(Call<TrackResponse> call, Response<TrackResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    musicAdapter.updateTracks(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<TrackResponse> call, Throwable t) {
                Log.e("SearchMusic", "Failed to fetch tracks", t);
            }
        });
    }
}
