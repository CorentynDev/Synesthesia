package com.example.synesthesia.utilities;

import android.app.AlertDialog;
import android.util.Log;
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
            Log.d("FooterUtils", "Home button clicked");
            replaceFragment(activity, new HomeFragment());
        });

        research.setOnClickListener(view -> {
            Log.d("FooterUtils", "Research button clicked");
            replaceFragment(activity, new SearchUserFragment());
        });

        bookmarkButton.setOnClickListener(view -> {
            Log.d("FooterUtils", "Bookmark button clicked");
            replaceFragment(activity, new BookmarkFragment());
        });

        profileButton.setOnClickListener(view -> {
            Log.d("FooterUtils", "Profile button clicked");
            replaceFragment(activity, new UserProfileFragment());
        });

        createRecommendationButton.setOnClickListener(view -> {
            Log.d("FooterUtils", "Create recommendation button clicked");
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Choisissez un type de recommandation");

            String[] types = {"Musique", "Livre", "Film", "Jeu Vidéo"};
            builder.setItems(types, (dialog, which) -> {
                switch (which) {
                    case 0:
                        replaceFragment(activity, new SearchMusicFragment());
                        break;
                    case 1:
                        replaceFragment(activity, new SearchBookFragment());
                        break;
                    case 2:
                        replaceFragment(activity, new SearchMovieFragment());
                        break;
                    case 3:
                        replaceFragment(activity, new SearchGameFragment());
                        break;
                }
            });
            builder.create().show();
        });
    }

    private static void replaceFragment(@NonNull FragmentActivity activity, Fragment fragment) {
        Log.d("FooterUtils", "Replacing fragment with: " + fragment.getClass().getSimpleName());
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}

