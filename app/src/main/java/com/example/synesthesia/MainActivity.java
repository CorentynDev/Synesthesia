package com.example.synesthesia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

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

        Spinner filterSpinner = findViewById(R.id.filterSpinner);
        LinearLayout recommendationList = findViewById(R.id.recommendationList);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        recommendationsUtils = new RecommendationsUtils(FirebaseFirestore.getInstance());

        // Créer et configurer l'adaptateur pour le Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.filter_options, R.layout.custom_spinner_item);

        // Appliquer un style personnalisé pour le texte de l'élément sélectionné
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);

        filterSpinner.setAdapter(adapter);
        filterSpinner.setSelection(0); // Initialisation sur la première option

        // Ajouter le Listener pour le SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Utiliser la position actuelle du spinner pour déterminer le filtre
            boolean filterFollowed = filterSpinner.getSelectedItemPosition() == 1;
            recommendationsUtils.getRecommendationData(MainActivity.this, recommendationList, swipeRefreshLayout, filterFollowed);
        });

        // Ajouter le Listener pour détecter les changements dans le Spinner
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Utiliser le SwipeRefreshLayout pour recharger avec le bon filtre
                boolean filterFollowed = position == 1; // Position 1 pour "Recommandations des gens suivis"
                recommendationsUtils.getRecommendationData(MainActivity.this, recommendationList, swipeRefreshLayout, filterFollowed);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Charger les recommandations sans filtre si rien n'est sélectionné
                recommendationsUtils.getRecommendationData(MainActivity.this, recommendationList, swipeRefreshLayout, false);
            }
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
