package com.example.synesthesia.api;

import com.example.synesthesia.models.BooksResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleBooksApi {
    @GET("/books/v1/volumes")
    Call<BooksResponse> searchBooks(@Query("q") String query, @Query("key") String apiKey);
}

//TODO: Add the system to have more than 10 books in the list