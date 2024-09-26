package com.example.synesthesia;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.synesthesia.models.Recommendation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class BookmarksActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // On récupère le document de recommandation spécifique à cet utilisateur
            db.collection("recommendations").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Le document a été trouvé, on peut le convertir en objet Recommendation
                            Recommendation recommendation = documentSnapshot.toObject(Recommendation.class);

                            // Assure-toi que recommendation n'est pas null
                            if (recommendation != null) {
                                // Récupère l'ID de la recommandation
                                String recommendationID = recommendation.getID();

                                // Appelle la méthode pour obtenir les recommandations marquées
                                getBookmarkedRecommendations(recommendationID);
                            }
                        } else {
                            Log.d("BookmarksActivity", "Document does not exist");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("BookmarksActivity", "Error fetching recommendation", e);
                    });
        }
    }
    private void getBookmarkedRecommendations(String recommendationID) {
        // Récupération des utilisateurs qui ont marqué cette recommandation
        db.collection("users")
                .whereArrayContains("bookmarkedRecommendations", recommendationID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recommendation> recommendations = new ArrayList<>();
                    // Parcourir les documents récupérés
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Convertir chaque document en objet Recommendation
                        Recommendation recommendation = document.toObject(Recommendation.class);
                        if (recommendation != null) {
                            recommendations.add(recommendation);
                        }
                    }

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
                })
                .addOnFailureListener(e -> Log.e("BookmarksActivity", "Error fetching bookmarks", e));
    }

    private void addRecommendationCard(LinearLayout container, Recommendation recommendation, String recommendationId) {
        // Utilisez la méthode que vous avez déjà dans MainActivity pour afficher les cartes de recommandation
        // Copiez/collez la méthode addRecommendationCard ici
    }
}