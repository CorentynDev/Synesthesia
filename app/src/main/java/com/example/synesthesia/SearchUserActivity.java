package com.example.synesthesia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.models.User;
import com.example.synesthesia.utilities.FooterUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchUserActivity extends AppCompatActivity {
    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);
        FooterUtils.setupFooter(this, R.id.research);

        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        Button searchButton = findViewById(R.id.searchButton);
        EditText searchField = findViewById(R.id.searchField);  // Récupère le champ de recherche

        userAdapter = new UserAdapter(this, userList);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(userAdapter);

        // Action au clic du bouton "Valider"
        searchButton.setOnClickListener(v -> {
            String query = searchField.getText().toString().trim();
            if (!query.isEmpty()) {
                searchUsers(query);
            } else {
                Log.d("SearchUserActivity", "Search field is empty or only contains spaces");
            }

        });

        fetchUsersFromFirestore();  // Charge initialement tous les utilisateurs
    }

    private void fetchUsersFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear(); // Efface les utilisateurs existants
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
                        userAdapter.notifyDataSetChanged(); // Met à jour l'adaptateur
                    } else {
                        Log.e("SearchUserActivity", "Error fetching users: ", task.getException());
                    }
                });
    }

    private void searchUsers(String query) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String queryLowerCase = query.toLowerCase();
        Log.d("SearchUserActivity", "Starting search for: " + queryLowerCase); // Log de recherche

        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("SearchUserActivity", "Search successful, number of results: " + task.getResult().size());

                        userList.clear(); // Efface la liste avant d'ajouter les résultats de la recherche

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String pseudo = document.getString("username");
                            String profileImageUrl = document.getString("profileImageUrl");
                            String id = document.getId();

                            if (pseudo != null && profileImageUrl != null && pseudo.toLowerCase().contains(queryLowerCase)) {
                                User user = new User(pseudo, profileImageUrl, id);
                                userList.add(user);
                            }
                        }

                        userAdapter.notifyDataSetChanged(); // Met à jour l'adaptateur
                    } else {
                        Log.e("SearchUserActivity", "Error fetching users: ", task.getException());
                    }
                });
    }
}