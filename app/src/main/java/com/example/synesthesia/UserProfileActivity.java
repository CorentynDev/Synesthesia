package com.example.synesthesia;

import android.os.Bundle;
import android.util.Log;
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

public class UserProfileActivity extends AppCompatActivity {

    private ImageView userProfileImageView;
    private TextView userPseudoTextView, userEmailTextView;
    private RecyclerView userRecommendationsRecyclerView;
    private RecommendationAdapter recommendationAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize Firebase Auth and Firestore
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Bind views
        userProfileImageView = findViewById(R.id.userProfileImageView);
        userPseudoTextView = findViewById(R.id.userPseudoTextView);
        userEmailTextView = findViewById(R.id.userEmailTextView);
        userRecommendationsRecyclerView = findViewById(R.id.userRecommendationsRecyclerView);

        // Setup RecyclerView
        userRecommendationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Initialize adapter with an empty list
        recommendationAdapter = new RecommendationAdapter(new ArrayList<>());
        userRecommendationsRecyclerView.setAdapter(recommendationAdapter);

        // Load user data
        loadUserData();
    }

    private void loadUserData() {
        String userId = firebaseAuth.getCurrentUser().getUid();

        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        String pseudo = documentSnapshot.getString("username");
                        String email = documentSnapshot.getString("email");

                        if (profileImageUrl != null) {
                            Picasso.get().load(profileImageUrl).into(userProfileImageView);
                        }

                        Log.d("PROFIL", "Pseudonyme de l'utilisateur : " + pseudo);
                        userPseudoTextView.setText(pseudo);
                        userEmailTextView.setText(email);
                    }
                });

        loadUserRecommendations();
    }

    private void loadUserRecommendations() {
        String userId = firebaseAuth.getCurrentUser().getUid();

        firestore.collection("recommendations")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recommendation> recommendations = queryDocumentSnapshots.toObjects(Recommendation.class);
                    recommendationAdapter.setRecommendations(recommendations);
                });
    }
}