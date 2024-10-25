package com.example.synesthesia.models;

import java.util.List;

public class ArtistResponse {
    private final List<Artist> data;

    public ArtistResponse(List<Artist> data) {
        this.data = data;
    }

    public List<Artist> getData() {
        return data;
    }

}
