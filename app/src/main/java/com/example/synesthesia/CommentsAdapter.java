package com.example.synesthesia;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.synesthesia.models.Comment;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
    private List<Comment> comments;
    private FirebaseFirestore db; // Instance Firestore

    public CommentsAdapter(List<Comment> comments) {
        this.comments = comments;
        this.db = FirebaseFirestore.getInstance(); // Initialiser Firestore
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);

        holder.commentTextView.setText(comment.getCommentText());

        // Récupérer les informations de l'utilisateur à partir de Firestore en utilisant l'userId
        String userId = comment.getUserId();
        if (userId != null && !userId.isEmpty()) {
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                            // Afficher le pseudo
                            if (username != null) {
                                holder.usernameTextView.setText(username);
                            }

                            // Afficher l'image de profil
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(holder.profileImageView.getContext())
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.placeholder_image) // Image par défaut si aucune image n'est trouvée
                                        .into(holder.profileImageView);
                            } else {
                                holder.profileImageView.setImageResource(R.drawable.placeholder_image);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("CommentsAdapter", "Error fetching user data", e);
                    });
        } else {
            // Si l'ID utilisateur est manquant ou vide, utiliser une valeur par défaut
            holder.usernameTextView.setText("Utilisateur inconnu");
            holder.profileImageView.setImageResource(R.drawable.placeholder_image);
        }

        // Afficher la date ou l'heure du commentaire
        holder.timestampTextView.setText(getTimeAgo(comment.getTimestamp()));
    }

    private String getTimeAgo(Timestamp timestamp) {
        if (timestamp == null) {
            return "A l'instant";
        }

        long time = timestamp.toDate().getTime();
        long now = System.currentTimeMillis();

        if (time > now || time <= 0) {
            return "à l'instant";
        }

        final long diff = now - time;
        if (diff < 60 * 1000) {
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

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView commentTextView;
        TextView usernameTextView; // Pour afficher le nom de l'utilisateur
        ImageView profileImageView; // Pour afficher l'image de profil
        TextView timestampTextView; // Pour afficher le timestamp du commentaire

        public CommentViewHolder(View itemView) {
            super(itemView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView); // Assurez-vous que cela existe dans votre layout
            profileImageView = itemView.findViewById(R.id.profileImageView); // Assurez-vous que cela existe dans votre layout
            timestampTextView = itemView.findViewById(R.id.timestampTextView); // Ajoutez cette ligne
        }
    }
}
