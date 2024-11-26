package com.example.synesthesia;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.api.TmdbApiClient;
import com.example.synesthesia.api.TmdbApiService;
import com.example.synesthesia.models.TmdbMovie;
import com.example.synesthesia.models.TmdbMovieResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchMovieActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MoviesAdapter adapter;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_movies);
        Button searchButton = findViewById(R.id.searchButton);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchEditText = findViewById(R.id.searchEditText);

        searchButton.setOnClickListener(v -> performSearch(searchEditText.getText().toString()));
    }

    private void performSearch(String query) {
        TmdbApiService apiService = TmdbApiClient.getRetrofitInstance().create(TmdbApiService.class);
        apiService.searchMovies("f07ebbaf992b26f432b9ba90fa71ea8d", query, "fr-FR").enqueue(new Callback<TmdbMovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<TmdbMovieResponse> call, @NonNull Response<TmdbMovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TmdbMovie> movies = response.body().getMovies();
                    adapter = new MoviesAdapter(movies, SearchMovieActivity.this);
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
