package com.example.synesthesia.utilities;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.synesthesia.R;
import com.example.synesthesia.models.Recommendation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class RecommendationsUtils {

    private final FirebaseFirestore db;
    private final LikeUtils likeUtils;
    private final BookmarkUtils bookmarkUtils;
    private final CommentUtils commentUtils;

    public RecommendationsUtils(FirebaseFirestore db) {
        this.db = db;
        this.likeUtils = new LikeUtils(db);
        this.bookmarkUtils = new BookmarkUtils(db);
        this.commentUtils = new CommentUtils(db);
    }

    /**
     * Get recommendations data from Firestore and display it as a list.
     *
     * @param context                Context in which method is called (an activity).
     * @param recommendationList     LinearLayout in which recommendations cards will be added.
     * @param swipeRefreshLayout     SwipeRefreshLayout used to allow user to refresh the list.
     */
    public void getRecommendationData(Context context, LinearLayout recommendationList, @NonNull SwipeRefreshLayout swipeRefreshLayout) {
        Log.d("RecommendationsUtils", "Starting to fetch recommendations");

        swipeRefreshLayout.setRefreshing(true);

        db.collection("recommendations").orderBy("timestamp", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
            Log.d("RecommendationsUtils", "Successfully fetched recommendations");
            recommendationList.removeAllViews();

            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Recommendation recommendation = document.toObject(Recommendation.class);
                addRecommendationCard(context, recommendationList, recommendation, document.getId());
            }

            swipeRefreshLayout.setRefreshing(false);
        }).addOnFailureListener(e -> {
            Log.e("FirestoreData", "Error when fetching documents: ", e);
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    /**
     * Add a recommendation card to a context (an activity).
     *
     * @param context               Context in which the method is called (the activity).
     * @param container             LinearLayout in which the recommendation card will be added.
     * @param recommendation        The object Recommendation that contains the several details to display on the card.
     * @param recommendationId      ID of the recommendation, used for the interactions (like, comment, bookmark).
     */
    public void addRecommendationCard(Context context, LinearLayout container, @NonNull Recommendation recommendation, String recommendationId) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View cardView = inflater.inflate(R.layout.recommendation_card, container, false);

        TextView titleTextView = cardView.findViewById(R.id.recommendationTitle);
        titleTextView.setText(recommendation.getTitle());

        TextView dateTextView = cardView.findViewById(R.id.recommendationDate);
        Timestamp timestamp = recommendation.getTimestamp();
        String timeAgo = TimeUtils.getTimeAgo(timestamp);
        dateTextView.setText(timeAgo);

        TextView userTextView = cardView.findViewById(R.id.recommendationUser);
        ImageView profileImageView = cardView.findViewById(R.id.profileImageView);
        UserUtils.loadUserProfile(context, recommendation.getUserId(), userTextView, profileImageView);

        ImageView typeIconImageView = cardView.findViewById(R.id.recommendationTypeIcon);

        ImageView coverImageView = cardView.findViewById(R.id.recommendationCover);
        ImagesUtils.loadImage(context, recommendation.getCoverUrl(), coverImageView);

        setupLikeAndMarkButtons(cardView, recommendation, recommendationId);

        TextView userNote = cardView.findViewById(R.id.userRating);
        userNote.setText(recommendation.getUserNote());

        TextView commentCounter = cardView.findViewById(R.id.commentCounter);
        ImageView commentButton = cardView.findViewById(R.id.commentButton);

        commentUtils.loadCommentCount(recommendationId, commentCounter);

        commentButton.setOnClickListener(v -> commentUtils.showCommentModal(context, recommendationId, commentCounter));

        String recommendationType = recommendation.getType();
        if ("book".equals(recommendationType)) {
            typeIconImageView.setImageResource(R.drawable.book);
        } else if ("music".equals(recommendationType)) {
            typeIconImageView.setImageResource(R.drawable.musical_note);
        } else {
            typeIconImageView.setVisibility(View.GONE);
        }

        container.addView(cardView);
    }

    /**
     * Configure the several buttons (like and bookmark).
     *
     * @param cardView             Vue of the recommendation card that contains the buttons to configure.
     * @param recommendation       The Recommendation object for which the buttons are configured.
     * @param recommendationId     ID of the recommendation, used for the interactions.
     */
    private void setupLikeAndMarkButtons(@NonNull View cardView, @NonNull Recommendation recommendation, String recommendationId) {
        ImageView likeButton = cardView.findViewById(R.id.likeButton);
        TextView likeCounter = cardView.findViewById(R.id.likeCounter);
        ImageView markButton = cardView.findViewById(R.id.bookmarkRecommendationButton);

        List<String> likedBy = recommendation.getLikedBy() != null ? recommendation.getLikedBy() : new ArrayList<>();
        likeCounter.setText(String.valueOf(likedBy.size()));

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Initialize the like button
            final boolean[] isCurrentlyLiked = {likeUtils.isLiked(userId, recommendation)};
            likeButton.setImageResource(isCurrentlyLiked[0] ? R.drawable.given_like : R.drawable.like);

            // Récupérer les bookmarks de l'utilisateur et initialiser l'état du bouton
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<String> bookmarkedRecommendations = (List<String>) documentSnapshot.get("bookmarkedRecommendations");
                            boolean isBookmarked = bookmarkedRecommendations != null && bookmarkedRecommendations.contains(recommendationId);

                            markButton.setImageResource(isBookmarked ? R.drawable.bookmark_active : R.drawable.bookmark);

                            final boolean[] isCurrentlyMarked = {isBookmarked};
                            markButton.setOnClickListener(v -> {
                                boolean newMarkStatus = !isCurrentlyMarked[0];
                                bookmarkUtils.updateMarkUI(markButton, newMarkStatus);
                                bookmarkUtils.updateMarkList(userId, recommendation, newMarkStatus);
                                bookmarkUtils.toggleMark(recommendationId, userId, newMarkStatus, () -> isCurrentlyMarked[0] = newMarkStatus);
                            });
                        }
                    })
                    .addOnFailureListener(e -> Log.e("setupLikeAndMarkButtons", "Error fetching user bookmarks", e));

            likeButton.setOnClickListener(v -> {
                boolean newLikeStatus = !isCurrentlyLiked[0];
                likeUtils.updateLikeUI(likeButton, likeCounter, newLikeStatus, likedBy.size());
                likeUtils.updateLikeList(userId, recommendation, newLikeStatus);
                likeUtils.toggleLike(recommendationId, userId, newLikeStatus, () -> isCurrentlyLiked[0] = newLikeStatus);
            });
        }
    }
}
