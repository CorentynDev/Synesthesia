package com.example.synesthesia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.synesthesia.utilities.FooterUtils;
import com.example.synesthesia.utilities.RecommendationsUtils;
import com.example.synesthesia.utilities.UserUtils;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    UserUtils userUtils = new UserUtils();
    RecommendationsUtils recommendationsUtils;

    ImageView profileImageView;
    TextView profileSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        recommendationsUtils = new RecommendationsUtils(db);

        profileImageView = findViewById(R.id.profileImageView);
        profileSummary = findViewById(R.id.profileSummary);

        FooterUtils.setupFooter(this);

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        LinearLayout recommendationList = findViewById(R.id.recommendationList);

        swipeRefreshLayout.setOnRefreshListener(() -> recommendationsUtils.getRecommendationData(this, recommendationList, swipeRefreshLayout));

        recommendationsUtils.getRecommendationData(this, recommendationList, swipeRefreshLayout);
        userUtils.getUserProfile(profileImageView, profileSummary);
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
