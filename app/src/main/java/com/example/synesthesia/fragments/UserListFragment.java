package com.example.synesthesia.fragments;

import static com.example.synesthesia.utilities.UserUtils.db;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.MainActivity;
import com.example.synesthesia.R;
import com.example.synesthesia.adapters.UserListAdapter;
import com.example.synesthesia.utilities.FooterUtils;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserListFragment extends Fragment implements UserListAdapter.OnUserClickListener {

    private RecyclerView userRecyclerView;
    private UserListAdapter adapter;
    private List<String> userIdList = new ArrayList<>();
    private String userId;
    private String type;

    public UserListFragment() {
        // Required empty public constructor
    }

    @NonNull
    public static UserListFragment newInstance(String userId, String type) {
        UserListFragment fragment = new UserListFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FooterUtils.setupFooter(requireActivity(), R.id.profileButton);

        TextView titleTextView = view.findViewById(R.id.titleTextView);
        userRecyclerView = view.findViewById(R.id.userRecyclerView);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new UserListAdapter(userIdList, this);
        userRecyclerView.setAdapter(adapter);

        // Récupération des arguments passés au fragment
        Bundle args = getArguments();
        if (args != null) {
            userId = args.getString("userId");
            type = args.getString("type");

            if ("followers".equals(type)) {
                titleTextView.setText("Followers");
                loadFollowers();
            } else if ("following".equals(type)) {
                titleTextView.setText("Following");
                loadFollowing();
            }
        }
    }

    private void loadFollowers() {
        db.collection("followers")
                .document(userId)
                .collection("followers")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    userIdList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        userIdList.add(doc.getId());
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("LoadFollowers", "Erreur lors du chargement des followers", e));
    }

    private void loadFollowing() {
        db.collection("followers")
                .document(userId)
                .collection("following")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    userIdList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        userIdList.add(doc.getId());
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("LoadFollowing", "Erreur lors du chargement des following", e));
    }

    @Override
    public void onUserClick(String userId) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showUserProfileFragment(userId);
        }
    }
}