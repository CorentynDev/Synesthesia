package com.example.synesthesia;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.models.Recommendation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView userProfileImageView;
    private TextView userPseudoTextView, userEmailTextView;
    private RecommendationAdapter recommendationAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        userProfileImageView = findViewById(R.id.userProfileImageView);
        userPseudoTextView = findViewById(R.id.userPseudoTextView);
        userEmailTextView = findViewById(R.id.userEmailTextView);
        RecyclerView userRecommendationsRecyclerView = findViewById(R.id.userRecommendationsRecyclerView);

        userRecommendationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recommendationAdapter = new RecommendationAdapter(new ArrayList<>());
        userRecommendationsRecyclerView.setAdapter(recommendationAdapter);

        loadUserData();
    }

    private void loadUserData() {
        String userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        String pseudo = documentSnapshot.getString("username");
                        String email = documentSnapshot.getString("email");

                        if (profileImageUrl != null) {
                            Picasso.get().load(profileImageUrl).into(userProfileImageView);
                        }

                        userPseudoTextView.setText(pseudo);
                        userEmailTextView.setText(email);
                    }
                });

        loadUserRecommendations();
    }

    private void loadUserRecommendations() {
        String userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        firestore.collection("recommendations")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recommendation> recommendations = queryDocumentSnapshots.toObjects(Recommendation.class);
                    recommendationAdapter.setRecommendations(recommendations);
                });
    }
}