package com.example.synesthesia.utilities;

import android.app.AlertDialog;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.synesthesia.R;
import com.example.synesthesia.fragments.BookmarkFragment;
import com.example.synesthesia.fragments.HomeFragment;
import com.example.synesthesia.fragments.SearchBookFragment;
import com.example.synesthesia.fragments.SearchGameFragment;
import com.example.synesthesia.fragments.SearchMovieFragment;
import com.example.synesthesia.fragments.SearchMusicFragment;
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

        bookmarkButton.setOnClickListener(view -> {
            BookmarkFragment bookmarkFragment = new BookmarkFragment();
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, bookmarkFragment);
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
                FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                switch (which) {
                    case 0:
                        SearchMusicFragment searchMusicFragment = new SearchMusicFragment();
                        transaction.replace(R.id.fragmentContainer, searchMusicFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                    case 1:
                        SearchBookFragment searchBookFragment = new SearchBookFragment();
                        transaction.replace(R.id.fragmentContainer, searchBookFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                    case 2:
                        SearchMovieFragment searchMovieFragment = new SearchMovieFragment();
                        transaction.replace(R.id.fragmentContainer, searchMovieFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                    case 3:
                        SearchGameFragment searchGameFragment = new SearchGameFragment();
                        transaction.replace(R.id.fragmentContainer, searchGameFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                }
            });
            builder.create().show();
        });
    }


    private static void replaceFragment(@NonNull FragmentActivity activity, Fragment fragment) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
