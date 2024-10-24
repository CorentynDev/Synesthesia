package com.example.synesthesia;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.synesthesia.api.DeezerApi;
import com.example.synesthesia.models.Track;
import com.example.synesthesia.models.Comment;
import com.example.synesthesia.models.Recommendation;
import com.example.synesthesia.models.TrackResponse;
import com.example.synesthesia.models.musicDansAlbum;
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

    private Track track;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EditText commentField;
    private DeezerApi deezerApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_details);

        FooterUtils.setupFooter(this, R.id.createRecommendationButton);

        // Récupération des objets passés par Intent
        track = getIntent().getParcelableExtra("track");

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
        musicDuration.setText(formatDuration(track.getDuration()));

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

        String idMusic = track.getId();
        Log.d(TAG, "ID de la musique : " + idMusic);

        Call<TrackResponse> call = deezerApi.getTrackById(idMusic); // Utilisez deezerApi ici
        Log.d(TAG, "ID de la musique : " + call);
        call.enqueue(new Callback<TrackResponse>() {
            @Override
            public void onResponse(Call<TrackResponse> call, Response<TrackResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TrackResponse trackResponse = response.body();
                    // Log détaillé de la réponse
                    Log.d(TAG, "Réponse de l'API : " + trackResponse);

                    // Vérifiez si data n'est pas null et contient au moins un élément
                    if (trackResponse.getData() != null && !trackResponse.getData().isEmpty()) {
                        coverUrl[0] = trackResponse.getData().get(0).getAlbum().getCoverXl();
                        // Chargez l'image avec Glide
                        Glide.with(MusicDetailsActivity.this)
                                .load(coverUrl[0])
                                .placeholder(R.drawable.placeholder_image)
                                .into(trackImage);
                    } else {
                        Log.e(TAG, "Aucune piste trouvée dans la réponse");
                        Toast.makeText(MusicDetailsActivity.this, "Aucune piste trouvée", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Log pour afficher le code d'erreur et le message
                    Log.e(TAG, "Erreur lors de la réponse : " + response.code() + " - " + response.message());
                    Toast.makeText(MusicDetailsActivity.this, "Erreur lors de la récupération des détails de la musique : " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TrackResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });

        Log.d(TAG, "URL de couverture: " + coverUrl[0]);

        // Chargement de l'image avec Glide
        if (coverUrl[0] != null && !coverUrl[0].isEmpty()) {
            Glide.with(this)
                    .load(coverUrl[0])
                    .placeholder(R.drawable.placeholder_image)
                    .into(trackImage);
        } else {
            Log.w(TAG, "URL de couverture est null ou vide");
            trackImage.setImageResource(R.drawable.placeholder_image);
        }

        // Initialisation de Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Actions des boutons
        backButton.setOnClickListener(v -> finish());

        recommendButton.setOnClickListener(v -> {
            String commentText = commentField.getText().toString().trim();
            submitRecommendation(track, commentText.isEmpty() ? "" : commentText);
        });
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

                        String recommendationCoverUrl = null;
                        if (track.getAlbum() != null) {
                            recommendationCoverUrl = track.getAlbum().getCoverXl();
                        } else if (track.getArtist() != null) {
                            recommendationCoverUrl = track.getArtist().getImageUrl();
                        }

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
                                commentText
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