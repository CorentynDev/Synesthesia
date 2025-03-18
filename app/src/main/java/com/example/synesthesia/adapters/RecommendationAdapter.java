package com.example.synesthesia.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.R;
import com.example.synesthesia.models.Recommendation;
import com.example.synesthesia.utilities.CommentUtils;
import com.example.synesthesia.utilities.LikeUtils;
import com.example.synesthesia.utilities.RecommendationsUtils;
import com.example.synesthesia.utilities.TimeUtils;
import com.example.synesthesia.utilities.UserUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {

    private List<Recommendation> recommendations;
    private final FirebaseFirestore db;
    private final HashMap<String, User> userCache;
    private final LikeUtils likeUtils;
    private final CommentUtils commentUtils;
    private final Context context;
    private final HashMap<String, String> recommendationIdMap; // Map to store recommendation IDs

    public RecommendationAdapter(List<Recommendation> recommendations, Context context) {
        this.recommendations = recommendations;
        this.db = FirebaseFirestore.getInstance();
        this.userCache = new HashMap<>();
        this.likeUtils = new LikeUtils(db);
        this.commentUtils = new CommentUtils(db);
        this.context = context;
        this.recommendationIdMap = new HashMap<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recommendation_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recommendation recommendation = recommendations.get(position);
        String recommendationId = recommendationIdMap.get(recommendation.getTitle()); // Get the ID from the map

        holder.titleTextView.setText(recommendation.getTitle());

        if (recommendation.getCoverUrl() != null && !recommendation.getCoverUrl().isEmpty()) {
            Picasso.get().load(recommendation.getCoverUrl())
                    .placeholder(R.drawable.rotating_loader)
                    .into(holder.coverImageView);
        } else {
            holder.coverImageView.setImageResource(R.drawable.placeholder_image);
        }

        String userNote = recommendation.getUserNote();
        if (userNote != null && !userNote.isEmpty()) {
            holder.userRating.setText(userNote);
        } else {
            holder.userRating.setText("Pas de note");
        }

        String userId = recommendation.getUserId();
        if (userId != null && !userId.isEmpty()) {
            loadUserData(userId, holder);
        } else {
            displayDefaultUser(holder);
        }

        List<String> likedBy = recommendation.getLikedBy();
        if (likedBy == null) {
            likedBy = new ArrayList<>();
        }
        holder.likesCountTextView.setText(String.valueOf(likedBy.size()));

        if (recommendation.getTimestamp() != null) {
            String timeAgo = TimeUtils.getTimeAgo(recommendation.getTimestamp());
            holder.dateTextView.setText(timeAgo);
        } else {
            holder.dateTextView.setText("Date inconnue");
        }

        setupLikeButton(holder, recommendation, recommendationId);
        setupCommentButton(holder, recommendationId);
        setupBookmarkButton(holder, recommendation, recommendationId);
    }

    private void setupLikeButton(ViewHolder holder, Recommendation recommendation, String recommendationId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            final boolean[] isCurrentlyLiked = {likeUtils.isLiked(userId, recommendation)};
            holder.likeButton.setImageResource(isCurrentlyLiked[0] ? R.drawable.given_like : R.drawable.like);

            holder.likeButton.setOnClickListener(v -> {
                boolean newLikeStatus = !isCurrentlyLiked[0];
                likeUtils.updateLikeUI(holder.likeButton, holder.likesCountTextView, newLikeStatus, recommendation.getLikedBy().size());
                likeUtils.updateLikeList(userId, recommendation, newLikeStatus);
                likeUtils.toggleLike(recommendationId, userId, newLikeStatus, () -> isCurrentlyLiked[0] = newLikeStatus);

                if (newLikeStatus) {
                    RecommendationsUtils.sendLikeNotification(context, recommendation.getUserId());
                }
            });
        }
    }

    private void setupCommentButton(ViewHolder holder, String recommendationId) {
        commentUtils.loadCommentCount(recommendationId, holder.commentCounter);

        holder.commentButton.setOnClickListener(v -> commentUtils.showCommentModal(context, recommendationId, holder.commentCounter));
    }

    private void setupBookmarkButton(ViewHolder holder, Recommendation recommendation, String recommendationId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> bookmarkedRecommendations = (List<String>) documentSnapshot.get("bookmarkedRecommendations");
                    boolean isBookmarked = bookmarkedRecommendations != null && bookmarkedRecommendations.contains(recommendationId);

                    holder.markButton.setImageResource(isBookmarked ? R.drawable.bookmark_active : R.drawable.bookmark);

                    final boolean[] isCurrentlyMarked = {isBookmarked};
                    holder.markButton.setOnClickListener(v -> {
                        boolean newMarkStatus = !isCurrentlyMarked[0];
                        RecommendationsUtils.updateMarkUI(holder.markButton, newMarkStatus);
                        RecommendationsUtils.updateMarkList(userId, recommendationId, newMarkStatus);
                        RecommendationsUtils.toggleMark(recommendationId, userId, newMarkStatus, () -> isCurrentlyMarked[0] = newMarkStatus);
                    });
                }
            }).addOnFailureListener(e -> Log.e("setupBookmarkButton", "Error fetching user bookmarks", e));
        }
    }

    private void loadUserData(String userId, ViewHolder holder) {
        if (userCache.containsKey(userId)) {
            bindUserData(holder, userCache.get(userId));
        } else {
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                            String username = documentSnapshot.getString("username");
                            User user = new User(profileImageUrl, username);

                            userCache.put(userId, user);
                            bindUserData(holder, user);
                        } else {
                            displayDefaultUser(holder);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("TAG", "Erreur lors du chargement de l'utilisateur: ", e);
                        displayDefaultUser(holder);
                    });
        }
    }

    private void bindUserData(ViewHolder holder, User user) {
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Picasso.get().load(user.getProfileImageUrl())
                    .placeholder(R.drawable.rotating_loader)
                    .into(holder.profileImageView);
        } else {
            holder.profileImageView.setImageResource(R.drawable.default_profil_picture);
        }
        holder.userNameTextView.setText(user.getUsername() != null ? user.getUsername() : "Utilisateur inconnu");
    }

    private void displayDefaultUser(ViewHolder holder) {
        holder.userNameTextView.setText("Utilisateur inconnu");
        holder.profileImageView.setImageResource(R.drawable.default_profil_picture);
    }

    @Override
    public int getItemCount() {
        return recommendations != null ? recommendations.size() : 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setRecommendations(List<Recommendation> recommendations, HashMap<String, String> recommendationIdMap) {
        this.recommendations = recommendations;
        this.recommendationIdMap.clear();
        this.recommendationIdMap.putAll(recommendationIdMap);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView coverImageView;
        ImageView profileImageView;
        TextView titleTextView;
        TextView userNameTextView;
        TextView likesCountTextView;
        TextView dateTextView;
        TextView userRating;
        ImageView likeButton;
        ImageView commentButton;
        TextView commentCounter;
        ImageView markButton;

        ViewHolder(View itemView) {
            super(itemView);
            coverImageView = itemView.findViewById(R.id.recommendationCover);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            titleTextView = itemView.findViewById(R.id.recommendationTitle);
            userNameTextView = itemView.findViewById(R.id.recommendationUser);
            likesCountTextView = itemView.findViewById(R.id.likeCounter);
            dateTextView = itemView.findViewById(R.id.recommendationDate);
            userRating = itemView.findViewById(R.id.userRating);
            likeButton = itemView.findViewById(R.id.likeButton);
            commentButton = itemView.findViewById(R.id.commentButton);
            commentCounter = itemView.findViewById(R.id.commentCounter);
            markButton = itemView.findViewById(R.id.bookmarkRecommendationButton);
        }
    }

    private static class User {
        private final String profileImageUrl;
        private final String username;

        public User(String profileImageUrl, String username) {
            this.profileImageUrl = profileImageUrl;
            this.username = username;
        }

        public String getProfileImageUrl() {
            return profileImageUrl;
        }

        public String getUsername() {
            return username;
        }
    }
}
