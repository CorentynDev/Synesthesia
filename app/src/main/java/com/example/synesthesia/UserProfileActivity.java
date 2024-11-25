package com.example.synesthesia;

import static com.example.synesthesia.utilities.UserUtils.db;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.synesthesia.models.Recommendation;
import com.example.synesthesia.utilities.FooterUtils;
import com.example.synesthesia.utilities.RecommendationsUtils;
import com.example.synesthesia.utilities.UserUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;

public class UserProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView userProfileImageView;
    private TextView userPseudoTextView;
    private TextView userEmailTextView;
    private UserUtils userUtils;
    private RecommendationsUtils recommendationsUtils;
    private Button followButton;

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
        followButton = findViewById(R.id.followButton);


        userUtils = new UserUtils();

        String targetUserId = getIntent().getStringExtra("userId");
        if (targetUserId == null || targetUserId.isEmpty()) {
            targetUserId = userUtils.getCurrentUserId();
        }

        boolean isCurrentUser = targetUserId.equals(userUtils.getCurrentUserId());

        loadUserData(targetUserId, isCurrentUser);

        loadUserRecommendations(targetUserId, linearLayoutUserRecommendations);

        Log.d("UserProfileActivity", "targetUserId: " + targetUserId);
        Log.d("UserProfileActivity", "currentUserId: " + userUtils.getCurrentUserId());

        if (!isCurrentUser) {
            Log.d("UserProfileActivity", "Affichage d'un autre profil.");
            userEmailTextView.setVisibility(View.GONE);
            userPasswordTextView.setVisibility(View.GONE);
            findViewById(R.id.logoutButton).setVisibility(View.GONE);
            followButton.setVisibility(View.VISIBLE); // Afficher le bouton Suivre
            final String userIdToFollow = targetUserId; // Créer une variable finale
            followButton.setOnClickListener(v -> followUser(userIdToFollow));

            db.collection("followers")
                    .document(userUtils.getCurrentUserId())
                    .collection("following")
                    .document(targetUserId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Log.d("UserProfileActivity", "L'utilisateur est déjà suivi.");
                            followButton.setText("Suivi");
                            followButton.setEnabled(false); // Désactiver le bouton
                        } else {
                            Log.d("UserProfileActivity", "L'utilisateur n'est pas suivi.");
                            followButton.setVisibility(View.VISIBLE); // Afficher le bouton pour suivre
                        }
                    })

                    .addOnFailureListener(e -> Log.e("CheckFollow", "Erreur lors de la vérification du suivi", e));

        } else {
            Log.d("UserProfileActivity", "Affichage de mon propre profil.");
            userPseudoTextView.setOnClickListener(v -> userUtils.showEditPseudoDialog(this, userPseudoTextView));
            userEmailTextView.setOnClickListener(v -> userUtils.showEditEmailDialog(this, userEmailTextView));
            userProfileImageView.setOnClickListener(v -> showEditProfileImageDialog());
            userPasswordTextView.setOnClickListener(v -> userUtils.showChangePasswordDialog(this));
            Button logoutButton = findViewById(R.id.logoutButton);
            logoutButton.setOnClickListener(v -> logoutUser());
            followButton.setVisibility(View.GONE);
        }
    }

    private void loadUserRecommendations(String userId, LinearLayout linearLayoutUserRecommendations) {
        db.collection("recommendations")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    linearLayoutUserRecommendations.removeAllViews();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recommendation recommendation = document.toObject(Recommendation.class);
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

    private void loadUserData(String userId, boolean isCurrentUser) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String pseudo = documentSnapshot.getString("username");
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                        userPseudoTextView.setText(pseudo);

                        // Charger l'image de profil
                        UserUtils.loadImageFromUrl(this, profileImageUrl, userProfileImageView);

                        // Si c'est l'utilisateur actuel, charge aussi son email
                        if (isCurrentUser) {
                            String email = documentSnapshot.getString("email");
                            userEmailTextView.setText(email);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("LoadUserData", "Erreur lors du chargement des données utilisateur", e));
    }

    private void logoutUser() {
        // Déconnexion de Firebase Auth
        FirebaseAuth.getInstance().signOut();

        // Redirection vers la page de connexion
        Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
        startActivity(intent);

        // Ferme l'activité actuelle
        finish();
    }
    private void followUser(String userIdToFollow) {
        // Référence à Firestore
        String currentUserId = userUtils.getCurrentUserId();

        // Ajouter l'utilisateur suivi dans la collection "following" de l'utilisateur actuel
        db.collection("followers")
                .document(currentUserId) // ID de l'utilisateur actuel
                .collection("following") // Collection des personnes suivies
                .document(userIdToFollow) // ID de l'utilisateur à suivre
                .set(new HashMap<>())// Ajout de l'objet vide ou des données supplémentaires si nécessaire
                .addOnSuccessListener(aVoid -> {
                    Log.d("FollowUser", "Ajout à 'following' réussi pour l'utilisateur actuel.");

                    // Ajouter l'utilisateur actuel dans la collection "followers" de l'utilisateur suivi
                    db.collection("followers")
                            .document(userIdToFollow) // ID de l'utilisateur suivi
                            .collection("followers") // Collection des abonnés
                            .document(currentUserId) // ID de l'utilisateur actuel
                            .set(new HashMap<>())// Ajout de l'objet vide
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d("FollowUser", "Ajout à 'followers' réussi pour l'utilisateur suivi.");
                                followButton.setText("Suivi"); // Mettre à jour le texte du bouton
                                followButton.setEnabled(false); // Désactiver le bouton
                                Toast.makeText(this, "Vous suivez maintenant cet utilisateur.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Log.e("FollowUser", "Erreur lors de l'ajout dans 'followers'", e));
                })
                .addOnFailureListener(e -> Log.e("FollowUser", "Erreur lors de l'ajout dans 'following'", e));
    }
}