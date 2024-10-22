package com.example.synesthesia.utilities;

import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.synesthesia.R;
import com.example.synesthesia.models.Recommendation;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.List;

public class LikeUtils {

    private final FirebaseFirestore db;

    public LikeUtils(FirebaseFirestore db) {
        this.db = db; // Initialisation de Firestore
    }

    /**
     * Met à jour la liste des likes en fonction de l'utilisateur et de l'état du like.
     */
    public void updateLikeList(String userId, Recommendation recommendation, boolean addLike) {
        List<String> likedBy = recommendation.getLikedBy();
        if (likedBy == null) {
            likedBy = new ArrayList<>();  // Si likedBy est null, initialiser une nouvelle liste
            recommendation.setLikedBy(likedBy);
        }

        if (addLike) {
            if (!likedBy.contains(userId)) {
                likedBy.add(userId);
            }
        } else {
            likedBy.remove(userId);
        }
    }

    /**
     * Met à jour l'interface utilisateur pour les likes.
     */
    public void updateLikeUI(ImageView likeButton, TextView likeCounter, boolean isCurrentlyLiked, int currentLikesCount) {
        if (isCurrentlyLiked) {
            likeButton.setImageResource(R.drawable.given_like);
            likeCounter.setText(String.valueOf(currentLikesCount + 1));
        } else {
            likeButton.setImageResource(R.drawable.like);
            likeCounter.setText(String.valueOf(Math.max(currentLikesCount - 1, 0))); // Eviter d'aller sous 0
        }
    }

    /**
     * Vérifie si un utilisateur a aimé une recommandation.
     */
    public boolean isLiked(String userId, Recommendation recommendation) {
        List<String> likedBy = recommendation.getLikedBy();
        return likedBy != null && likedBy.contains(userId);
    }

    /**
     * Basculer l'état du like dans la base de données Firestore.
     */
    public void toggleLike(String recommendationId, String userId, boolean isLiked, Runnable onComplete) {
        if (recommendationId == null || userId == null) {
            Log.e("ToggleLike", "Recommendation ID or User ID is null");
            return;
        }

        DocumentReference recommendationRef = db.collection("recommendations").document(recommendationId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(recommendationRef);
            if (!snapshot.exists()) {
                throw new FirebaseFirestoreException("Document does not exist", FirebaseFirestoreException.Code.NOT_FOUND);
            }

            List<String> likedBy = (List<String>) snapshot.get("likedBy");
            if (likedBy == null) {
                likedBy = new ArrayList<>();
            }

            if (isLiked) {
                if (!likedBy.contains(userId)) {
                    likedBy.add(userId);
                }
            } else {
                likedBy.remove(userId);
            }

            transaction.update(recommendationRef, "likedBy", likedBy);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("ToggleLike", "Transaction success!");
            onComplete.run();
        }).addOnFailureListener(e -> {
            Log.e("ToggleLike", "Transaction failure.", e);
            onComplete.run();
        });
    }
}
