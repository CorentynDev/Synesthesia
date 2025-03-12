package com.example.synesthesia;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.synesthesia.utilities.FooterUtils;
import com.example.synesthesia.utilities.RecommendationsUtils;
import com.example.synesthesia.utilities.UserUtils;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private boolean isFollowingFilterActive = false; // Variable pour suivre l'état du filtre

    RecommendationsUtils recommendationsUtils;

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean o) {
            if (o){
                Toast.makeText(MainActivity.this, "Post  notification permission granted!", Toast.LENGTH_SHORT).show();
            }
        }
    });

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
            activityResultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }

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
                    // Mettre à jour le texte du bouton
                    filterMenuButton.setText(R.string.all_recommendations);
                    isFollowingFilterActive = false; // Mise à jour de l'état du filtre
                    return true;
                } else if (id == R.id.followed_recommendations) {
                    // Filtrer pour les recommandations des gens suivis
                    recommendationsUtils.getRecommendationData(MainActivity.this, recommendationList, swipeRefreshLayout, true);
                    // Mettre à jour le texte du bouton
                    filterMenuButton.setText(R.string.followed_recommendations);
                    isFollowingFilterActive = true; // Mise à jour de l'état du filtre
                    return true;
                }
                return false;
            });

            popupMenu.show(); // Afficher le menu
        });

        // Ajouter le Listener pour le SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Relancer les données avec le filtre actuel et mettre à jour le texte du bouton
            recommendationsUtils.getRecommendationData(MainActivity.this, recommendationList, swipeRefreshLayout, isFollowingFilterActive);

            // Mettre à jour le texte du bouton en fonction du filtre actif
            if (isFollowingFilterActive) {
                filterMenuButton.setText(R.string.followed_recommendations);
            } else {
                filterMenuButton.setText(R.string.all_recommendations);
            }
        });

        // Charger les recommandations par défaut à la création (filtre "Toutes")
        recommendationsUtils.getRecommendationData(this, recommendationList, swipeRefreshLayout, false);

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                new UserUtils().saveUserToken(currentUserId, token);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.notif, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.notif) {
            Intent intent = new Intent(this, NotifActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
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