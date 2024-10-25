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

import com.example.synesthesia.models.Comment;
import com.example.synesthesia.models.Recommendation;
import com.example.synesthesia.utilities.TimeUtils;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;

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
        holder.titleTextView.setText(recommendation.getTitle());

        // Charger l'image de couverture
        if (recommendation.getCoverUrl() != null && !recommendation.getCoverUrl().isEmpty()) {
            Picasso.get().load(recommendation.getCoverUrl()).into(holder.coverImageView);
        } else {
            holder.coverImageView.setImageResource(R.drawable.placeholder_image);
        }

        String userNote = recommendation.getUserNote();
        Log.d(TAG, "User note: " + userNote);
        if (userNote != null && !userNote.isEmpty()) {
            holder.userRating.setText(userNote);
            Log.d(TAG, "User note set to: " + userNote);
        } else {
            holder.userRating.setText("Pas de note");
            Log.d(TAG, "User note is null or empty, setting to default message.");
        }


        // Récupérer l'ID utilisateur associé à la recommandation
        String userId = recommendation.getUserId();

        if (userId != null && !userId.isEmpty()) {
            // Vérifier si l'utilisateur est dans le cache
            if (userCache.containsKey(userId)) {
                User user = userCache.get(userId);
                bindUserData(holder, user);
            } else {
                // Récupérer les informations de l'utilisateur depuis Firestore
                db.collection("users").document(userId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                                String userName = documentSnapshot.getString("username");
                                User user = new User(profileImageUrl, userName); // Classe User définie plus bas
                                userCache.put(userId, user); // Ajouter dans le cache
                                bindUserData(holder, user);
                            } else {
                                // Gestion des cas où l'utilisateur n'existe pas
                                holder.userNameTextView.setText("Utilisateur inconnu");
                                holder.profileImageView.setImageResource(R.drawable.default_profil_picture);
                            }
                        })
                        .addOnFailureListener(e -> {
                            holder.userNameTextView.setText("Erreur de chargement");
                            holder.profileImageView.setImageResource(R.drawable.default_profil_picture);
                        });
            }
        } else {
            // Si l'userId est null ou vide, afficher des informations par défaut
            holder.userNameTextView.setText("Utilisateur inconnu");
            holder.profileImageView.setImageResource(R.drawable.default_profil_picture);
        }

        // Affichage des likes
        List<String> likedBy = recommendation.getLikedBy() != null ? recommendation.getLikedBy() : new ArrayList<>();
        holder.likesCountTextView.setText(String.valueOf(likedBy.size()));

        // Affichage de la date
        if (recommendation.getTimestamp() != null) {
            String timeAgo = TimeUtils.getTimeAgo(recommendation.getTimestamp());
            holder.dateTextView.setText(timeAgo);
        } else {
            holder.dateTextView.setText("Date inconnue");
        }
    }

    private void bindUserData(ViewHolder holder, User user) {
        // Méthode pour lier les données utilisateur
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Picasso.get().load(user.getProfileImageUrl()).into(holder.profileImageView);
        } else {
            holder.profileImageView.setImageResource(R.drawable.default_profil_picture);
        }
        holder.userNameTextView.setText(user.getUsername() != null ? user.getUsername() : "Utilisateur inconnu");
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

    // Classe pour stocker les données utilisateur
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