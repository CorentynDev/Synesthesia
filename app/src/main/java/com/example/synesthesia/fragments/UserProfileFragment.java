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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.synesthesia.MainActivity;
import com.example.synesthesia.R;
import com.example.synesthesia.authentication.LoginActivity;
import com.example.synesthesia.models.Recommendation;
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

    public UserProfileFragment() {
        // Required empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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

        final String finalTargetUserId = targetUserId;
        followerLayout.setOnClickListener(v -> openFollowerList(finalTargetUserId));
        followingLayout.setOnClickListener(v -> openFollowingList(finalTargetUserId));

        boolean isCurrentUser = targetUserId.equals(userUtils.getCurrentUserId());

        loadUserData(targetUserId, isCurrentUser);
        loadUserRecommendations(targetUserId, linearLayoutUserRecommendations);
        loadUserStats(targetUserId);

        if (!isCurrentUser) {
            view.findViewById(R.id.logoutButton).setVisibility(View.GONE);
            followButton.setVisibility(View.VISIBLE);
            followButton.setOnClickListener(v -> toggleFollowUser(finalTargetUserId));
        } else {
            Button logoutButton = view.findViewById(R.id.logoutButton);
            logoutButton.setOnClickListener(v -> logoutUser());
            followButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.user_info_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.paramètre) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showUserInfoFragment();
            }
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
        db.collection("recommendations")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int publicationCountValue = querySnapshot.size();
                    publicationCount.setText(String.valueOf(publicationCountValue));
                })
                .addOnFailureListener(e -> Log.e("LoadUserStats", "Erreur lors du chargement des publications", e));

        db.collection("followers")
                .document(userId)
                .collection("followers")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int followerCountValue = querySnapshot.size();
                    followerCount.setText(String.valueOf(followerCountValue));
                })
                .addOnFailureListener(e -> Log.e("LoadUserStats", "Erreur lors du chargement des followers", e));

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

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(requireContext(), LoginActivity.class));
        requireActivity().finish();
    }

    private void loadUserData(String userId, boolean isCurrentUser) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        UserUtils.loadImageFromUrl(requireContext(), profileImageUrl, userProfileImageView);
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
        db.collection("followers").document(currentUserId).collection("following").document(userIdToFollow)
                .set(new HashMap<>())
                .addOnSuccessListener(aVoid -> {
                    followButton.setText("Suivi");
                });
    }

    private void unfollowUser(String userIdToFollow) {
        String currentUserId = userUtils.getCurrentUserId();
        db.collection("followers").document(currentUserId).collection("following").document(userIdToFollow)
                .delete().addOnSuccessListener(aVoid -> {
                    followButton.setText("Suivre");
                });
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
}
