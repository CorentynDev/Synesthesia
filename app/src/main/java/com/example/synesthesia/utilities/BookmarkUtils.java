package com.example.synesthesia.utilities;

import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

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
        this.db = db;
    }

    /**
     * Update the users list who have marked a recommendation.
     *
     * @param userId          User ID who marks or unmarks the recommendation.
     * @param recommendation  Recommendation object that contains the users list who have marked this recommendation.
     * @param addMark         Boolean that indicates if the user marks or unmarks the recommendation.
     */
    public void updateMarkList(String userId, @NonNull Recommendation recommendation, boolean addMark) {
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
     * Update the user interface so as to display the state of the bookmark.
     *
     * @param markButton        ImageView representing the bookmark button.
     * @param isCurrentlyMarked Boolean indicating if the recommendation is currently marked.
     */
    public void updateMarkUI(ImageView markButton, boolean isCurrentlyMarked) {
        if (isCurrentlyMarked) {
            markButton.setImageResource(R.drawable.bookmark_active); // Icône active
        } else {
            markButton.setImageResource(R.drawable.bookmark); // Icône non active
        }
    }

    /**
     * Check if a user has marked a recommendation.
     *
     * @param userId          User ID to check.
     * @param recommendation  Recommendation object that contains the users list who have marked the recommendation.
     * @return                True if the user has marked the recommendation, else false.
     */
    public boolean isMarked(String userId, @NonNull Recommendation recommendation) {
        List<String> markedBy = recommendation.getMarkedBy();
        return markedBy != null && markedBy.contains(userId);
    }

    /**
     * Able or disable a recommendation bookmark for a specific user.
     *
     * @param recommendationId  Recommendation ID to mark or unmark.
     * @param userId            User ID for who the bookmark is modified.
     * @param isMarked          Boolean indicating if the recommendation is currently marked.
     * @param onComplete        Runnable to execute after the transaction, in success or not.
     */
    public void toggleMark(String recommendationId, String userId, boolean isMarked, Runnable onComplete) {
        if (recommendationId == null || userId == null) {
            Log.e("ToggleMark", "Recommendation ID or User ID is null");
            return;
        }

        DocumentReference userRef = db.collection("users").document(userId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(userRef);
            if (!snapshot.exists()) {
                throw new FirebaseFirestoreException("User document does not exist", FirebaseFirestoreException.Code.NOT_FOUND);
            }

            //noinspection unchecked
            List<String> bookmarkedRecommendations = (List<String>) snapshot.get("bookmarkedRecommendations");
            if (bookmarkedRecommendations == null) {
                bookmarkedRecommendations = new ArrayList<>();
            }

            if (isMarked) {
                if (!bookmarkedRecommendations.contains(recommendationId)) {
                    bookmarkedRecommendations.add(recommendationId);
                }
            } else {
                bookmarkedRecommendations.remove(recommendationId);
            }

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
