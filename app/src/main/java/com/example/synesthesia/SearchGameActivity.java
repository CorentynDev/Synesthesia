package com.example.synesthesia;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.adapters.GamesAdapter;
import com.example.synesthesia.api.GiantBombApiClient;
import com.example.synesthesia.api.GiantBombApiService;
import com.example.synesthesia.models.GiantBombGame;
import com.example.synesthesia.models.GiantBombResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchGameActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GamesAdapter adapter;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_games);
        Button searchButton = findViewById(R.id.searchButton);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchEditText = findViewById(R.id.searchEditText);

        searchButton.setOnClickListener(v -> performSearch(searchEditText.getText().toString()));
    }

    private void performSearch(String query) {
        GiantBombApiService apiService = GiantBombApiClient.getRetrofitInstance().create(GiantBombApiService.class);
        apiService.searchGames("d9d7b71c1e1dc8970f331f1de97007a295bd8282", "json", query, "game").enqueue(new Callback<GiantBombResponse>() {
            @Override
            public void onResponse(@NonNull Call<GiantBombResponse> call, @NonNull Response<GiantBombResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.i("API Response", String.valueOf(response));
                    List<GiantBombGame> games = response.body().getGames();
                    adapter = new GamesAdapter(games, SearchGameActivity.this);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<GiantBombResponse> call, Throwable t) {
                // Gérer l'erreur
            }
        });
    }
}
