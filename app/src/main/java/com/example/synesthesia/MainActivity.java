package com.example.synesthesia;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.synesthesia.utilities.FooterUtils;
import com.example.synesthesia.utilities.RecommendationsUtils;
import com.example.synesthesia.utilities.UserUtils;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    RecommendationsUtils recommendationsUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FooterUtils.setupFooter(this, R.id.homeButton);

        Button filterMenuButton = findViewById(R.id.filterMenuButton); // Remplacement du Spinner par un bouton
        LinearLayout recommendationList = findViewById(R.id.recommendationList);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        recommendationsUtils = new RecommendationsUtils(FirebaseFirestore.getInstance());

        // Ajouter un listener pour afficher le PopupMenu
        filterMenuButton.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, filterMenuButton);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.filter_menu, popupMenu.getMenu()); // Fichier XML du menu

            // Gérer les clics sur les options du menu
            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.all_recommendations) {
                    // Filtrer pour toutes les recommandations
                    recommendationsUtils.getRecommendationData(MainActivity.this, recommendationList, swipeRefreshLayout, false);
                    return true;
                } else if (id == R.id.followed_recommendations) {
                    // Filtrer pour les recommandations des gens suivis
                    recommendationsUtils.getRecommendationData(MainActivity.this, recommendationList, swipeRefreshLayout, true);
                    return true;
                }
                return false;
            });

            popupMenu.show(); // Afficher le menu
        });

        // Ajouter le Listener pour le SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Relancer les données avec le filtre par défaut (false pour toutes les recommandations)
            recommendationsUtils.getRecommendationData(MainActivity.this, recommendationList, swipeRefreshLayout, false);
        });

        // Charger les recommandations par défaut à la création
        recommendationsUtils.getRecommendationData(this, recommendationList, swipeRefreshLayout, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        UserUtils userUtils = new UserUtils();
        if (!userUtils.isUserLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Arrêter la musique si elle est en cours de lecture
        if (RecommendationsUtils.globalMediaPlayer != null && RecommendationsUtils.globalMediaPlayer.isPlaying()) {
            RecommendationsUtils.globalMediaPlayer.stop();
            RecommendationsUtils.globalMediaPlayer.release();
            RecommendationsUtils.globalMediaPlayer = null;
        }

        // Réinitialiser le bouton de lecture/pause de la musique en cours
        if (RecommendationsUtils.currentlyPlayingButton != null) {
            RecommendationsUtils.currentlyPlayingButton.setImageResource(R.drawable.bouton_de_lecture);
            RecommendationsUtils.currentlyPlayingButton = null;
        }
        RecommendationsUtils.currentlyPlayingUrl = null;
    }
}
