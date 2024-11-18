package com.example.synesthesia;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.synesthesia.api.GoogleBooksApi;
import com.example.synesthesia.dialogs.BookRecommendationDialog;
import com.example.synesthesia.models.Book;
import com.example.synesthesia.models.BooksResponse;
import com.example.synesthesia.utilities.FooterUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchBookActivity extends AppCompatActivity {

    private GoogleBooksApi googleBooksApi;
    private RecyclerView booksRecyclerView;
    private BooksAdapter booksAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_book);

        FooterUtils.setupFooter(this, R.id.createRecommendationButton);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        googleBooksApi = retrofit.create(GoogleBooksApi.class);

        booksRecyclerView = findViewById(R.id.booksRecyclerView);
        booksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        EditText searchField = findViewById(R.id.searchField);
        Button searchButton = findViewById(R.id.searchButton);

        // Mise en focus automatique et affichage du clavier
        searchField.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        booksRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Glide.with(SearchBookActivity.this).resumeRequests();
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Glide.with(SearchBookActivity.this).resumeRequests();
                } else {
                    Glide.with(SearchBookActivity.this).pauseRequests();
                }
            }
        });

        searchButton.setOnClickListener(v -> {
            String query = searchField.getText().toString();
            searchBooks(query);
        });
        searchField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) { // Vérifie si l'action est "Done" (OK)
                String query = searchField.getText().toString();
                searchBooks(query); // Appelle la recherche
                return true; // Action gérée
            }
            return false;
        });
    }

    private void searchBooks(String query) {
        String apiKey = "AIzaSyDQm9NR9F8AbZtrAqxWcSnUdC87Dci-hP8";
        Call<BooksResponse> call = googleBooksApi.searchBooks(query, apiKey);
        call.enqueue(new Callback<BooksResponse>() {
            @Override
            public void onResponse(@NonNull Call<BooksResponse> call, @NonNull Response<BooksResponse> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    List<Book> books = response.body().getItems();
                    booksAdapter = new BooksAdapter(books, SearchBookActivity.this);
                    booksRecyclerView.setAdapter(booksAdapter);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BooksResponse> call, @NonNull Throwable t) {
                Log.e("GoogleBooksAPI", "Error: " + t.getMessage());
            }
        });
    }
}
