package com.example.synesthesia;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.synesthesia.utilities.RecommendationsUtils;
import com.example.synesthesia.utilities.UserUtils;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    UserUtils userUtils = new UserUtils();
    RecommendationsUtils recommendationsUtils;

    ImageView profileImageView;
    TextView profileSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Set the layout first

        db = FirebaseFirestore.getInstance();
        recommendationsUtils = new RecommendationsUtils(db);

        // Initialize views only after setContentView()
        profileImageView = findViewById(R.id.profileImageView);
        profileSummary = findViewById(R.id.profileSummary);

        ImageView profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });

        ImageView bookmarkRecommendationButton = findViewById(R.id.bookmarkButton);
        bookmarkRecommendationButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, BookmarksActivity.class);
            startActivity(intent);
        });

        ImageView createRecommendationButton = findViewById(R.id.createRecommendationButton);
        createRecommendationButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Choisissez un type de recommandation");

            String[] types = {"Musique", "Film", "Jeux Vidéo", "Livre"};

            builder.setItems(types, (dialog, which) -> {
                switch (which) {
                    case 0:
                        Intent musicIntent = new Intent(MainActivity.this, SearchMusicActivity.class);
                        startActivity(musicIntent);
                        break;
                    case 1:
                        // New filmIntent
                        break;
                    case 2:
                        // New videoGameIntent
                        break;
                    case 3:
                        Intent bookIntent = new Intent(MainActivity.this, SearchBookActivity.class);
                        startActivity(bookIntent);
                        break;
                }
            });
            builder.create().show();
        });

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        LinearLayout recommendationList = findViewById(R.id.recommendationList);

        swipeRefreshLayout.setOnRefreshListener(() -> recommendationsUtils.getRecommendationData(this, recommendationList, swipeRefreshLayout));

        recommendationsUtils.getRecommendationData(this, recommendationList, swipeRefreshLayout);
        userUtils.getUserProfile(profileImageView, profileSummary);  // Load profile data here
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!userUtils.isUserLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
