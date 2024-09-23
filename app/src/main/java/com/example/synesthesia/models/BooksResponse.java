package com.example.synesthesia.models;

import java.util.List;

public class BooksResponse {
    private final List<Book> items;

    public BooksResponse(List<Book> items) {
        this.items = items;
    }

    public List<Book> getItems() {
        return items;
    }
}
