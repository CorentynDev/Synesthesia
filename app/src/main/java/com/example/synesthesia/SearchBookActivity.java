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
        int maxResults = 40;  // Limite de résultats par requête
        int startIndex = 0;   // L'index de départ (pour la pagination)

        fetchBooks(query, apiKey, maxResults, startIndex);
    }

    private void fetchBooks(String query, String apiKey, int maxResults, int startIndex) {
        // Faire une requête API pour récupérer les livres
        Call<BooksResponse> call = googleBooksApi.searchBooks(query, apiKey, maxResults, startIndex);
        call.enqueue(new Callback<BooksResponse>() {
            @Override
            public void onResponse(@NonNull Call<BooksResponse> call, @NonNull Response<BooksResponse> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    List<Book> books = response.body().getItems();

                    if (books != null && !books.isEmpty()) {
                        // Si la liste des livres est vide, on arrête la pagination
                        if (booksAdapter == null) {
                            booksAdapter = new BooksAdapter(books, SearchBookActivity.this);
                            booksRecyclerView.setAdapter(booksAdapter);
                        } else {
                            // Si des livres sont déjà affichés, on les ajoute à la liste
                            booksAdapter.addBooks(books);
                        }

                        // Si nous avons récupéré 40 livres, on fait une autre requête pour obtenir plus de livres
                        if (books.size() == maxResults) {
                            // On fait une nouvelle requête avec l'index suivant
                            fetchBooks(query, apiKey, maxResults, startIndex + maxResults);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<BooksResponse> call, @NonNull Throwable t) {
                Log.e("GoogleBooksAPI", "Error: " + t.getMessage());
            }
        });
    }
}
