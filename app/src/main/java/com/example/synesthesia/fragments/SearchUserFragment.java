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

public class SearchUserFragment extends Fragment {
    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private EditText searchField;
    private Button searchButton;

    private String userId;  // Ajout des variables pour récupérer les paramètres
    private String type;

    public SearchUserFragment() {
        // Required empty public constructor
    }

    // Méthode pour créer une nouvelle instance du fragment avec les paramètres
    public static SearchUserFragment newInstance(String userId, String type) {
        SearchUserFragment fragment = new SearchUserFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);  // Ajoute les paramètres au Bundle
        args.putString("type", type);
        fragment.setArguments(args);  // Attache le Bundle au fragment
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Récupère les arguments passés au fragment
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            type = getArguments().getString("type");
        }

        FooterUtils.setupFooter(getActivity(), R.id.research);  // Met à jour le footer

        usersRecyclerView = view.findViewById(R.id.usersRecyclerView);
        searchButton = view.findViewById(R.id.searchButton);
        searchField = view.findViewById(R.id.searchField);

        userAdapter = new UserAdapter(getContext(), userList);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        usersRecyclerView.setAdapter(userAdapter);

        // Action au clic du bouton de recherche
        searchButton.setOnClickListener(v -> {
            String query = searchField.getText().toString().trim();
            if (!query.isEmpty()) {
                searchUsers(query);
            } else {
                Log.d("SearchUserFragment", "Search field is empty or only contains spaces");
            }
        });

        // TextWatcher pour la recherche dynamique
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // Pas nécessaire ici
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String query = charSequence.toString().trim();
                if (!query.isEmpty()) {
                    searchUsers(query);
                } else {
                    fetchUsersFromFirestore();  // Affiche tous les utilisateurs si la recherche est vide
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Pas nécessaire ici
            }
        });

        // Charger initialement tous les utilisateurs
        fetchUsersFromFirestore();
    }

    // Méthode pour récupérer tous les utilisateurs depuis Firestore
    private void fetchUsersFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();  // Efface les utilisateurs existants
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String pseudo = document.getString("username");
                            String profileImageUrl = document.getString("profileImageUrl");
                            String id = document.getId();
                            if (pseudo != null && profileImageUrl != null) {
                                // Crée un objet User et l'ajoute à la liste
                                User user = new User(pseudo, profileImageUrl, id);
                                userList.add(user);
                            }
                        }
                        userAdapter.notifyDataSetChanged();  // Met à jour l'adaptateur
                    } else {
                        Log.e("SearchUserFragment", "Error fetching users: ", task.getException());
                    }
                });
    }

    // Méthode pour rechercher des utilisateurs en fonction de la requête
    private void searchUsers(String query) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String queryLowerCase = query.toLowerCase();
        Log.d("SearchUserFragment", "Starting search for: " + queryLowerCase);  // Log de recherche

        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("SearchUserFragment", "Search successful, number of results: " + task.getResult().size());

                        userList.clear();  // Efface la liste avant d'ajouter les résultats de la recherche

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String pseudo = document.getString("username");
                            String profileImageUrl = document.getString("profileImageUrl");
                            String id = document.getId();

                            if (pseudo != null && profileImageUrl != null && pseudo.toLowerCase().contains(queryLowerCase)) {
                                User user = new User(pseudo, profileImageUrl, id);
                                userList.add(user);
                            }
                        }

                        userAdapter.notifyDataSetChanged();  // Met à jour l'adaptateur
                    } else {
                        Log.e("SearchUserFragment", "Error fetching users: ", task.getException());
                    }
                });
    }
}
