package com.example.synesthesia.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.synesthesia.R;
import com.example.synesthesia.adapters.RecommendationAdapter;
import com.example.synesthesia.authentication.LoginActivity;
import com.example.synesthesia.models.Recommendation;
import com.example.synesthesia.utilities.RecommendationsUtils;
import com.example.synesthesia.utilities.UserUtils;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private boolean isFollowingFilterActive = false;
    private RecommendationsUtils recommendationsUtils;
    private RecommendationAdapter recommendationAdapter;
    private List<Recommendation> recommendationList;
    private SwipeRefreshLayout swipeRefreshLayout; // Déclarez ici

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button filterMenuButton = view.findViewById(R.id.filterMenuButton);
        RecyclerView recommendationRecyclerView = view.findViewById(R.id.recommendationList);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout); // Initialisez ici

        recommendationsUtils = new RecommendationsUtils(FirebaseFirestore.getInstance());
        recommendationList = new ArrayList<>();

        // Set up RecyclerView
        recommendationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recommendationAdapter = new RecommendationAdapter(recommendationList);
        recommendationRecyclerView.setAdapter(recommendationAdapter);

        filterMenuButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), filterMenuButton);
            MenuInflater inflater1 = popupMenu.getMenuInflater();
            inflater1.inflate(R.menu.filter_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.all_recommendations) {
                    updateRecommendations(false);
                    filterMenuButton.setText(R.string.all_recommendations);
                    isFollowingFilterActive = false;
                    return true;
                } else if (id == R.id.followed_recommendations) {
                    updateRecommendations(true);
                    filterMenuButton.setText(R.string.followed_recommendations);
                    isFollowingFilterActive = true;
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            updateRecommendations(isFollowingFilterActive);
            if (isFollowingFilterActive) {
                filterMenuButton.setText(R.string.followed_recommendations);
            } else {
                filterMenuButton.setText(R.string.all_recommendations);
            }
        });

        updateRecommendations(false);

        return view;
    }

    private void updateRecommendations(boolean isFollowing) {
        recommendationsUtils.getRecommendationData(requireActivity(), recommendations -> {
            recommendationList.clear();
            recommendationList.addAll(recommendations);
            recommendationAdapter.notifyDataSetChanged();
        }, swipeRefreshLayout, isFollowing);
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
