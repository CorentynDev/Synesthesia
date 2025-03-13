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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.synesthesia.R;
import com.example.synesthesia.adapters.BooksAdapter;
import com.example.synesthesia.api.GoogleBooksApi;
import com.example.synesthesia.models.Book;
import com.example.synesthesia.models.BooksResponse;
import com.example.synesthesia.utilities.FooterUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchBookFragment extends Fragment {

    private GoogleBooksApi googleBooksApi;
    private RecyclerView booksRecyclerView;
    private BooksAdapter booksAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_book, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FooterUtils.setupFooter(requireActivity(), R.id.createRecommendationButton);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        googleBooksApi = retrofit.create(GoogleBooksApi.class);

        booksRecyclerView = view.findViewById(R.id.booksRecyclerView);
        booksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        EditText searchField = view.findViewById(R.id.searchField);
        Button searchButton = view.findViewById(R.id.searchButton);

        searchField.requestFocus();
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        booksRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Glide.with(SearchBookFragment.this).resumeRequests();
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Glide.with(SearchBookFragment.this).resumeRequests();
                } else {
                    Glide.with(SearchBookFragment.this).pauseRequests();
                }
            }
        });

        searchButton.setOnClickListener(v -> {
            String query = searchField.getText().toString();
            searchBooks(query);
        });

        searchField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String query = searchField.getText().toString();
                searchBooks(query);
                return true;
            }
            return false;
        });
    }

    private void searchBooks(String query) {
        String apiKey = "AIzaSyDQm9NR9F8AbZtrAqxWcSnUdC87Dci-hP8";
        int maxResults = 40;
        int startIndex = 0;

        if (booksAdapter != null) {
            booksAdapter.clearBooks();
        }

        fetchBooks(query, apiKey, maxResults, startIndex);
    }

    private void fetchBooks(String query, String apiKey, int maxResults, int startIndex) {
        Call<BooksResponse> call = googleBooksApi.searchBooks(query, apiKey, maxResults, startIndex);
        call.enqueue(new Callback<BooksResponse>() {
            @Override
            public void onResponse(@NonNull Call<BooksResponse> call, @NonNull Response<BooksResponse> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    List<Book> books = response.body().getItems();

                    if (books != null && !books.isEmpty()) {
                        if (booksAdapter == null) {
                            booksAdapter = new BooksAdapter(books, getContext());
                            booksRecyclerView.setAdapter(booksAdapter);
                        } else {
                            booksAdapter.addBooks(books);
                        }

                        if (books.size() == maxResults) {
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
