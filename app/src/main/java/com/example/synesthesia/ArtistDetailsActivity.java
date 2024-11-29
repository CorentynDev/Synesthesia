package com.example.synesthesia;

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
import com.example.synesthesia.models.Artist;
import com.example.synesthesia.models.Comment;
import com.example.synesthesia.models.Recommendation;
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

public class ArtistDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ArtistDetailsActivity";

    private DeezerApi deezerApi;
    private String artistImageUrl; // Variable pour stocker l'URL de l'image de l'artiste
    private EditText commentField;

    private FirebaseFirestore db; // Firestore pour stocker les recommandations
    private FirebaseAuth mAuth; // Authentification Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_details);

        // Initialisation de Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Récupération de l'objet passé par Intent
        Artist artist = getIntent().getParcelableExtra("artist");

        // Vérification de l'objet artist
        if (artist == null) {
            Log.e(TAG, "L'objet Artist est nul");
            Toast.makeText(this, "Erreur: Aucun artiste sélectionné", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialisation des vues
        ImageView artistImage = findViewById(R.id.artistImage);
        TextView artistName = findViewById(R.id.artistName);
        Button backButton = findViewById(R.id.backButton);
        Button recommendButton = findViewById(R.id.recommendButton);
        commentField = findViewById(R.id.commentField);

        // Initialisation de Retrofit pour l'API Deezer
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.deezer.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        deezerApi = retrofit.create(DeezerApi.class);

        // Chargement des détails de l'artiste
        String artistId = artist.getId();
        fetchArtistDetails(artistId, artistImage, artistName);

        // Action du bouton retour
        backButton.setOnClickListener(v -> finish());

        // Action du bouton de recommandation
        recommendButton.setOnClickListener(v -> {
            String commentText = commentField.getText().toString().trim();
            submitRecommendation(artist, commentText.isEmpty() ? "" : commentText);

            Intent intent = new Intent(ArtistDetailsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void fetchArtistDetails(String artistId, ImageView artistImage, TextView artistName) {
        Call<Artist> call = deezerApi.getArtistById(artistId); // Assurez-vous d'avoir cette méthode dans DeezerApi
        call.enqueue(new Callback<Artist>() {
            @Override
            public void onResponse(@NonNull Call<Artist> call, @NonNull Response<Artist> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Récupération des détails de l'artiste
                    Artist artistDetails = response.body();

                    // Chargement de l'image de l'artiste
                    artistImageUrl = artistDetails.getImageUrl();
                    loadArtistImage(artistImageUrl, artistImage);
                    artistName.setText(artistDetails.getName());
                } else {
                    Log.e(TAG, "Erreur lors de la réponse : " + response.code() + " - " + response.message());
                    Toast.makeText(ArtistDetailsActivity.this, "Erreur lors de la récupération des détails de l'artiste : " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Artist> call, Throwable t) {
                Log.e(TAG, "Échec de la récupération des détails de l'artiste", t);
                Toast.makeText(ArtistDetailsActivity.this, "Échec de la connexion à l'API", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadArtistImage(String artistUrl, ImageView artistImage) {
        if (artistUrl != null && !artistUrl.isEmpty()) {
            Glide.with(this)
                    .load(artistUrl)
                    .placeholder(R.drawable.placeholder_image) // Assurez-vous d'avoir une image par défaut
                    .into(artistImage);
        } else {
            Log.w(TAG, "URL d'image de l'artiste est null ou vide");
            artistImage.setImageResource(R.drawable.placeholder_image);
        }
    }

    private void submitRecommendation(Artist artist, String commentText) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Utilisateur non authentifié", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        // Vérifier que le nom d'utilisateur est disponible
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");

                        Comment firstComment = new Comment(userId, commentText, new Timestamp(new Date()));

                        List<Comment> commentsList = new ArrayList<>();
                        commentsList.add(firstComment);

                        // Utiliser artistImageUrl récupéré dans fetchArtistDetails
                        String recommendationCoverUrl = artistImageUrl;

                        Log.d(TAG, "URL de couverture de recommandation: " + recommendationCoverUrl);

                        Timestamp recommendationTimestamp = new Timestamp(new Date());
                        String type = "artist";

                        Recommendation recommendation = new Recommendation(
                                artist.getName(), // Le nom de l'artiste
                                null,
                                recommendationCoverUrl,
                                userId,
                                username,
                                commentsList,
                                recommendationTimestamp,
                                type,
                                commentText,
                                artist.getId()
                        );

                        // Ajouter la recommandation à Firestore
                        db.collection("recommendations")
                                .add(recommendation)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(ArtistDetailsActivity.this, "Recommandation enregistrée", Toast.LENGTH_SHORT).show();
                                    finish(); // Fermer l'activité après l'enregistrement
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Erreur lors de l'enregistrement de la recommandation", e);
                                    Toast.makeText(ArtistDetailsActivity.this, "Erreur lors de l'enregistrement de la recommandation", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.e(TAG, "Utilisateur non trouvé");
                        Toast.makeText(ArtistDetailsActivity.this, "Utilisateur non trouvé", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de la récupération des données de l'utilisateur", e);
                    Toast.makeText(ArtistDetailsActivity.this, "Erreur lors de la récupération des données de l'utilisateur", Toast.LENGTH_SHORT).show();
                });
    }
}
