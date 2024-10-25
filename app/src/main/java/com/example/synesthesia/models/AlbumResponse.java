package com.example.synesthesia.models;

import java.util.List;

public class AlbumResponse {
    private final List<Album> data;

    public AlbumResponse(List<Album> data) {
        this.data = data;
    }

    public List<Album> getData() {
        return data;
    }

}
