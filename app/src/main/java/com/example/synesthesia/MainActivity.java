package com.example.synesthesia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

    // Obtenir les données depuis Firestore
    public void getRecommendationData() {
        db.collection("recommendations").get().addOnSuccessListener(queryDocumentSnapshots -> {
            // Référence au conteneur de cartes
            LinearLayout recommendationList = findViewById(R.id.recommendationList);
            recommendationList.removeAllViews();  // Efface les vues précédentes si nécessaire

            // Pour chaque document, créer une carte
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String recommendationTitle = document.getString("title");
                String recommendationDate = document.getString("date");
                String recommendationCover = document.getString("cover");

                // Créer une nouvelle carte pour chaque recommandation
                addRecommendationCard(recommendationList, recommendationTitle, recommendationDate, recommendationCover);

                Log.d("FirestoreData", "Recommendation: " + recommendationTitle + ", publication date: " + recommendationDate + ", cover: " + recommendationCover);
            }
        }).addOnFailureListener(e -> Log.e("FirestoreData", "Error when fetching documents: ", e));
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

    // Méthode pour créer et ajouter dynamiquement une carte
    public void addRecommendationCard(LinearLayout container, String title, String date, String coverUrl) {
        // Utilisation de LayoutInflater pour gonfler la vue de la carte depuis le fichier XML
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.recommendation_card, container, false);

        // Associe les données de la recommandation aux vues de la carte
        TextView titleTextView = cardView.findViewById(R.id.recommendationTitle);
        titleTextView.setText(title);

        TextView dateTextView = cardView.findViewById(R.id.recommendationDate);
        dateTextView.setText(date);

        ImageView coverImageView = cardView.findViewById(R.id.recommendationCover);

        // Utilisation de Glide pour charger l'image à partir de l'URL
        Glide.with(this)
                .load(coverUrl)  // URL de l'image
                .placeholder(R.color.gray_medium)  // Image de substitution pendant le chargement
                .error(R.color.red)  // Image de substitution en cas d'erreur
                .into(coverImageView);  // Charge l'image dans l'ImageView
        Log.d("GlideImageLoad", "Loading image from URL: " + coverUrl);


        // Ajoute la carte au conteneur LinearLayout
        container.addView(cardView);
    }
}
