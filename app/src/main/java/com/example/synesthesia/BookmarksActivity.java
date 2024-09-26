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
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            getBookmarkedRecommendations(userId);
        }
    }
    private void getBookmarkedRecommendations(String userId) {
        db.collection("recommendations")
                .whereArrayContains("markedBy", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recommendation> recommendations = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recommendation recommendation = document.toObject(Recommendation.class);
                        recommendations.add(recommendation);
                    }
                    // Configuration de la RecyclerView
                    RecyclerView recyclerView = findViewById(R.id.recyclerViewBookmarks);
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    // Définir l'adaptateur avec la liste de recommandations
                    RecommendationAdapter adapter = new RecommendationAdapter(recommendations);
                    recyclerView.setAdapter(adapter);
                    // Si aucune recommandation n'est trouvée, afficher le message "emptyView"
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