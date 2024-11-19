package com.example.synesthesia;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.synesthesia.api.DeezerApi;
import com.example.synesthesia.models.Comment;
import com.example.synesthesia.models.Recommendation;
import com.example.synesthesia.models.TrackResponse;
import com.example.synesthesia.models.Track;
import com.example.synesthesia.utilities.FooterUtils;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MusicDetailsActivity extends AppCompatActivity {

    private static final String TAG = "MusicDetailsActivity";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EditText commentField;
    private DeezerApi deezerApi;
    private String coverImageUrl;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_details);

        FooterUtils.setupFooter(this, R.id.createRecommendationButton);

        // Récupération des objets passés par Intent
        Track track = getIntent().getParcelableExtra("track");

        // Vérification de l'objet track
        if (track == null) {
            Log.e(TAG, "L'objet Track est nul");
            Toast.makeText(this, "Erreur: Aucun morceau sélectionné", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialisation des vues
        ImageView trackImage = findViewById(R.id.musicImage);
        TextView musicTitle = findViewById(R.id.musicTitle);
        TextView musicArtist = findViewById(R.id.musicArtist);
        TextView musicDuration = findViewById(R.id.musicDuration);
        commentField = findViewById(R.id.commentField);
        Button recommendButton = findViewById(R.id.recommendButton);
        Button backButton = findViewById(R.id.backButton);

        // Vérification de l'initialisation des vues
        if (trackImage == null || musicTitle == null || musicArtist == null || musicDuration == null || commentField == null || recommendButton == null || backButton == null) {
            Log.e(TAG, "Une ou plusieurs vues sont nulles");
            Toast.makeText(this, "Erreur d'initialisation des vues", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Remplissage des données de la musique
        musicTitle.setText(track.getTitle());
        musicArtist.setText(track.getArtist().getName());
        musicDuration.setText(formatDuration(track.getDuration()) + "minutes");
        // Chargement de l'image de couverture
        final String[] coverUrl = {null};
        if (track.getAlbum() != null) {
            coverUrl[0] = track.getAlbum().getCoverXl();
        } else if (track.getArtist() != null) {
            coverUrl[0] = track.getArtist().getImageUrl();
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.deezer.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        deezerApi = retrofit.create(DeezerApi.class);

        // Récupération de l'ID de la musique et appel à l'API pour récupérer les détails
        String idMusic = track.getId();
        fetchTrackDetails(idMusic, trackImage);

        // Initialisation de Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Actions des boutons
        backButton.setOnClickListener(v -> finish());

        recommendButton.setOnClickListener(v -> {
            String commentText = commentField.getText().toString().trim();
            submitRecommendation(track, commentText.isEmpty() ? "" : commentText);

            Intent intent = new Intent(MusicDetailsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void fetchTrackDetails(String idMusic, ImageView trackImage) {
        Call<Track> call = deezerApi.getTrackById(idMusic);
        call.enqueue(new Callback<Track>() {
            @Override
            public void onResponse(@NonNull Call<Track> call, @NonNull Response<Track> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Récupération des détails de la piste directement depuis le corps de la réponse
                    Track trackDetails = response.body();

                    // Chargement de l'image de couverture
                    coverImageUrl = trackDetails.getAlbum().getCoverXl(); // Assigner à coverImageUrl
                    loadCoverImage(coverImageUrl, trackImage);
                } else {
                    // Afficher un message d'erreur si la réponse ne contient pas les détails attendus
                    Log.e(TAG, "Erreur lors de la réponse : " + response.code() + " - " + response.message());
                    Toast.makeText(MusicDetailsActivity.this, "Erreur lors de la récupération des détails de la musique : " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Track> call, Throwable t) {
                Log.e(TAG, "Échec de la récupération des détails de la musique", t);
                Toast.makeText(MusicDetailsActivity.this, "Échec de la connexion à l'API", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCoverImage(String coverUrl, ImageView trackImage) {
        if (coverUrl != null && !coverUrl.isEmpty()) {
            Glide.with(this)
                    .load(coverUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(trackImage);
        } else {
            Log.w(TAG, "URL de couverture est null ou vide");
            trackImage.setImageResource(R.drawable.placeholder_image);
        }
    }

    private void submitRecommendation(Track track, String commentText) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Utilisateur non authentifié", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");

                        Comment firstComment = new Comment(userId, commentText, new Timestamp(new Date()));

                        List<Comment> commentsList = new ArrayList<>();
                        commentsList.add(firstComment);

                        // Utiliser coverImageUrl récupéré dans fetchTrackDetails
                        String recommendationCoverUrl = coverImageUrl;

                        Log.d(TAG, "URL de couverture de recommandation: " + recommendationCoverUrl);

                        Timestamp recommendationTimestamp = new Timestamp(new Date());
                        String type = "music";

                        Recommendation recommendation = new Recommendation(
                                track.getTitle(),
                                null,
                                recommendationCoverUrl,
                                userId,
                                username,
                                commentsList,
                                recommendationTimestamp,
                                type,
                                commentText,
                                track.getId()
                        );

                        db.collection("recommendations")
                                .add(recommendation)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(MusicDetailsActivity.this, "Recommandation enregistrée", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Erreur lors de l'enregistrement de la recommandation", e);
                                    Toast.makeText(MusicDetailsActivity.this, "Erreur lors de l'enregistrement de la recommandation", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.e(TAG, "Utilisateur non trouvé");
                        Toast.makeText(MusicDetailsActivity.this, "Utilisateur non trouvé", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de la récupération des données de l'utilisateur", e);
                    Toast.makeText(MusicDetailsActivity.this, "Erreur lors de la récupération des données de l'utilisateur", Toast.LENGTH_SHORT).show();
                });
    }

    private String formatDuration(int durationInSeconds) {
        int minutes = durationInSeconds / 60;
        int seconds = durationInSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}