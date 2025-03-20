package com.example.synesthesia.fragments;

import static com.example.synesthesia.utilities.UserUtils.db;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.synesthesia.MainActivity;
import com.example.synesthesia.R;
import com.example.synesthesia.authentication.LoginActivity;
import com.example.synesthesia.firebase.MyFirebaseMessagingService;
import com.example.synesthesia.models.Recommendation;
import com.example.synesthesia.utilities.FooterUtils;
import com.example.synesthesia.utilities.NotificationUtils;
import com.example.synesthesia.utilities.RecommendationsUtils;
import com.example.synesthesia.utilities.UserUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;

public class UserProfileFragment extends Fragment {

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
    private boolean isCurrentUser;

    public UserProfileFragment() {
        // Required empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem parametreItem = menu.findItem(R.id.paramètre);
        MenuItem deconectionItem = menu.findItem(R.id.deconection);
        if (parametreItem != null && deconectionItem != null) {
            parametreItem.setVisible(isCurrentUser); // Rendre l'élément visible si c'est le profil de l'utilisateur connecté
            deconectionItem.setVisible(isCurrentUser); // Rendre l'élément visible si c'est le profil de l'utilisateur connecté
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        publicationCount = view.findViewById(R.id.publicationCount);
        followerCount = view.findViewById(R.id.followerCount);
        followingCount = view.findViewById(R.id.followingCount);
        followerLayout = view.findViewById(R.id.followerLayout);
        followingLayout = view.findViewById(R.id.followingLayout);
        userProfileImageView = view.findViewById(R.id.userProfileImageView);
        LinearLayout linearLayoutUserRecommendations = view.findViewById(R.id.linearLayoutUserRecommendations);
        followButton = view.findViewById(R.id.followButton);

        FooterUtils.setupFooter(requireActivity(), R.id.profileButton);

        userUtils = new UserUtils();
        recommendationsUtils = new RecommendationsUtils(db);

        Bundle args = getArguments();
        String targetUserId = null;
        if (args != null) {
            targetUserId = args.getString("userId");
        }

        if (targetUserId == null || targetUserId.isEmpty()) {
            targetUserId = userUtils.getCurrentUserId();
        }

        isCurrentUser = targetUserId.equals(userUtils.getCurrentUserId());

        final String finalTargetUserId = targetUserId;
        followerLayout.setOnClickListener(v -> openFollowerList(finalTargetUserId));
        followingLayout.setOnClickListener(v -> openFollowingList(finalTargetUserId));

        loadUserData(targetUserId, isCurrentUser);
        loadUserRecommendations(targetUserId, linearLayoutUserRecommendations);
        loadUserStats(targetUserId);

        if (!isCurrentUser) {
            followButton.setVisibility(View.VISIBLE);
            followButton.setEnabled(false); // Désactiver le bouton temporairement pendant la vérification
            checkIfFollowing(finalTargetUserId);
            followButton.setOnClickListener(v -> toggleFollowUser(finalTargetUserId));
        } else {
            followButton.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.paramètre) { // ID de l'élément du menu
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showUserInfoFragment();
            }
            return true;
        } else if (id == R.id.deconection) {
            logoutUser();
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
                        recommendationsUtils.addRecommendationCard(requireContext(), linearLayoutUserRecommendations, recommendation, document.getId());
                    }
                })
                .addOnFailureListener(e -> Log.e("LoadRecommendations", "Erreur lors du chargement des recommandations", e));
    }

    private void loadUserStats(String userId) {
        db.collection("recommendations").whereEqualTo("userId", userId).get().addOnSuccessListener(querySnapshot -> {
            int publicationCountValue = querySnapshot.size();
            publicationCount.setText(String.valueOf(publicationCountValue));
        }).addOnFailureListener(e -> Log.e("LoadUserStats", "Erreur lors du chargement des publications", e));

        db.collection("followers").document(userId).collection("followers").get().addOnSuccessListener(querySnapshot -> {
            int followerCountValue = querySnapshot.size();
            followerCount.setText(String.valueOf(followerCountValue));
        }).addOnFailureListener(e -> Log.e("LoadUserStats", "Erreur lors du chargement des followers", e));

        db.collection("followers").document(userId).collection("following").get().addOnSuccessListener(querySnapshot -> {
            int followingCountValue = querySnapshot.size();
            followingCount.setText(String.valueOf(followingCountValue));
        }).addOnFailureListener(e -> Log.e("LoadUserStats", "Erreur lors du chargement des following", e));
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(requireContext(), LoginActivity.class));
        requireActivity().finish();
    }

    private void loadUserData(String userId, boolean isCurrentUser) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String pseudo = documentSnapshot.getString("username");
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        UserUtils.loadImageFromUrl(requireContext(), profileImageUrl, userProfileImageView);
                        // Si c'est l'utilisateur actuel, charge aussi son email
                        if (isCurrentUser) {
                            String email = documentSnapshot.getString("email");
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("LoadUserData", "Erreur lors du chargement des données utilisateur", e));
    }

    private void toggleFollowUser(String userIdToFollow) {
        String currentUserId = userUtils.getCurrentUserId();
        db.collection("followers").document(currentUserId).collection("following").document(userIdToFollow)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        unfollowUser(userIdToFollow);
                    } else {
                        followUser(userIdToFollow);
                    }
                });
    }

    private void followUser(String userIdToFollow) {
        String currentUserId = userUtils.getCurrentUserId();
        db.collection("followers").document(currentUserId).collection("following").document(userIdToFollow).set(new HashMap<>()).addOnSuccessListener(aVoid -> {
            db.collection("followers").document(userIdToFollow).collection("followers").document(currentUserId).set(new HashMap<>()).addOnSuccessListener(aVoid2 -> {
                Log.d("FollowUser", "Utilisateur suivi avec succès.");
                followButton.setText("Suivi");
                followButton.setEnabled(true);
                Toast.makeText(requireContext(), "Vous suivez maintenant cet utilisateur.", Toast.LENGTH_SHORT).show();
                // Mettre à jour les compteurs
                loadUserStats(userIdToFollow);

                // Envoyer une notification
                sendFollowNotification(userIdToFollow);
            }).addOnFailureListener(e -> Log.e("FollowUser", "Erreur lors de l'ajout dans 'followers'", e));
        }).addOnFailureListener(e -> Log.e("FollowUser", "Erreur lors de l'ajout dans 'following'", e));
    }

    private void sendFollowNotification(String userIdToFollow) {
        // Récupérer le pseudo de l'utilisateur connecté
        UserUtils.getPseudo().addOnSuccessListener(username -> {
            // Récupérer les informations de l'utilisateur suivi
            db.collection("users").document(userIdToFollow).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String fcmTokenToFollow = documentSnapshot.getString("fcmToken"); // Token FCM du suivi

                    if (fcmTokenToFollow != null) {
                        String title = "Nouveau follower!";
                        String message = username + " commence à vous suivre."; // Affiche le pseudo

                        // Envoyer la notification
                        NotificationUtils.sendNotification(requireContext(), fcmTokenToFollow, title, message);
                        Log.d("FCM", "Notification envoyée à " + userIdToFollow);
                        MyFirebaseMessagingService.saveNotificationToFirestore(userIdToFollow, title, message);
                    }
                }
            }).addOnFailureListener(e -> Log.e("FCM", "Erreur lors de la récupération du token de l'utilisateur suivi", e));
        }).addOnFailureListener(e -> Log.e("FCM", "Erreur lors de la récupération du pseudo de l'utilisateur connecté", e));
    }

    private void unfollowUser(String userIdToFollow) {
        String currentUserId = userUtils.getCurrentUserId();
        db.collection("followers").document(currentUserId).collection("following").document(userIdToFollow).delete().addOnSuccessListener(aVoid -> {
            db.collection("followers").document(userIdToFollow).collection("followers").document(currentUserId).delete().addOnSuccessListener(aVoid2 -> {
                Log.d("UnfollowUser", "Utilisateur désabonné avec succès.");
                followButton.setText("Suivre");
                followButton.setEnabled(true);
                Toast.makeText(requireContext(), "Vous ne suivez plus cet utilisateur.", Toast.LENGTH_SHORT).show();
                // Mettre à jour les compteurs
                loadUserStats(userIdToFollow);
            }).addOnFailureListener(e -> Log.e("UnfollowUser", "Erreur lors de la suppression dans 'followers'", e));
        }).addOnFailureListener(e -> Log.e("UnfollowUser", "Erreur lors de la suppression dans 'following'", e));
    }

    private void openFollowerList(String userId) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showUserListFragment(userId, "followers");
        }
    }

    private void openFollowingList(String userId) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showUserListFragment(userId, "following");
        }
    }

    private void checkIfFollowing(String targetUserId) {
        String currentUserId = userUtils.getCurrentUserId();
        db.collection("followers")
                .document(currentUserId)
                .collection("following")
                .document(targetUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        followButton.setText("Suivi");
                    } else {
                        followButton.setText("Suivre");
                    }
                    followButton.setEnabled(true);
                })
                .addOnFailureListener(e -> Log.e("CheckFollow", "Erreur lors de la vérification du suivi", e));
    }

}
