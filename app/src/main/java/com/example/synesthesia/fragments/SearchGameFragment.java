package com.example.synesthesia.fragments;

import android.os.Bundle;
import android.util.Log;
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
import com.example.synesthesia.adapters.GamesAdapter;
import com.example.synesthesia.api.GiantBombApiClient;
import com.example.synesthesia.api.GiantBombApiService;
import com.example.synesthesia.models.GiantBombGame;
import com.example.synesthesia.models.GiantBombResponse;
import com.example.synesthesia.utilities.FooterUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchGameFragment extends Fragment {

    private RecyclerView recyclerView;
    private GamesAdapter adapter;
    private EditText searchEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_game, container, false);
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
        GiantBombApiService apiService = GiantBombApiClient.getRetrofitInstance().create(GiantBombApiService.class);
        apiService.searchGames("d9d7b71c1e1dc8970f331f1de97007a295bd8282", "json", query, "game").enqueue(new Callback<GiantBombResponse>() {
            @Override
            public void onResponse(@NonNull Call<GiantBombResponse> call, @NonNull Response<GiantBombResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.i("API Response", String.valueOf(response));
                    List<GiantBombGame> games = response.body().getGames();
                    adapter = new GamesAdapter(games, getContext());
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(@NonNull Call<GiantBombResponse> call, @NonNull Throwable t) {
                // Gérer l'erreur
                Log.e("API Error", t.getMessage());
            }
        });
    }
}
