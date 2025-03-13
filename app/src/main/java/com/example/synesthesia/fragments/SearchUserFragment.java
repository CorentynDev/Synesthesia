package com.example.synesthesia.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.R;
import com.example.synesthesia.adapters.UserAdapter;
import com.example.synesthesia.models.User;
import com.example.synesthesia.utilities.FooterUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchUserFragment extends Fragment {
    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private EditText searchField;
    private Button searchButton;

    public SearchUserFragment() {
        // Required empty public constructor
    }

    @NonNull
    public static SearchUserFragment newInstance(String userId, String type) {
        SearchUserFragment fragment = new SearchUserFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            String userId = getArguments().getString("userId");
            String type = getArguments().getString("type");
        }

        FooterUtils.setupFooter(requireActivity(), R.id.research);

        usersRecyclerView = view.findViewById(R.id.usersRecyclerView);
        searchButton = view.findViewById(R.id.searchButton);
        searchField = view.findViewById(R.id.searchField);

        userAdapter = new UserAdapter(getContext(), userList);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        usersRecyclerView.setAdapter(userAdapter);

        searchButton.setOnClickListener(v -> {
            String query = searchField.getText().toString().trim();
            if (!query.isEmpty()) {
                searchUsers(query);
            } else {
                Log.d("SearchUserFragment", "Search field is empty or only contains spaces");
            }
        });

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String query = charSequence.toString().trim();
                if (!query.isEmpty()) {
                    searchUsers(query);
                } else {
                    fetchUsersFromFirestore();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        fetchUsersFromFirestore();
    }

    private void fetchUsersFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String pseudo = document.getString("username");
                            String profileImageUrl = document.getString("profileImageUrl");
                            String id = document.getId();
                            if (pseudo != null && profileImageUrl != null) {
                                User user = new User(pseudo, profileImageUrl, id);
                                userList.add(user);
                            }
                        }
                        userAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("SearchUserFragment", "Error fetching users: ", task.getException());
                    }
                });
    }

    private void searchUsers(@NonNull String query) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String queryLowerCase = query.toLowerCase();

        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("SearchUserFragment", "Search successful, number of results: " + task.getResult().size());

                        userList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String pseudo = document.getString("username");
                            String profileImageUrl = document.getString("profileImageUrl");
                            String id = document.getId();

                            if (pseudo != null && profileImageUrl != null && pseudo.toLowerCase().contains(queryLowerCase)) {
                                User user = new User(pseudo, profileImageUrl, id);
                                userList.add(user);
                            }
                        }

                        userAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("SearchUserFragment", "Error fetching users: ", task.getException());
                    }
                });
    }
}
