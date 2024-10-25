package com.example.synesthesia;

import android.annotation.SuppressLint;
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
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.synesthesia.utilities.TimeUtils;

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

    @SuppressLint("SetTextI18n")
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

        holder.timestampTextView.setText(TimeUtils.getTimeAgo(comment.getTimestamp()));
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
