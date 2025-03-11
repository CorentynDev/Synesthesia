package com.example.synesthesia;

import static com.example.synesthesia.utilities.UserUtils.db;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.synesthesia.models.Recommendation;
import com.example.synesthesia.utilities.FooterUtils;
import com.example.synesthesia.utilities.NotificationUtils;
import com.example.synesthesia.utilities.RecommendationsUtils;
import com.example.synesthesia.utilities.UserUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;

import com.example.synesthesia.R;


public class UserProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView userProfileImageView;
    private UserUtils userUtils;
    private RecommendationsUtils recommendationsUtils;
    private Button followButton;
    private TextView publicationCount;
    private TextView followerCount;
    private TextView followingCount;
    private LinearLayout followerLayout;
    private LinearLayout followingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialisation des TextViews
        publicationCount = findViewById(R.id.publicationCount);
        followerCount = findViewById(R.id.followerCount);
        followingCount = findViewById(R.id.followingCount);

        followerLayout = findViewById(R.id.followerLayout);
        followingLayout = findViewById(R.id.followingLayout);

        recommendationsUtils = new RecommendationsUtils(db);
        FooterUtils.setupFooter(this, R.id.profileButton);

        userProfileImageView = findViewById(R.id.userProfileImageView);
        LinearLayout linearLayoutUserRecommendations = findViewById(R.id.linearLayoutUserRecommendations);
        followButton = findViewById(R.id.followButton);


        userUtils = new UserUtils();

        String targetUserId = getIntent().getStringExtra("userId");
        if (targetUserId == null || targetUserId.isEmpty()) {
            targetUserId = userUtils.getCurrentUserId();
        }
        String finalTargetUserId = targetUserId;
        followerLayout.setOnClickListener(v -> openFollowerList(finalTargetUserId));
        followingLayout.setOnClickListener(v -> openFollowingList(finalTargetUserId));

        boolean isCurrentUser = targetUserId.equals(userUtils.getCurrentUserId());

        loadUserData(targetUserId, isCurrentUser);

        loadUserRecommendations(targetUserId, linearLayoutUserRecommendations);

        // Charger les données utilisateur
        loadUserData(targetUserId, isCurrentUser);
        loadUserRecommendations(targetUserId, linearLayoutUserRecommendations);

        // Charger les statistiques
        loadUserStats(targetUserId);

        Log.d("UserProfileActivity", "targetUserId: " + targetUserId);
        Log.d("UserProfileActivity", "currentUserId: " + userUtils.getCurrentUserId());

        if (!isCurrentUser) {
            Log.d("UserProfileActivity", "Affichage d'un autre profil.");
            findViewById(R.id.logoutButton).setVisibility(View.GONE);
            followButton.setVisibility(View.VISIBLE); // Afficher le bouton Suivre
            final String userIdToFollow = targetUserId; // Créer une variable finale
            followButton.setOnClickListener(v -> toggleFollowUser(userIdToFollow));

            // Vérifie si l'utilisateur est déjà suivi
            db.collection("followers")
                    .document(userUtils.getCurrentUserId())
                    .collection("following")
                    .document(userIdToFollow)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            followButton.setText("Suivi");
                        } else {
                            followButton.setText("Suivre");
                        }
                    })
                    .addOnFailureListener(e -> Log.e("CheckFollow", "Erreur lors de la vérification du suivi", e));

        } else {
            Log.d("UserProfileActivity", "Affichage de mon propre profil.");
            Button logoutButton = findViewById(R.id.logoutButton);
            logoutButton.setOnClickListener(v -> logoutUser());
            followButton.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_info_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.paramètre) { // ID de l'élément du menu
            Intent intent = new Intent(this, UserInfoActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
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

                        //userPseudoTextView.setText(pseudo);

                        // Charger l'image de profil
                        UserUtils.loadImageFromUrl(this, profileImageUrl, userProfileImageView);

                        // Si c'est l'utilisateur actuel, charge aussi son email
                        if (isCurrentUser) {
                            String email = documentSnapshot.getString("email");
                            //userEmailTextView.setText(email);
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
    private void toggleFollowUser(String userIdToFollow) {
        String currentUserId = userUtils.getCurrentUserId();

        // Vérifier si l'utilisateur est déjà suivi
        db.collection("followers")
                .document(currentUserId) // Document de l'utilisateur actuel
                .collection("following") // Collection des suivis
                .document(userIdToFollow) // ID de l'utilisateur à vérifier
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Utilisateur déjà suivi, désabonner
                        unfollowUser(userIdToFollow);
                    } else {
                        // Utilisateur non suivi, suivre
                        followUser(userIdToFollow);
                    }
                })
                .addOnFailureListener(e -> Log.e("ToggleFollowUser", "Erreur lors de la vérification du suivi", e));
    }

    private void followUser(String userIdToFollow) {
        String currentUserId = userUtils.getCurrentUserId();

        db.collection("followers")
                .document(currentUserId)
                .collection("following")
                .document(userIdToFollow)
                .set(new HashMap<>())
                .addOnSuccessListener(aVoid -> {
                    db.collection("followers")
                            .document(userIdToFollow)
                            .collection("followers")
                            .document(currentUserId)
                            .set(new HashMap<>())
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d("FollowUser", "Utilisateur suivi avec succès.");
                                followButton.setText("Suivi");
                                followButton.setEnabled(true);
                                Toast.makeText(this, "Vous suivez maintenant cet utilisateur.", Toast.LENGTH_SHORT).show();
                                // Mettre à jour les compteurs
                                loadUserStats(userIdToFollow);

                                // Envoyer une notification
                                sendFollowNotification(userIdToFollow);
                            })
                            .addOnFailureListener(e -> Log.e("FollowUser", "Erreur lors de l'ajout dans 'followers'", e));
                })
                .addOnFailureListener(e -> Log.e("FollowUser", "Erreur lors de l'ajout dans 'following'", e));
    }

    private void sendFollowNotification(String userIdToFollow) {
        // Récupérer le pseudo de l'utilisateur connecté
        UserUtils.getPseudo().addOnSuccessListener(username -> {
            // Récupérer les informations de l'utilisateur suivi
            db.collection("users").document(userIdToFollow).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fcmTokenToFollow = documentSnapshot.getString("fcmToken"); // Token FCM du suivi

                            if (fcmTokenToFollow != null) {
                                String title = "Nouveau follower!";
                                String message = username + " commence à vous suivre."; // Affiche le pseudo

                                // Envoyer la notification
                                NotificationUtils.sendNotificationFollow(this, fcmTokenToFollow, title, message);
                                Log.d("FCM", "Notification envoyée à " + userIdToFollow);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("FCM", "Erreur lors de la récupération du token de l'utilisateur suivi", e));
        }).addOnFailureListener(e -> Log.e("FCM", "Erreur lors de la récupération du pseudo de l'utilisateur connecté", e));
    }

    private void unfollowUser(String userIdToFollow) {
        String currentUserId = userUtils.getCurrentUserId();

        db.collection("followers")
                .document(currentUserId)
                .collection("following")
                .document(userIdToFollow)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    db.collection("followers")
                            .document(userIdToFollow)
                            .collection("followers")
                            .document(currentUserId)
                            .delete()
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d("UnfollowUser", "Utilisateur désabonné avec succès.");
                                followButton.setText("Suivre");
                                followButton.setEnabled(true);
                                Toast.makeText(this, "Vous ne suivez plus cet utilisateur.", Toast.LENGTH_SHORT).show();
                                // Mettre à jour les compteurs
                                loadUserStats(userIdToFollow);
                            })
                            .addOnFailureListener(e -> Log.e("UnfollowUser", "Erreur lors de la suppression dans 'followers'", e));
                })
                .addOnFailureListener(e -> Log.e("UnfollowUser", "Erreur lors de la suppression dans 'following'", e));
    }

    private void loadUserStats(String userId) {
        // Charger le nombre de publications
        db.collection("recommendations")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int publicationCountValue = querySnapshot.size();
                    publicationCount.setText(String.valueOf(publicationCountValue));
                })
                .addOnFailureListener(e -> Log.e("LoadUserStats", "Erreur lors du chargement des publications", e));

        // Charger le nombre de followers
        db.collection("followers")
                .document(userId)
                .collection("followers")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int followerCountValue = querySnapshot.size();
                    followerCount.setText(String.valueOf(followerCountValue));
                })
                .addOnFailureListener(e -> Log.e("LoadUserStats", "Erreur lors du chargement des followers", e));

        // Charger le nombre de following
        db.collection("followers")
                .document(userId)
                .collection("following")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int followingCountValue = querySnapshot.size();
                    followingCount.setText(String.valueOf(followingCountValue));
                })
                .addOnFailureListener(e -> Log.e("LoadUserStats", "Erreur lors du chargement des following", e));
    }

    private void openFollowerList(String userId) {
        Intent intent = new Intent(this, UserListActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("type", "followers"); // Identifier qu'on veut afficher les followers
        startActivity(intent);
    }

    private void openFollowingList(String userId) {
        Intent intent = new Intent(this, UserListActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("type", "following"); // Identifier qu'on veut afficher les personnes suivies
        startActivity(intent);
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