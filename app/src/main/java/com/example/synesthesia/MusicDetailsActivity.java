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
import com.example.synesthesia.models.Track;
import com.example.synesthesia.models.Comment;
import com.example.synesthesia.models.Recommendation;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MusicDetailsActivity extends AppCompatActivity {

    private static final String TAG = "MusicDetailsActivity";

    private Track track;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_details); // Assurez-vous que ce layout existe

        // Récupérer le morceau depuis l'Intent
        track = getIntent().getParcelableExtra("track");
        Log.d(TAG, "Track received: " + track);

        if (track == null) {
            Log.e(TAG, "Track object is null");
            Toast.makeText(this, "Erreur: Aucun morceau sélectionné", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialiser les vues
        ImageView trackImage = findViewById(R.id.musicImage);
        TextView musicTitle = findViewById(R.id.musicTitle);
        TextView musicArtist = findViewById(R.id.musicArtist);
        TextView musicDuration = findViewById(R.id.musicDuration);
        EditText commentField = findViewById(R.id.commentField);
        Button recommendButton = findViewById(R.id.recommendButton);
        Button backButton = findViewById(R.id.backButton);

        // Vérifier que les vues ne sont pas nulles
        if (trackImage == null || musicTitle == null || musicArtist == null || musicDuration == null || commentField == null || recommendButton == null || backButton == null) {
            Log.e(TAG, "Une ou plusieurs vues sont nulles");
            Toast.makeText(this, "Erreur d'initialisation des vues", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Définir les détails du morceau
        musicTitle.setText(track.getTitle());
        musicArtist.setText(track.getArtistName());
        musicDuration.setText(formatDuration(track.getDuration()));

        // Déterminer l'URL de couverture appropriée
        String coverUrl = null;
        if (track.getAlbum() != null) {
            coverUrl = track.getAlbum().getCoverUrl();
        } else if (track.getArtist() != null) { // Utilisez getArtist() pour obtenir l'objet Artist
            coverUrl = track.getArtist().getImageUrl(); // Utilisez la méthode appropriée pour obtenir l'URL de l'image de l'artiste
        }

        Log.d(TAG, "Cover URL: " + coverUrl);

        if (coverUrl != null && !coverUrl.isEmpty()) {
            Glide.with(this)
                    .load(coverUrl) // Utilisez l'URL d'image correcte
                    .placeholder(R.drawable.placeholder_image)
                    .into(trackImage);
        } else {
            Log.w(TAG, "Cover URL est null ou vide");
            trackImage.setImageResource(R.drawable.placeholder_image); // Image par défaut
        }

        // Initialiser Firebase Firestore et FirebaseAuth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Bouton retour
        backButton.setOnClickListener(v -> finish());

        // Bouton recommander
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

        // Récupérer le nom d'utilisateur de Firestore
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");

                        // Créer le premier commentaire (note de l'utilisateur)
                        Comment firstComment = new Comment(userId, commentText, new Timestamp(new Date()));

                        // Créer une liste de commentaires avec le premier commentaire
                        List<Comment> commentsList = new ArrayList<>();
                        commentsList.add(firstComment);

                        // Déterminer l'URL de couverture appropriée pour la recommandation
                        String recommendationCoverUrl = null;
                        if (track.getAlbum() != null) {
                            recommendationCoverUrl = track.getAlbum().getCoverUrl();
                        } else if (track.getArtist() != null) {
                            recommendationCoverUrl = track.getArtist().getImageUrl();
                        }

                        Log.d(TAG, "Recommendation Cover URL: " + recommendationCoverUrl);

                        // Créer un objet Recommendation avec les détails de la musique
                        Recommendation recommendation = new Recommendation(
                                null, // ID sera auto-généré par Firestore
                                track.getTitle(),
                                null, // Date sera définie automatiquement par Firestore
                                recommendationCoverUrl, // Cover image URL correcte
                                userId,
                                username,
                                commentsList // Liste des commentaires
                        );

                        // Ajouter la recommandation à Firestore
                        db.collection("recommendations")
                                .add(recommendation)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(MusicDetailsActivity.this, "Recommandation enregistrée", Toast.LENGTH_SHORT).show();
                                    finish(); // Fermer l'activité après l'enregistrement
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

    // Fonction utilitaire pour formater la durée en minutes et secondes
    private String formatDuration(int durationInSeconds) {
        int minutes = durationInSeconds / 60;
        int seconds = durationInSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
