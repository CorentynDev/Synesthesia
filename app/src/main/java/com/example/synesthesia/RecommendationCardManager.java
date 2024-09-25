package com.example.synesthesia;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.synesthesia.models.Recommendation;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RecommendationCardManager {

    private final FirebaseFirestore db;

    public RecommendationCardManager() {
        db = FirebaseFirestore.getInstance();
    }

    public void addRecommendationCard(Context context, LinearLayout container, Recommendation recommendation, String recommendationId) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View cardView = inflater.inflate(R.layout.recommendation_card, container, false);

        TextView titleTextView = cardView.findViewById(R.id.recommendationTitle);
        titleTextView.setText(recommendation.getTitle());

        TextView dateTextView = cardView.findViewById(R.id.recommendationDate);
        Timestamp timestamp = recommendation.getTimestamp();
        if (timestamp != null) {
            dateTextView.setText(getTimeAgo(timestamp));
        } else {
            dateTextView.setText("Date inconnue");
        }

        TextView userTextView = cardView.findViewById(R.id.recommendationUser);
        ImageView profileImageView = cardView.findViewById(R.id.profileImageView);
        db.collection("users").document(recommendation.getUserId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        userTextView.setText(username);
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        if (profileImageUrl != null) {
                            Picasso.get().load(profileImageUrl).into(profileImageView);
                        }
                    } else {
                        userTextView.setText("Utilisateur inconnu");
                    }
                })
                .addOnFailureListener(e -> {
                    userTextView.setText("Erreur de chargement");
                });

        ImageView coverImageView = cardView.findViewById(R.id.recommendationCover);
        if (recommendation.getCoverUrl() != null && !recommendation.getCoverUrl().isEmpty()) {
            Glide.with(context)
                    .load(recommendation.getCoverUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .into(coverImageView);
        } else {
            coverImageView.setImageResource(R.drawable.placeholder_image);
        }

        ImageView likeButton = cardView.findViewById(R.id.likeButton);
        TextView likeCounter = cardView.findViewById(R.id.likeCounter);
        ImageView commentButton = cardView.findViewById(R.id.commentButton);
        ImageView markButton = cardView.findViewById(R.id.markButton);

        final List<String> likedBy;
        if (recommendation.getLikedBy() == null) {
            likedBy = new ArrayList<>();
            recommendation.setLikedBy(likedBy);
        } else {
            likedBy = recommendation.getLikedBy();
        }

        likeCounter.setText(String.valueOf(likedBy.size()));

        // Implement listeners for like, comment, and bookmark if needed

        container.addView(cardView);
    }

    private String getTimeAgo(Timestamp timestamp) {
        long time = timestamp.toDate().getTime();
        long now = System.currentTimeMillis();

        if (time > now || time <= 0) {
            return "à l'instant";
        }

        final long diff = now - time;
        if (diff < 60 * 1000) { // moins d'une minute
            return "Il y a " + diff / 1000 + " secondes";
        } else if (diff < 2 * 60 * 1000) {
            return "Il y a une minute";
        } else if (diff < 50 * 60 * 1000) {
            return "Il y a " + diff / (60 * 1000) + " minutes";
        } else if (diff < 90 * 60 * 1000) {
            return "Il y a une heure";
        } else if (diff < 24 * 60 * 60 * 1000) {
            return "Il y a " + diff / (60 * 60 * 1000) + " heures";
        } else if (diff < 48 * 60 * 60 * 1000) {
            return "Hier";
        } else {
            return "Il y a " + diff / (24 * 60 * 60 * 1000) + " jours";
        }
    }
}
