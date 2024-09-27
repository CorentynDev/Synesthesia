package com.example.synesthesia;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.synesthesia.models.Recommendation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class BookmarksActivity extends AppCompatActivity {
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        // Initialisation de Firebase Firestore et l'utilisateur actuel
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            // On récupère le document utilisateur
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Récupère le tableau bookmarkedRecommendations du document utilisateur
                            List<String> bookmarkedRecommendations = (List<String>) documentSnapshot.get("bookmarkedRecommendations");

                            // Vérifie si le tableau n'est pas null et contient des éléments
                            if (bookmarkedRecommendations != null && !bookmarkedRecommendations.isEmpty()) {
                                // Parcourir tous les IDs dans bookmarkedRecommendations
                                List<Recommendation> recommendations = new ArrayList<>();
                                for (String recommendationID : bookmarkedRecommendations) {
                                    Log.d("BookmarksActivity", "Bookmarked Recommendation ID: " + recommendationID);

                                    // Appelle la méthode pour obtenir les informations de chaque recommandation
                                    getBookmarkedRecommendations(recommendationID, recommendations);
                                }
                            } else {
                                // Affiche un message si le tableau est vide ou nul
                                Log.d("BookmarksActivity", "Aucun enregistrement effectué");
                            }
                        } else {
                            Log.d("BookmarksActivity", "User document does not exist");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("BookmarksActivity", "Error fetching user document", e);
                    });
        }
    }

    // Méthode pour récupérer les recommandations à partir de la collection 'recommendations'
    private void getBookmarkedRecommendations(String recommendationID, List<Recommendation> recommendations) {
        // Récupérer les informations de la recommandation dans la collection 'recommendations'
        db.collection("recommendations").document(recommendationID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Convertir le document en objet Recommendation
                        Recommendation recommendation = documentSnapshot.toObject(Recommendation.class);
                        if (recommendation != null) {
                            recommendations.add(recommendation);
                        }

                        // Mettre à jour l'interface après avoir récupéré les recommandations
                        updateRecyclerView(recommendations);
                    } else {
                        Log.d("BookmarksActivity", "Recommendation document does not exist");
                    }
                })
                .addOnFailureListener(e -> Log.e("BookmarksActivity", "Error fetching recommendation", e));
    }

    // Méthode pour mettre à jour la RecyclerView avec les recommandations
    private void updateRecyclerView(List<Recommendation> recommendations) {
        // Configuration de la RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerViewBookmarks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Définir l'adaptateur avec la liste de recommandations
        RecommendationAdapter adapter = new RecommendationAdapter(recommendations);
        recyclerView.setAdapter(adapter);

        // Gérer l'affichage si aucune recommandation n'est trouvée
        TextView emptyView = findViewById(R.id.emptyView);
        if (recommendations.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}
