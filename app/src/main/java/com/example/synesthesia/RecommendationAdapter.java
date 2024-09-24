package com.example.synesthesia;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.models.Recommendation;
import com.google.firebase.Timestamp;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {

    private List<Recommendation> recommendations;

    public RecommendationAdapter(List<Recommendation> recommendations) {
        this.recommendations = recommendations;
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

        if (recommendation.getCoverUrl() != null && !recommendation.getCoverUrl().isEmpty()) {
            Picasso.get().load(recommendation.getCoverUrl()).into(holder.coverImageView);
        } else {
            holder.coverImageView.setImageResource(R.drawable.placeholder_image);
        }

        final List<String> likedBy;
        if (recommendation.getLikedBy() == null) {
            likedBy = new ArrayList<>();
            recommendation.setLikedBy(likedBy);
        } else {
            likedBy = recommendation.getLikedBy();
        }
        holder.likesCountTextView.setText(String.valueOf(likedBy.size()));

        // Date de publication
        if (recommendation.getTimestamp() != null) {
            String timeAgo = getTimeAgo(recommendation.getTimestamp());
            holder.dateTextView.setText(timeAgo);
        } else {
            holder.dateTextView.setText("Date inconnue");
        }
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
        TextView titleTextView;
        TextView likesCountTextView;
        TextView dateTextView;

        ViewHolder(View itemView) {
            super(itemView);
            coverImageView = itemView.findViewById(R.id.recommendationCover);
            titleTextView = itemView.findViewById(R.id.recommendationTitle);
            likesCountTextView = itemView.findViewById(R.id.likeCounter);
            dateTextView = itemView.findViewById(R.id.recommendationDate);
        }
    }

    private String getTimeAgo(Timestamp timestamp) {
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
}