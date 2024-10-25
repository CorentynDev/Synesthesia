package com.example.synesthesia;

import static com.example.synesthesia.utilities.UserUtils.db;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.models.Recommendation;
import com.example.synesthesia.utilities.FooterUtils;
import com.example.synesthesia.utilities.RecommendationsUtils;
import com.example.synesthesia.utilities.UserUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView userProfileImageView;
    private TextView userPseudoTextView;
    private TextView userEmailTextView;
    private UserUtils userUtils;
    private RecommendationsUtils recommendationsUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        recommendationsUtils = new RecommendationsUtils(db);
        FooterUtils.setupFooter(this, R.id.profileButton);

        userProfileImageView = findViewById(R.id.userProfileImageView);
        userPseudoTextView = findViewById(R.id.userPseudoTextView);
        userEmailTextView = findViewById(R.id.userEmailTextView);
        TextView userPasswordTextView = findViewById(R.id.userPasswordTextView);
        LinearLayout linearLayoutUserRecommendations = findViewById(R.id.linearLayoutUserRecommendations);

        userUtils = new UserUtils();

        userUtils.loadUserData(userProfileImageView, userPseudoTextView, userEmailTextView);

        userPseudoTextView.setOnClickListener(v -> userUtils.showEditPseudoDialog(this, userPseudoTextView));
        userEmailTextView.setOnClickListener(v -> userUtils.showEditEmailDialog(this, userEmailTextView));
        userProfileImageView.setOnClickListener(v -> showEditProfileImageDialog());
        userPasswordTextView.setOnClickListener(v -> userUtils.showChangePasswordDialog(this));

        loadUserRecommendations(linearLayoutUserRecommendations);
    }

    private void loadUserRecommendations(LinearLayout linearLayoutUserRecommendations) {
        String userId = userUtils.getCurrentUserId();

        db.collection("recommendations")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    linearLayoutUserRecommendations.removeAllViews();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recommendation recommendation = document.toObject(Recommendation.class);
                        // Passer l'ID de la recommandation ici
                        recommendationsUtils.addRecommendationCard(this, linearLayoutUserRecommendations, recommendation, document.getId());
                    }
                })
                .addOnFailureListener(e -> Log.e("LoadRecommendations", "Erreur lors du chargement des recommandations", e));
    }


    private void showEditProfileImageDialog() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            userUtils.uploadProfileImage(this, imageUri, userProfileImageView);
        }
    }
}
