package com.example.synesthesia.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.synesthesia.R;
import com.example.synesthesia.models.Recommendation;
import com.example.synesthesia.utilities.RecommendationsUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BookmarksFragment extends Fragment {
    private FirebaseFirestore db;
    private RecommendationsUtils recommendationsUtils;
    private LinearLayout linearLayoutBookmarks;
    private TextView emptyView;
    private List<String> bookmarkedRecommendations;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_bookmarks, container, false);

        db = FirebaseFirestore.getInstance();
        recommendationsUtils = new RecommendationsUtils(db);

        // Lier les vues
        linearLayoutBookmarks = rootView.findViewById(R.id.linearLayoutBookmarks);
        emptyView = rootView.findViewById(R.id.emptyView);

        // Charger les données pour l'utilisateur connecté
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            listenToBookmarks(userId);
        } else {
            Log.e("BookmarksFragment", "User not logged in");
            showEmptyMessage();
        }

        return rootView;
    }

    private void listenToBookmarks(String userId) {
        db.collection("users").document(userId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e("BookmarksFragment", "Error listening to bookmarks", error);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        List<String> newBookmarkedRecommendations = (List<String>) snapshot.get("bookmarkedRecommendations");

                        if (newBookmarkedRecommendations != null && !newBookmarkedRecommendations.isEmpty()) {
                            if (!newBookmarkedRecommendations.equals(bookmarkedRecommendations)) {
                                bookmarkedRecommendations = new ArrayList<>(newBookmarkedRecommendations);
                                loadRecommendations(bookmarkedRecommendations);
                            }
                        } else {
                            bookmarkedRecommendations = new ArrayList<>();
                            showEmptyMessage();
                        }
                    } else {
                        Log.d("BookmarksFragment", "User document does not exist or is empty");
                        showEmptyMessage();
                    }
                });
    }

    private void loadRecommendations(@NonNull List<String> recommendationIds) {
        linearLayoutBookmarks.removeAllViews();

        if (recommendationIds.isEmpty()) {
            showEmptyMessage();
            return;
        }

        db.collection("recommendations")
                .whereIn(FieldPath.documentId(), recommendationIds)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Log.e("BookmarksFragment", "Error fetching recommendations", error);
                        return;
                    }

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        linearLayoutBookmarks.removeAllViews();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Recommendation recommendation = document.toObject(Recommendation.class);
                            String documentId = document.getId();

                            recommendationsUtils.addRecommendationCard(
                                    getContext(),
                                    linearLayoutBookmarks,
                                    recommendation,
                                    documentId
                            );
                        }
                    } else {
                        showEmptyMessage();
                    }
                });
    }

    private void showEmptyMessage() {
        linearLayoutBookmarks.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }
}
