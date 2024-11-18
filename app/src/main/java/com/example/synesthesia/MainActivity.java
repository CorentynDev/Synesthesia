package com.example.synesthesia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.synesthesia.utilities.FooterUtils;
import com.example.synesthesia.utilities.RecommendationsUtils;
import com.example.synesthesia.utilities.UserUtils;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    RecommendationsUtils recommendationsUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        recommendationsUtils = new RecommendationsUtils(db);

        FooterUtils.setupFooter(this, R.id.homeButton);

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        LinearLayout recommendationList = findViewById(R.id.recommendationList);

        swipeRefreshLayout.setOnRefreshListener(() -> recommendationsUtils.getRecommendationData(this, recommendationList, swipeRefreshLayout));

        recommendationsUtils.getRecommendationData(this, recommendationList, swipeRefreshLayout);
    }

    @Override
    protected void onStart() {
        super.onStart();
        UserUtils userUtils = new UserUtils();
        if (!userUtils.isUserLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
