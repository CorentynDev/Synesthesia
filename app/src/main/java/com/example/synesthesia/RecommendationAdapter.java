package com.example.synesthesia;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.models.Recommendation;
import com.example.synesthesia.utilities.TimeUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {

    private List<Recommendation> recommendations;
    private final FirebaseFirestore db;
    private final HashMap<String, User> userCache;

    public RecommendationAdapter(List<Recommendation> recommendations) {
        this.recommendations = recommendations;
        this.db = FirebaseFirestore.getInstance();
        this.userCache = new HashMap<>();
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

        // Titre de la recommandation
        holder.titleTextView.setText(recommendation.getTitle());

        // Charger l'image de couverture avec Picasso
        if (recommendation.getCoverUrl() != null && !recommendation.getCoverUrl().isEmpty()) {
            Picasso.get().load(recommendation.getCoverUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.coverImageView);
        } else {
            holder.coverImageView.setImageResource(R.drawable.placeholder_image);
        }

        // Note utilisateur
        String userNote = recommendation.getUserNote();
        if (userNote != null && !userNote.isEmpty()) {
            holder.userRating.setText(userNote);
        } else {
            holder.userRating.setText("Pas de note");
        }

        // Récupération des informations utilisateur
        String userId = recommendation.getUserId();
        if (userId != null && !userId.isEmpty()) {
            loadUserData(userId, holder);
        } else {
            displayDefaultUser(holder);
        }

        // Afficher le nombre de likes
        List<String> likedBy = recommendation.getLikedBy() != null ? recommendation.getLikedBy() : new ArrayList<>();
        holder.likesCountTextView.setText(String.valueOf(likedBy.size()));

        // Afficher la date
        if (recommendation.getTimestamp() != null) {
            String timeAgo = TimeUtils.getTimeAgo(recommendation.getTimestamp());
            holder.dateTextView.setText(timeAgo);
        } else {
            holder.dateTextView.setText("Date inconnue");
        }
    }

    /**
     * Méthode pour charger les données utilisateur depuis Firestore ou le cache.
     */
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

                            userCache.put(userId, user); // Ajouter l'utilisateur au cache
                            bindUserData(holder, user);
                        } else {
                            displayDefaultUser(holder);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur lors du chargement de l'utilisateur: ", e);
                        displayDefaultUser(holder);
                    });
        }
    }

    /**
     * Méthode pour lier les données utilisateur à la vue.
     */
    private void bindUserData(ViewHolder holder, User user) {
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Picasso.get().load(user.getProfileImageUrl())
                    .placeholder(R.drawable.default_profil_picture)
                    .into(holder.profileImageView);
        } else {
            holder.profileImageView.setImageResource(R.drawable.default_profil_picture);
        }
        holder.userNameTextView.setText(user.getUsername() != null ? user.getUsername() : "Utilisateur inconnu");
    }

    /**
     * Méthode pour afficher les valeurs par défaut lorsque les données utilisateur sont indisponibles.
     */
    private void displayDefaultUser(ViewHolder holder) {
        holder.userNameTextView.setText("Utilisateur inconnu");
        holder.profileImageView.setImageResource(R.drawable.default_profil_picture);
    }

    @Override
    public int getItemCount() {
        return recommendations != null ? recommendations.size() : 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setRecommendations(List<Recommendation> recommendations) {
        this.recommendations = recommendations;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder pour le RecyclerView.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView coverImageView;
        ImageView profileImageView;
        TextView titleTextView;
        TextView userNameTextView;
        TextView likesCountTextView;
        TextView dateTextView;
        TextView userRating;

        ViewHolder(View itemView) {
            super(itemView);
            coverImageView = itemView.findViewById(R.id.recommendationCover);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            titleTextView = itemView.findViewById(R.id.recommendationTitle);
            userNameTextView = itemView.findViewById(R.id.recommendationUser);
            likesCountTextView = itemView.findViewById(R.id.likeCounter);
            dateTextView = itemView.findViewById(R.id.recommendationDate);
            userRating = itemView.findViewById(R.id.userRating);
        }
    }

    /**
     * Classe interne pour représenter un utilisateur.
     */
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
