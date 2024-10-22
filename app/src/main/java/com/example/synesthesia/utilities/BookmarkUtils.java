package com.example.synesthesia.utilities;

import android.util.Log;
import android.widget.ImageView;

import com.example.synesthesia.R;
import com.example.synesthesia.models.Recommendation;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

public class BookmarkUtils {

    private final FirebaseFirestore db;

    public BookmarkUtils(FirebaseFirestore db) {
        this.db = db; // Initialisation de Firestore
    }

    /**
     * Met à jour la liste des bookmarks (favoris) en fonction de l'utilisateur et de l'état du bookmark.
     */
    public void updateMarkList(String userId, Recommendation recommendation, boolean addMark) {
        List<String> markedBy = recommendation.getMarkedBy();
        if (markedBy == null) {
            markedBy = new ArrayList<>();
            recommendation.setMarkedBy(markedBy);
        }
        if (addMark) {
            if (!markedBy.contains(userId)) {
                markedBy.add(userId);
            }
        } else {
            markedBy.remove(userId);
        }
    }

    /**
     * Met à jour l'interface utilisateur pour les bookmarks.
     */
    public void updateMarkUI(ImageView markButton, boolean isCurrentlyMarked) {
        if (isCurrentlyMarked) {
            markButton.setImageResource(R.drawable.bookmark_active); // Icône active
        } else {
            markButton.setImageResource(R.drawable.bookmark); // Icône non active
        }
    }

    /**
     * Vérifie si un utilisateur a bookmarké une recommandation.
     */
    public boolean isMarked(String userId, Recommendation recommendation) {
        List<String> markedBy = recommendation.getMarkedBy();
        return markedBy != null && markedBy.contains(userId);
    }

    /**
     * Basculer l'état du bookmark dans la base de données Firestore.
     */
    public void toggleMark(String recommendationId, String userId, boolean isMarked, Runnable onComplete) {
        if (recommendationId == null || userId == null) {
            Log.e("ToggleMark", "Recommendation ID or User ID is null");
            return;
        }

        // Référence vers le document de l'utilisateur
        DocumentReference userRef = db.collection("users").document(userId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(userRef);
            if (!snapshot.exists()) {
                throw new FirebaseFirestoreException("User document does not exist", FirebaseFirestoreException.Code.NOT_FOUND);
            }

            List<String> bookmarkedRecommendations = (List<String>) snapshot.get("bookmarkedRecommendations");
            if (bookmarkedRecommendations == null) {
                bookmarkedRecommendations = new ArrayList<>();
            }

            if (isMarked) {
                // Si la recommandation n'est pas encore bookmarkée, on l'ajoute
                if (!bookmarkedRecommendations.contains(recommendationId)) {
                    bookmarkedRecommendations.add(recommendationId);
                }
            } else {
                // Si la recommandation est bookmarkée, on la retire
                bookmarkedRecommendations.remove(recommendationId);
            }

            // Mise à jour du document de l'utilisateur avec la liste modifiée
            transaction.update(userRef, "bookmarkedRecommendations", bookmarkedRecommendations);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("ToggleMark", "Bookmark transaction success!");
            onComplete.run();
        }).addOnFailureListener(e -> {
            Log.e("ToggleMark", "Bookmark transaction failure.", e);
            onComplete.run();
        });
    }
}
