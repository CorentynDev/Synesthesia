package com.example.synesthesia;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.synesthesia.api.IgdbService;
import com.example.synesthesia.models.Game;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchGameActivity extends AppCompatActivity {

    private IgdbService igdbService;
    private RecyclerView gamesRecyclerView;
    private GamesAdapter gamesAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_game); // Layout à créer

        // Initialisation de Retrofit pour IGDB
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.igdb.com/v4/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        String accessToken = "votre_token_d_access";  // Token d'accès
        igdbService = retrofit.create(IgdbService.class);

        // Configuration de la RecyclerView pour afficher les résultats
        gamesRecyclerView = findViewById(R.id.gamesRecyclerView);
        gamesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        EditText searchField = findViewById(R.id.searchField);
        Button searchButton = findViewById(R.id.searchButton);

        searchButton.setOnClickListener(v -> {
            String query = searchField.getText().toString();
            searchGames(query);
        });
    }

    private void searchGames(String query) {
        String igdbQuery = "fields name,cover.url,released; search \"" + query + "\";";
        Call<List<Game>> call = igdbService.getGames(igdbQuery);
        call.enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                if (response.isSuccessful()) {
                    List<Game> games = response.body();
                    gamesAdapter = new GamesAdapter(games, SearchGameActivity.this);
                    gamesRecyclerView.setAdapter(gamesAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                Log.e("IGDB", "Error: " + t.getMessage());
            }
        });
    }
}
