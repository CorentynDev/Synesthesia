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
import com.example.synesthesia.utilities.FooterUtils;
import com.example.synesthesia.utilities.RecommendationsUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BookmarkFragment extends Fragment {
    private FirebaseFirestore db;
    private RecommendationsUtils recommendationsUtils;
    private LinearLayout linearLayoutBookmarks;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookmark, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FooterUtils.setupFooter(requireActivity(), R.id.bookmarkButton);

        db = FirebaseFirestore.getInstance();
        recommendationsUtils = new RecommendationsUtils(db);

        linearLayoutBookmarks = view.findViewById(R.id.linearLayoutBookmarks);
        emptyView = view.findViewById(R.id.emptyView);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            loadBookmarkedRecommendations(userId);
        } else {
            Log.e("BookmarksFragment", "User not logged in");
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
                        Log.d("BookmarksFragment", "User document does not exist");
                        showEmptyMessage();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("BookmarksFragment", "Error fetching user document", e);
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
                            Log.d("BookmarksFragment", "Recommendation document does not exist");
                        }

                        if (recommendations.size() == recommendationIds.size()) {
                            recommendations.sort((r1, r2) -> r2.getTimestamp().compareTo(r1.getTimestamp()));

                            for (Recommendation recommendation : recommendations) {
                                recommendationsUtils.addRecommendationCard(getContext(), linearLayoutBookmarks, recommendation, recommendationId);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("BookmarksFragment", "Error fetching recommendation", e));
        }
    }

    private void showEmptyMessage() {
        linearLayoutBookmarks.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Arrêter la musique si elle est en cours de lecture
        if (RecommendationsUtils.globalMediaPlayer != null && RecommendationsUtils.globalMediaPlayer.isPlaying()) {
            RecommendationsUtils.globalMediaPlayer.stop();
            RecommendationsUtils.globalMediaPlayer.release();  // Libérer les ressources du MediaPlayer
            RecommendationsUtils.globalMediaPlayer = null;  // Réinitialiser le MediaPlayer
        }

        // Réinitialiser le bouton de lecture/pause de la musique en cours
        if (RecommendationsUtils.currentlyPlayingButton != null) {
            RecommendationsUtils.currentlyPlayingButton.setImageResource(R.drawable.bouton_de_lecture);  // Icône de lecture
            RecommendationsUtils.currentlyPlayingButton = null;
        }
        RecommendationsUtils.currentlyPlayingUrl = null;
    }
}
