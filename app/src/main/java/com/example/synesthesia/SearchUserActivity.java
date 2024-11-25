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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchUserActivity extends AppCompatActivity {
    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private List<String> pseudoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        Button searchButton = findViewById(R.id.searchButton);
        EditText searchField = findViewById(R.id.searchField);  // Récupère le champ de recherche

        userAdapter = new UserAdapter(this, pseudoList, userMap); // Correction ici
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(userAdapter);

        // Action au clic du bouton "Valider"
        searchButton.setOnClickListener(v -> {
            String query = searchField.getText().toString().trim();
            Log.d("SearchUserActivity", "Button clicked, search query: " + query);  // Ajout d'un log
            if (!query.isEmpty()) {
                searchUsers(query);  // Lance la recherche des utilisateurs
            } else {
                Log.d("SearchUserActivity", "Search field is empty");
            }
        });

        fetchUsersFromFirestore();  // Charge initialement tous les utilisateurs
    }
    private Map<String, String> userMap = new HashMap<>(); // Associe pseudo -> URL

    private void fetchUsersFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        pseudoList.clear();
                        userMap.clear(); // Nettoie les données existantes
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String pseudo = document.getString("username");
                            String profileImageUrl = document.getString("profileImageUrl");
                            if (pseudo != null && profileImageUrl != null) {
                                pseudoList.add(pseudo); // Ajoute uniquement les pseudos dans la liste
                                userMap.put(pseudo, profileImageUrl); // Stocke l'URL correspondante
                            }
                        }
                        userAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("SearchUserActivity", "Error fetching users: ", task.getException());
                    }
                });
    }

    private void searchUsers(String query) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Convertir la requête en minuscules pour ne pas tenir compte de la casse
        String queryLowerCase = query.toLowerCase();
        Log.d("SearchUserActivity", "Starting search for: " + queryLowerCase);  // Log de début de recherche

        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("SearchUserActivity", "Search successful, number of results: " + task.getResult().size());  // Log des résultats

                        pseudoList.clear();
                        userMap.clear(); // Nettoie aussi userMap pour éviter des erreurs

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String pseudo = document.getString("username");
                            String profileImageUrl = document.getString("profileImageUrl");
                            if (pseudo != null && profileImageUrl != null && pseudo.toLowerCase().contains(queryLowerCase)) {
                                pseudoList.add(pseudo);  // Ajouter le pseudo dans la liste
                                userMap.put(pseudo, profileImageUrl);  // Associer le pseudo à son image
                            }
                        }

                        userAdapter.notifyDataSetChanged();  // Met à jour l'adaptateur
                    } else {
                        Log.e("SearchUserActivity", "Error fetching users: ", task.getException());
                    }
                });
    }
}