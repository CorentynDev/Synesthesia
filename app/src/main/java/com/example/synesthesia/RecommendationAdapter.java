package com.example.synesthesia;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.models.Recommendation;
import com.squareup.picasso.Picasso;

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
                .inflate(R.layout.item_recommendation, parent, false);
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

        holder.likesCountTextView.setText(String.valueOf("Likes number: " + recommendation.getLikesCount()));
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
        TextView commentsTextView;

        ViewHolder(View itemView) {
            super(itemView);
            coverImageView = itemView.findViewById(R.id.recommendationCoverImageView);
            titleTextView = itemView.findViewById(R.id.recommendationTitleTextView);
            likesCountTextView = itemView.findViewById(R.id.recommendationLikesCountTextView);
            commentsTextView = itemView.findViewById(R.id.recommendationCommentsTextView);
        }
    }
}
