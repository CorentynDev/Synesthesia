package com.example.synesthesia;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.api.GoogleBooksApi;
import com.example.synesthesia.dialogs.BookRecommendationDialog;
import com.example.synesthesia.models.Book;
import com.example.synesthesia.models.BooksResponse;

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
        setContentView(R.layout.activity_search_book); // Layout à créer plus tard

        // Initialisation de Retrofit pour Google Books API
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        googleBooksApi = retrofit.create(GoogleBooksApi.class);

        // Configuration de la RecyclerView pour afficher les résultats
        booksRecyclerView = findViewById(R.id.booksRecyclerView); // Doit être dans activity_search_book.xml
        booksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        EditText searchField = findViewById(R.id.searchField);
        Button searchButton = findViewById(R.id.searchButton);

        // Déclenche la recherche lorsque le bouton est cliqué
        searchButton.setOnClickListener(v -> {
            String query = searchField.getText().toString();
            searchBooks(query); // Méthode pour effectuer la recherche via l'API
        });
    }

    // Méthode pour rechercher des livres via Google Books API
    private void searchBooks(String query) {
        String apiKey = "AIzaSyDQm9NR9F8AbZtrAqxWcSnUdC87Dci-hP8";
        Call<BooksResponse> call = googleBooksApi.searchBooks(query, apiKey);
        call.enqueue(new Callback<BooksResponse>() {
            @Override
            public void onResponse(Call<BooksResponse> call, Response<BooksResponse> response) {
                if (response.isSuccessful()) {
                    List<Book> books = response.body().getItems();
                    // Initialisation de l'Adapter pour afficher les résultats
                    booksAdapter = new BooksAdapter(books, book -> openBookDetails(book)); // Méthode à ajouter
                    booksRecyclerView.setAdapter(booksAdapter);
                }
            }

            @Override
            public void onFailure(Call<BooksResponse> call, Throwable t) {
                Log.e("GoogleBooksAPI", "Error: " + t.getMessage());
            }
        });
    }

    // Méthode pour ouvrir une modale ou une autre activité avec les détails du livre sélectionné
    private void openBookDetails(Book book) {
        // Ouvrir une modale avec les détails du livre (à implémenter à l'étape suivante)
        BookRecommendationDialog dialog = BookRecommendationDialog.newInstance(book);
        dialog.show(getSupportFragmentManager(), "BookRecommendationDialog");
    }
}
