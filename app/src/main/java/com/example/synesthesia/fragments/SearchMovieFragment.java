package com.example.synesthesia.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.R;
import com.example.synesthesia.adapters.MoviesAdapter;
import com.example.synesthesia.api.TmdbApiClient;
import com.example.synesthesia.api.TmdbApiService;
import com.example.synesthesia.models.TmdbMovie;
import com.example.synesthesia.models.TmdbMovieResponse;
import com.example.synesthesia.utilities.FooterUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchMovieFragment extends Fragment {

    private RecyclerView recyclerView;
    private MoviesAdapter adapter;
    private EditText searchEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_movie, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FooterUtils.setupFooter(requireActivity(), R.id.createRecommendationButton);

        Button searchButton = view.findViewById(R.id.searchButton);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchEditText = view.findViewById(R.id.searchEditText);

        searchButton.setOnClickListener(v -> performSearch(searchEditText.getText().toString()));
    }

    private void performSearch(String query) {
        TmdbApiService apiService = TmdbApiClient.getRetrofitInstance().create(TmdbApiService.class);
        apiService.searchMovies("f07ebbaf992b26f432b9ba90fa71ea8d", query, "fr-FR").enqueue(new Callback<TmdbMovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<TmdbMovieResponse> call, @NonNull Response<TmdbMovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TmdbMovie> movies = response.body().getMovies();
                    adapter = new MoviesAdapter(movies, getContext());
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(@NonNull Call<TmdbMovieResponse> call, @NonNull Throwable t) {
                // Gérer l'erreur
            }
        });
    }
}
