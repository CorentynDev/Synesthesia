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
import com.example.synesthesia.utilities.FooterUtils;
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
    private EditText commentField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_details);

        FooterUtils.setupFooter(this);

        track = getIntent().getParcelableExtra("track");
        Log.d(TAG, "Track received: " + track);

        if (track == null) {
            Log.e(TAG, "Track object is null");
            Toast.makeText(this, "Erreur: Aucun morceau sélectionné", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageView trackImage = findViewById(R.id.musicImage);
        TextView musicTitle = findViewById(R.id.musicTitle);
        TextView musicArtist = findViewById(R.id.musicArtist);
        TextView musicDuration = findViewById(R.id.musicDuration);
        commentField = findViewById(R.id.commentField);
        Button recommendButton = findViewById(R.id.recommendButton);
        Button backButton = findViewById(R.id.backButton);

        if (trackImage == null || musicTitle == null || musicArtist == null || musicDuration == null || commentField == null || recommendButton == null || backButton == null) {
            Log.e(TAG, "Une ou plusieurs vues sont nulles");
            Toast.makeText(this, "Erreur d'initialisation des vues", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        musicTitle.setText(track.getTitle());
        musicArtist.setText(track.getArtist().getName());
        musicDuration.setText(formatDuration(track.getDuration()));

        String coverUrl = null;
        if (track.getAlbum() != null) {
            coverUrl = track.getAlbum().getCoverXl();
        } else if (track.getArtist() != null) {
            coverUrl = track.getArtist().getImageUrl();
        }

        Log.d(TAG, "Cover URL: " + coverUrl);

        if (coverUrl != null && !coverUrl.isEmpty()) {
            Glide.with(this)
                    .load(coverUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(trackImage);
        } else {
            Log.w(TAG, "Cover URL est null ou vide");
            trackImage.setImageResource(R.drawable.placeholder_image);
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

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

                        Log.d(TAG, "Recommendation Cover URL: " + recommendationCoverUrl);

                        Timestamp recommendationTimestamp = new Timestamp(new Date());
                        String type = "music";

                        String userNote = commentField.getText().toString().trim();

                        Recommendation recommendation = new Recommendation(
                                track.getTitle(),
                                null,
                                recommendationCoverUrl,
                                userId,
                                username,
                                commentsList,
                                recommendationTimestamp,
                                type,
                                userNote
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
