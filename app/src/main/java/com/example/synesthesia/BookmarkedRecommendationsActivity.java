package com.example.synesthesia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.synesthesia.models.Recommendation;
import java.io.Serializable;
import java.util.List;

public class BookmarkedRecommendationsActivity extends AppCompatActivity {

    private LinearLayout bookmarkedList; // Layout that will contain bookmarked recommendations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarked_recommendations);

        bookmarkedList = findViewById(R.id.bookmarkedList);
        List<Recommendation> bookmarkedRecommendations = (List<Recommendation>) getIntent().getSerializableExtra("bookmarkedRecommendations");

        if (bookmarkedRecommendations != null) {
            for (Recommendation recommendation : bookmarkedRecommendations) {
                addBookmarkedRecommendationCard(recommendation);
            }
        }
    }

    private void addBookmarkedRecommendationCard(Recommendation recommendation) {
        // Inflate your recommendation card layout and populate it with the recommendation data
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.recommendation_card, bookmarkedList, false);

        TextView titleTextView = cardView.findViewById(R.id.recommendationTitle);
        titleTextView.setText(recommendation.getTitle());

        // Additional fields like date, user, etc.

        bookmarkedList.addView(cardView);
    }
}
