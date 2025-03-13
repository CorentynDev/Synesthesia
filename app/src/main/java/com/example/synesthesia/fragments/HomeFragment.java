package com.example.synesthesia.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.synesthesia.authentication.LoginActivity;
import com.example.synesthesia.R;
import com.example.synesthesia.utilities.RecommendationsUtils;
import com.example.synesthesia.utilities.UserUtils;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeFragment extends Fragment {

    private boolean isFollowingFilterActive = false;
    private RecommendationsUtils recommendationsUtils;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button filterMenuButton = view.findViewById(R.id.filterMenuButton);
        LinearLayout recommendationList = view.findViewById(R.id.recommendationList);
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        recommendationsUtils = new RecommendationsUtils(FirebaseFirestore.getInstance());

        filterMenuButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), filterMenuButton);
            MenuInflater inflater1 = popupMenu.getMenuInflater();
            inflater1.inflate(R.menu.filter_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.all_recommendations) {
                    recommendationsUtils.getRecommendationData(requireActivity(), recommendationList, swipeRefreshLayout, false);
                    filterMenuButton.setText(R.string.all_recommendations);
                    isFollowingFilterActive = false;
                    return true;
                } else if (id == R.id.followed_recommendations) {
                    recommendationsUtils.getRecommendationData(requireActivity(), recommendationList, swipeRefreshLayout, true);
                    filterMenuButton.setText(R.string.followed_recommendations);
                    isFollowingFilterActive = true;
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            recommendationsUtils.getRecommendationData(requireActivity(), recommendationList, swipeRefreshLayout, isFollowingFilterActive);

            if (isFollowingFilterActive) {
                filterMenuButton.setText(R.string.followed_recommendations);
            } else {
                filterMenuButton.setText(R.string.all_recommendations);
            }
        });

        recommendationsUtils.getRecommendationData(requireActivity(), recommendationList, swipeRefreshLayout, false);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        UserUtils userUtils = new UserUtils();
        if (!userUtils.isUserLoggedIn()) {
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (RecommendationsUtils.globalMediaPlayer != null && RecommendationsUtils.globalMediaPlayer.isPlaying()) {
            RecommendationsUtils.globalMediaPlayer.stop();
            RecommendationsUtils.globalMediaPlayer.release();
            RecommendationsUtils.globalMediaPlayer = null;
        }

        if (RecommendationsUtils.currentlyPlayingButton != null) {
            RecommendationsUtils.currentlyPlayingButton.setImageResource(R.drawable.bouton_de_lecture);
            RecommendationsUtils.currentlyPlayingButton = null;
        }
        RecommendationsUtils.currentlyPlayingUrl = null;
    }
}
