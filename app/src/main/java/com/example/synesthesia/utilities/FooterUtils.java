package com.example.synesthesia.utilities;

import android.app.AlertDialog;
import android.content.Intent;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.synesthesia.R;
import com.example.synesthesia.SearchBookActivity;
import com.example.synesthesia.SearchGameActivity;
import com.example.synesthesia.SearchMovieActivity;
import com.example.synesthesia.SearchMusicActivity;
import com.example.synesthesia.fragments.HomeFragment;
import com.example.synesthesia.fragments.SearchUserFragment;
import com.example.synesthesia.fragments.UserProfileFragment;

public class FooterUtils {

    public static void setupFooter(@NonNull FragmentActivity activity, int activeButtonId) {
        ImageView homeButton = activity.findViewById(R.id.homeButton);
        ImageView research = activity.findViewById(R.id.research);
        ImageView createRecommendationButton = activity.findViewById(R.id.createRecommendationButton);
        ImageView bookmarkButton = activity.findViewById(R.id.bookmarkButton);
        ImageView profileButton = activity.findViewById(R.id.profileButton);

        homeButton.setImageResource(R.drawable.home);
        research.setImageResource(R.drawable.loupe);
        createRecommendationButton.setImageResource(R.drawable.add);
        bookmarkButton.setImageResource(R.drawable.bookmark);
        profileButton.setImageResource(R.drawable.user);

        // Active le bouton en fonction de l'id fourni
        if (activeButtonId == R.id.homeButton) {
            homeButton.setImageResource(R.drawable.home_active);
        } else if (activeButtonId == R.id.research) {
            research.setImageResource(R.drawable.loupe_active);
        } else if (activeButtonId == R.id.createRecommendationButton) {
            createRecommendationButton.setImageResource(R.drawable.add_active);
        } else if (activeButtonId == R.id.bookmarkButton) {
            bookmarkButton.setImageResource(R.drawable.bookmark_active);
        } else if (activeButtonId == R.id.profileButton) {
            profileButton.setImageResource(R.drawable.user_active);
        }

        // Configuration des listeners
        homeButton.setOnClickListener(view -> {
            HomeFragment homeFragment = new HomeFragment();
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, homeFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        research.setOnClickListener(view -> {
            SearchUserFragment searchUserFragment = new SearchUserFragment();
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, searchUserFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        profileButton.setOnClickListener(view -> {
            UserProfileFragment userProfileFragment = new UserProfileFragment();
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, userProfileFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        createRecommendationButton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Choisissez un type de recommandation");

            String[] types = {"Musique", "Livre", "Film", "Jeu Vidéo"};
            builder.setItems(types, (dialog, which) -> {
                switch (which) {
                    case 0:
                        activity.startActivity(new Intent(activity, SearchMusicActivity.class));
                        break;
                    case 1:
                        activity.startActivity(new Intent(activity, SearchBookActivity.class));
                        break;
                    case 2:
                        activity.startActivity(new Intent(activity, SearchMovieActivity.class));
                        break;
                    case 3:
                        activity.startActivity(new Intent(activity, SearchGameActivity.class));
                        break;
                }
            });
            builder.create().show();
        });
    }

    private static void replaceFragment(FragmentActivity activity, Fragment fragment) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
