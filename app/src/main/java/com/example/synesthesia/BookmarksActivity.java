package com.example.synesthesia;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.synesthesia.models.Recommendation;
import com.example.synesthesia.utilities.FooterUtils;
import com.example.synesthesia.utilities.RecommendationsUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class BookmarksActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private RecommendationsUtils recommendationsUtils;
    private LinearLayout linearLayoutBookmarks;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        FooterUtils.setupFooter(this, R.id.bookmarkButton);

        db = FirebaseFirestore.getInstance();
        recommendationsUtils = new RecommendationsUtils(db);

        linearLayoutBookmarks = findViewById(R.id.linearLayoutBookmarks);
        emptyView = findViewById(R.id.emptyView);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            loadBookmarkedRecommendations(userId);
        } else {
            Log.e("BookmarksActivity", "User not logged in");
            showEmptyMessage();
        }
    }

    private void loadBookmarkedRecommendations(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> bookmarkedRecommendations = (List<String>) documentSnapshot.get("bookmarkedRecommendations");

                        if (bookmarkedRecommendations != null && !bookmarkedRecommendations.isEmpty()) {
                            loadRecommendations(bookmarkedRecommendations);
                        } else {
                            showEmptyMessage();
                        }
                    } else {
                        Log.d("BookmarksActivity", "User document does not exist");
                        showEmptyMessage();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("BookmarksActivity", "Error fetching user document", e);
                    showEmptyMessage();
                });
    }

    private void loadRecommendations(@NonNull List<String> recommendationIds) {
        linearLayoutBookmarks.removeAllViews();
        List<Recommendation> recommendations = new ArrayList<>();

        for (String recommendationId : recommendationIds) {
            db.collection("recommendations").document(recommendationId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Recommendation recommendation = documentSnapshot.toObject(Recommendation.class);
                            if (recommendation != null) {
                                recommendations.add(recommendation);
                            }
                        } else {
                            Log.d("BookmarksActivity", "Recommendation document does not exist");
                        }

                        if (recommendations.size() == recommendationIds.size()) {
                            recommendations.sort((r1, r2) -> r2.getTimestamp().compareTo(r1.getTimestamp()));

                            for (Recommendation recommendation : recommendations) {
                                recommendationsUtils.addRecommendationCard(this, linearLayoutBookmarks, recommendation, recommendationId);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("BookmarksActivity", "Error fetching recommendation", e));
        }
    }

    private void showEmptyMessage() {
        linearLayoutBookmarks.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }
}
