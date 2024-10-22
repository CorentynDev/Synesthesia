package com.example.synesthesia.utilities;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.bumptech.glide.Glide;
import com.example.synesthesia.R;
import com.example.synesthesia.models.Recommendation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class RecommendationsUtils {

    private FirebaseFirestore db;
    private LikeUtils likeUtils;
    private BookmarkUtils bookmarkUtils;

    public RecommendationsUtils(FirebaseFirestore db) {
        this.db = db;  // Initialisation de la base de données Firestore
        this.likeUtils = new LikeUtils(db);  // Initialisation de LikeUtils
        this.bookmarkUtils = new BookmarkUtils(db);  // Initialisation de BookmarkUtils
    }

    /**
     * Fonction pour obtenir les données de recommandations depuis Firestore
     * et ajouter les cartes dans la liste.
     */
    public void getRecommendationData(Context context, LinearLayout recommendationList, SwipeRefreshLayout swipeRefreshLayout) {
        Log.d("RecommendationsUtils", "Starting to fetch recommendations");

        swipeRefreshLayout.setRefreshing(true);

        db.collection("recommendations").get().addOnSuccessListener(queryDocumentSnapshots -> {
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
     * Fonction pour ajouter une carte de recommandation à la vue
     */
    public void addRecommendationCard(Context context, LinearLayout container, Recommendation recommendation, String recommendationId) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View cardView = inflater.inflate(R.layout.recommendation_card, container, false);

        // Titre
        TextView titleTextView = cardView.findViewById(R.id.recommendationTitle);
        titleTextView.setText(recommendation.getTitle());

        // Date
        TextView dateTextView = cardView.findViewById(R.id.recommendationDate);
        Timestamp timestamp = recommendation.getTimestamp();
        TimeUtils timeUtils = new TimeUtils();
        String timeAgo = timeUtils.getTimeAgo(timestamp);
        dateTextView.setText(timeAgo != null ? timeAgo : "Date inconnue");

        // Utilisateur et image de profil
        TextView userTextView = cardView.findViewById(R.id.recommendationUser);
        ImageView profileImageView = cardView.findViewById(R.id.profileImageView);
        loadUserProfile(context, recommendation.getUserId(), userTextView, profileImageView);

        // Image de couverture
        ImageView coverImageView = cardView.findViewById(R.id.recommendationCover);
        loadImage(context, recommendation.getCoverUrl(), coverImageView);

        // Boutons de like et de bookmark
        setupLikeAndMarkButtons(cardView, recommendation, recommendationId);

        // Commentaires
        TextView commentCounter = cardView.findViewById(R.id.commentCounter);
        ImageView commentButton = cardView.findViewById(R.id.commentButton);
        loadCommentCount(recommendationId, commentCounter);

        // Gestion des commentaires
        commentButton.setOnClickListener(v -> showCommentModal(context, recommendationId, commentCounter));

        // Ajout de la vue dans le container
        container.addView(cardView);
    }

    /**
     * Configuration des boutons de like et de bookmark.
     */
    private void setupLikeAndMarkButtons(View cardView, Recommendation recommendation, String recommendationId) {
        ImageView likeButton = cardView.findViewById(R.id.likeButton);
        TextView likeCounter = cardView.findViewById(R.id.likeCounter);
        ImageView markButton = cardView.findViewById(R.id.bookmarkRecommendationButton);

        // Assurer que likedBy n'est pas null
        List<String> likedBy = recommendation.getLikedBy() != null ? recommendation.getLikedBy() : new ArrayList<>();
        likeCounter.setText(String.valueOf(likedBy.size()));

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            final boolean[] isCurrentlyLiked = {likeUtils.isLiked(userId, recommendation)};
            final boolean[] isCurrentlyMarked = {bookmarkUtils.isMarked(userId, recommendation)};

            // Gestion de l'état initial des boutons
            likeButton.setImageResource(isCurrentlyLiked[0] ? R.drawable.given_like : R.drawable.like);
            markButton.setImageResource(isCurrentlyMarked[0] ? R.drawable.bookmark_active : R.drawable.bookmark);

            // Click sur le bouton like
            likeButton.setOnClickListener(v -> {
                boolean newLikeStatus = !isCurrentlyLiked[0];
                likeUtils.updateLikeUI(likeButton, likeCounter, newLikeStatus, likedBy.size());
                likeUtils.updateLikeList(userId, recommendation, newLikeStatus);
                likeUtils.toggleLike(recommendationId, userId, newLikeStatus, () -> isCurrentlyLiked[0] = newLikeStatus);
            });

            // Click sur le bouton bookmark
            markButton.setOnClickListener(v -> {
                boolean newMarkStatus = !isCurrentlyMarked[0];
                bookmarkUtils.updateMarkUI(markButton, newMarkStatus);
                bookmarkUtils.updateMarkList(userId, recommendation, newMarkStatus);
                bookmarkUtils.toggleMark(recommendationId, userId, newMarkStatus, () -> isCurrentlyMarked[0] = newMarkStatus);
            });
        }
    }
}