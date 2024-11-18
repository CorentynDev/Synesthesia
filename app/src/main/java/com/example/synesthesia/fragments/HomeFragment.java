package com.example.synesthesia.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.synesthesia.R;
import com.example.synesthesia.utilities.RecommendationsUtils;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeFragment extends Fragment {

    private RecommendationsUtils recommendationsUtils;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout recommendationList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_home, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recommendationList = view.findViewById(R.id.recommendationList);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        recommendationsUtils = new RecommendationsUtils(db);

        swipeRefreshLayout.setOnRefreshListener(() -> recommendationsUtils.getRecommendationData(getContext(), recommendationList, swipeRefreshLayout));

        recommendationsUtils.getRecommendationData(getContext(), recommendationList, swipeRefreshLayout);

        return view;
    }
}
