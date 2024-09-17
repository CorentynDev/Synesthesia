package com.example.synesthesia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.synesthesia.models.Recommendation;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();

        Button createRecommendationButton = findViewById(R.id.createRecommendationButton);
        createRecommendationButton.setOnClickListener(v -> {
            // Création de la fenêtre modale avec les options de types de recommandation
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Choisissez un type de recommandation");

            // Options de type de recommandation
            String[] types = {"Musique", "Film", "Jeux Vidéo", "Livre"};

            // Gestion du clic sur l'une des options
            builder.setItems(types, (dialog, which) -> {
                switch (which) {
                    case 0: // Musique
                        // Lancer une activité pour la création de recommandation musicale (si implémentée plus tard)
                        break;
                    case 1: // Film
                        // Lancer une activité pour la création de recommandation de films (si implémentée plus tard)
                        break;
                    case 2: // Jeux Vidéo
                        // Lancer une activité pour la création de recommandation de jeux vidéo (si implémentée plus tard)
                        break;
                    case 3: // Livre
                        // Lancer l'Activity pour la recherche de livres
                        Intent intent = new Intent(MainActivity.this, SearchBookActivity.class);
                        startActivity(intent);
                        break;
                }
            });
            // Afficher la fenêtre modale
            builder.create().show();
        });


        // Lire les données
        getRecommendationData();

        // Appelle la méthode pour récupérer les infos de l'utilisateur
        getUserProfile();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Redirection vers LoginActivity si l'utilisateur n'est pas connecté
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void getRecommendationData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("recommendations").get().addOnSuccessListener(queryDocumentSnapshots -> {
            LinearLayout recommendationList = findViewById(R.id.recommendationList);
            recommendationList.removeAllViews();

            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Recommendation recommendation = document.toObject(Recommendation.class);
                addRecommendationCard(recommendationList, recommendation);
            }
        }).addOnFailureListener(e -> Log.e("FirestoreData", "Error when fetching documents: ", e));
    }

    public void addRecommendationCard(LinearLayout container, Recommendation recommendation) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.recommendation_card, container, false);

        TextView titleTextView = cardView.findViewById(R.id.recommendationTitle);
        titleTextView.setText(recommendation.getTitle());

        TextView dateTextView = cardView.findViewById(R.id.recommendationDate);
        dateTextView.setText(recommendation.getDate());

        // Affichez d'autres informations si disponibles

        container.addView(cardView);
    }

    // Méthode pour récupérer et afficher le pseudonyme de l'utilisateur
    private void getUserProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // Référence à la TextView pour le pseudonyme
        TextView profileSummary = findViewById(R.id.profileSummary);
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Rechercher les informations de l'utilisateur dans Firestore
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Récupérer le pseudonyme de l'utilisateur
                            String username = documentSnapshot.getString("username");

                            // Afficher le pseudonyme dans la TextView
                            profileSummary.setText("Welcome, " + username + "!");
                        } else {
                            Log.d("UserProfile", "No such document");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("UserProfile", "Error fetching user data", e);
                    });
        }
    }
}
