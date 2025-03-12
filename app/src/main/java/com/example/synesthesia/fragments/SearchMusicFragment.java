package com.example.synesthesia.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.MainActivity;
import com.example.synesthesia.R;
import com.example.synesthesia.adapters.MusicAdapter;
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

public class SearchMusicFragment extends Fragment {

    private MusicAdapter musicAdapter;
    private EditText searchField;
    private RadioGroup searchTypeGroup;
    private DeezerApi deezerApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_music, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FooterUtils.setupFooter(requireActivity(), R.id.createRecommendationButton);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.deezer.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        deezerApi = retrofit.create(DeezerApi.class);

        RecyclerView resultsRecyclerView = view.findViewById(R.id.resultsRecyclerView);
        searchField = view.findViewById(R.id.searchField);
        Button searchButton = view.findViewById(R.id.searchButton);
        searchTypeGroup = view.findViewById(R.id.searchTypeGroup);

        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        musicAdapter = new MusicAdapter(new ArrayList<>(), getContext());
        resultsRecyclerView.setAdapter(musicAdapter);

        searchField.requestFocus();
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        musicAdapter.setOnItemClickListener(item -> {
            if (item instanceof Artist) {
                Artist artist = (Artist) item;
                Log.d("SearchMusic", "Clic sur l'artiste : " + artist.getName());

                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showArtistDetailsFragment(artist);
                }
            } else if (item instanceof Album) {
                Album album = (Album) item;
                Log.d("SearchMusic", "Clicked on album: " + album.getTitle());
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showAlbumDetailsFragment(album);
                }
            } else if (item instanceof Track) {
                Track track = (Track) item;
                Log.d("SearchMusic", "Clicked on track: " + track.getTitle());
            }
        });

        searchField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch();
                return true;
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
    public void onPause() {
        super.onPause();
        if (musicAdapter != null) {
            musicAdapter.resetPlayer();
        }
    }
}
