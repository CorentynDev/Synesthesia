package com.example.synesthesia.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DeezerApiClient {
    private static final String BASE_URL = "https://api.deezer.com/";

    private static Retrofit retrofit;

    public static DeezerApi getDeezerApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(DeezerApi.class);
    }
}
