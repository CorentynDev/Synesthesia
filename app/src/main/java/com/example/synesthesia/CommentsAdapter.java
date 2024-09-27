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
    private final List<Comment> comments;
    private final FirebaseFirestore db;

    public CommentsAdapter(List<Comment> comments) {
        this.comments = comments;
        this.db = FirebaseFirestore.getInstance();
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

        String userId = comment.getUserId();
        if (userId != null && !userId.isEmpty()) {
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                            if (username != null) {
                                holder.usernameTextView.setText(username);
                            }

                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(holder.profileImageView.getContext())
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.placeholder_image)
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
            holder.usernameTextView.setText("Utilisateur inconnu");
            holder.profileImageView.setImageResource(R.drawable.placeholder_image);
        }

        holder.timestampTextView.setText(getTimeAgo(comment.getTimestamp()));
    }

    private String getTimeAgo(Timestamp timestamp) {
        if (timestamp == null) {
            return "Maintenant";
        }

        long time = timestamp.toDate().getTime();
        long now = System.currentTimeMillis();

        if (time > now || time <= 0) {
            return "Maintenant";
        }

        final long diff = now - time;
        if (diff < 60 * 1000) {
            return "Il y a " + diff / 1000 + " seconde(s)";
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
        TextView usernameTextView;
        ImageView profileImageView;
        TextView timestampTextView;

        public CommentViewHolder(View itemView) {
            super(itemView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}
