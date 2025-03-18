package com.example.synesthesia.utilities;

import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.synesthesia.R;
import com.example.synesthesia.models.Recommendation;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LikeUtils {

    private final FirebaseFirestore db;

    public LikeUtils(FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Update the users list whose liked a recommendation.
     *
     * @param userId              User ID who likes or don't like the recommendation.
     * @param recommendation      The Recommendation object where the like list has to be updated.
     * @param addLike             Specify if the user has to be added or removed from the list.
     */
    public void updateLikeList(String userId, @NonNull Recommendation recommendation, boolean addLike) {
        List<String> likedBy = recommendation.getLikedBy();
        if (likedBy == null) {
            likedBy = new ArrayList<>();
            recommendation.setLikedBy(likedBy);
        }

        if (addLike) {
            if (!likedBy.contains(userId)) {
                likedBy.add(userId);
            }
        } else {
            likedBy.remove(userId);
        }
        // Update like count for the recommendation type
        updateUserPreferenceScore(userId, recommendation.getType(), addLike);
    }

    /**
     * Update the user interface concerning the like button and the likes count.
     *
     * @param likeButton         ImageView representing the like button.
     * @param likeCounter        TextView displaying the current number of likes.
     * @param isCurrentlyLiked   Boolean indicating if the user currently likes the recommendation.
     * @param currentLikesCount  The current number of likes before the interaction of the user.
     */
    public void updateLikeUI(ImageView likeButton, TextView likeCounter, boolean isCurrentlyLiked, int currentLikesCount) {
        if (isCurrentlyLiked) {
            likeButton.setImageResource(R.drawable.given_like);
            likeCounter.setText(String.valueOf(currentLikesCount + 1));
        } else {
            likeButton.setImageResource(R.drawable.like);
            likeCounter.setText(String.valueOf(Math.max(currentLikesCount - 1, 0)));
        }
    }

    /**
     * Verify if a user has already liked the recommendation.
     *
     * @param userId          User ID to verify.
     * @param recommendation  Recommendation object that contains the users list who loved the recommendation.
     * @return                True is the user has liked the recommendation, else false.
     */
    public boolean isLiked(String userId, @NonNull Recommendation recommendation) {
        List<String> likedBy = recommendation.getLikedBy();
        return likedBy != null && likedBy.contains(userId);
    }

    /**
     * Change the state "like" of a recommendation for a specific user using a transaction.
     *
     * @param recommendationId   Recommendation ID to update.
     * @param userId             User ID who likes or not the recommendation.
     * @param isLiked            Boolean that indicates if the user currently likes the recommendation.
     * @param onComplete         Runnable to execute when the operation is finished, in success or not.
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

            //noinspection unchecked
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

    private void updateUserPreferenceScore(String userId, String type, boolean increment) {
        DocumentReference userRef = db.collection("users").document(userId);
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(userRef);
            Map<String, Long> preferenceScores = snapshot.contains("preferenceScores") ?
                    (Map<String, Long>) snapshot.get("preferenceScores") : new HashMap<>();

            long currentScore = preferenceScores.getOrDefault(type, 0L);
            long newScore = increment ? currentScore + 1 : Math.max(currentScore - 1, 0);
            preferenceScores.put(type, newScore);

            transaction.update(userRef, "preferenceScores", preferenceScores);
            return null;
        });
    }
}
