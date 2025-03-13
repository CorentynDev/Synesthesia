package com.example.synesthesia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_info_menu, menu);
        MenuItem parametreItem = menu.findItem(R.id.paramètre); // Remplacez `R.id.parametre` par l'ID réel de l'élément
        if (parametreItem != null) {
            parametreItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.notif) {
            Intent intent = new Intent(this, NotifActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onPause() {
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
