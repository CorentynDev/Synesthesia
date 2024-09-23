package com.example.synesthesia;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.synesthesia.api.DeezerApi;
import com.example.synesthesia.models.Album;
import com.example.synesthesia.models.Track;
import com.example.synesthesia.models.TrackResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AlbumDetailsActivity extends AppCompatActivity {

    private static final String TAG = "AlbumDetailsActivity";

    private ImageView albumCoverImageView;
    private TextView albumTitleTextView;
    private TextView albumArtistTextView;
    private TextView albumTracksCountTextView;
    private RecyclerView tracksRecyclerView;
    private Button backButton;

    private Album album;
    private TracksAdapter tracksAdapter;

    private DeezerApi deezerApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);

        // Initialiser les vues
        albumCoverImageView = findViewById(R.id.albumCoverImageView);
        albumTitleTextView = findViewById(R.id.albumTitleTextView);
        albumArtistTextView = findViewById(R.id.albumArtistTextView);
        albumTracksCountTextView = findViewById(R.id.albumTracksCountTextView);
        tracksRecyclerView = findViewById(R.id.tracksRecyclerView);
        backButton = findViewById(R.id.backButton);

        // Récupérer l'objet Album depuis l'intent
        album = getIntent().getParcelableExtra("album");
        if (album == null) {
            Log.e(TAG, "Album object is null");
            Toast.makeText(this, "Erreur: Aucun album sélectionné", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Afficher les détails de l'album
        displayAlbumDetails();

        // Configurer Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.deezer.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        deezerApi = retrofit.create(DeezerApi.class);

        // Configurer le RecyclerView pour les pistes
        tracksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tracksAdapter = new TracksAdapter();
        tracksRecyclerView.setAdapter(tracksAdapter);

        // Charger les pistes de l'album
        loadAlbumTracks();

        // Configurer le bouton de retour
        backButton.setOnClickListener(v -> finish());
    }

    private void displayAlbumDetails() {
        albumTitleTextView.setText(album.getTitle());
        albumArtistTextView.setText(album.getArtist() != null ? album.getArtist().getName() : "Artiste inconnu");
        albumTracksCountTextView.setText(album.getNbTracks() + " pistes");

        String coverUrl = album.getCoverXl(); // Assurez-vous d'utiliser la bonne méthode pour obtenir l'URL
        if (coverUrl != null && !coverUrl.isEmpty()) {
            Glide.with(this)
                    .load(coverUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(albumCoverImageView);
        } else {
            albumCoverImageView.setImageResource(R.drawable.placeholder_image);
        }
    }

    private void loadAlbumTracks() {
        String tracklistUrl = album.getTracklist(); // Assurez-vous que le modèle Album a une méthode getTracklist()

        if (tracklistUrl == null || tracklistUrl.isEmpty()) {
            Log.e(TAG, "Tracklist URL est null ou vide");
            Toast.makeText(this, "Aucune piste disponible pour cet album", Toast.LENGTH_SHORT).show();
            return;
        }

        // Extraire l'endpoint relatif pour Retrofit (enlève le domaine)
        String relativeUrl = tracklistUrl.replace("https://api.deezer.com/", "");

        Call<TrackResponse> call = deezerApi.getAlbumTracks(relativeUrl);
        call.enqueue(new Callback<TrackResponse>() {
            @Override
            public void onResponse(@NonNull Call<TrackResponse> call, @NonNull Response<TrackResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Track> tracks = response.body().getData();
                    tracksAdapter.setTracks(tracks);
                } else {
                    Log.e(TAG, "Réponse non réussie lors du chargement des pistes: " + response.code());
                    Toast.makeText(AlbumDetailsActivity.this, "Erreur lors du chargement des pistes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TrackResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Échec du chargement des pistes", t);
                Toast.makeText(AlbumDetailsActivity.this, "Erreur réseau lors du chargement des pistes", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
