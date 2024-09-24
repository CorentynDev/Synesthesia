package com.example.synesthesia;

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

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
    private List<Comment> comments;

    public CommentsAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);

        holder.commentTextView.setText(comment.getCommentText());
        holder.usernameTextView.setText(comment.getUsername()); // Affiche le nom de l'utilisateur

        if (comment.getProfileImageUrl() != null) {
            Glide.with(holder.profileImageView.getContext())
                    .load(comment.getProfileImageUrl())
                    .placeholder(R.drawable.placeholder_image) // Image par défaut si aucune image n'est trouvée
                    .into(holder.profileImageView); // Affiche l'image de profil
        } else {
            holder.profileImageView.setImageResource(R.drawable.placeholder_image);
        }

        // Affiche la date ou l'heure du commentaire
        holder.timestampTextView.setText(getTimeAgo(comment.getTimestamp())); // Utilisez votre méthode pour afficher le temps
    }

    private String getTimeAgo(Timestamp timestamp) {
        if (timestamp == null) return "Juste maintenant";

        long time = timestamp.toDate().getTime();
        long now = System.currentTimeMillis();
        final long diff = now - time;

        if (diff < 60000) return "Juste maintenant"; // moins d'une minute
        else if (diff < 3600000) return (diff / 60000) + " minutes ago"; // moins d'une heure
        else if (diff < 86400000) return (diff / 3600000) + " heures ago"; // moins d'un jour
        else return (diff / 86400000) + " jours ago"; // plus d'un jour
    }


    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView commentTextView;
        TextView usernameTextView; // Pour afficher le nom de l'utilisateur
        ImageView profileImageView; // Pour afficher l'image de profil
        TextView timestampTextView; // Ajoutez cette ligne

        public CommentViewHolder(View itemView) {
            super(itemView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView); // Assurez-vous que cela existe dans votre layout
            profileImageView = itemView.findViewById(R.id.profileImageView); // Assurez-vous que cela existe dans votre layout
            timestampTextView = itemView.findViewById(R.id.timestampTextView); // Ajoutez cette ligne
        }
    }

}