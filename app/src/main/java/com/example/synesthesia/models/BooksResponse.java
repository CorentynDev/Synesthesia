package com.example.synesthesia.models;

import java.util.List;

public class BooksResponse {
    private List<Book> items;

    public List<Book> getItems() {
        return items;
    }

    public void setItems(List<Book> items) {
        this.items = items;
    }
}
