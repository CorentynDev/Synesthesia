package com.example.synesthesia.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Game {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("released")
    private String released;

    @SerializedName("cover")
    private Cover cover;

    // Ajoutez d'autres attributs selon les champs retournés par l'API IGDB
    // par exemple des screenshots, des genres, etc.

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getReleased() {
        return released;
    }

    public Cover getCover() {
        return cover;
    }

    public static class Cover {
        @SerializedName("url")
        private String url;

        public String getUrl() {
            return url;
        }
    }
}
