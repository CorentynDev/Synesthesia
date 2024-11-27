package com.example.synesthesia.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.synesthesia.BookmarksActivity;
import com.example.synesthesia.MainActivity;
import com.example.synesthesia.R;
import com.example.synesthesia.SearchBookActivity;
import com.example.synesthesia.SearchGameActivity;
import com.example.synesthesia.SearchMovieActivity;
import com.example.synesthesia.SearchMusicActivity;
import com.example.synesthesia.SearchUserActivity;
import com.example.synesthesia.UserProfileActivity;

public class FooterUtils {

    /**
     * Method to initialize footer redirections and manage active/inactive states.
     * @param activity Current activity.
     * @param activeButtonId Resource ID of the button to be set as active.
     */
    public static void setupFooter(@NonNull Activity activity, int activeButtonId) {
        // Get all buttons
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
        } if (activeButtonId == R.id.research) {
            research.setImageResource(R.drawable.loupe_active);
        } else if (activeButtonId == R.id.createRecommendationButton) {
            createRecommendationButton.setImageResource(R.drawable.add_active);
        } else if (activeButtonId == R.id.bookmarkButton) {
            bookmarkButton.setImageResource(R.drawable.bookmark_active);
        } else if (activeButtonId == R.id.profileButton) {
            profileButton.setImageResource(R.drawable.user_active);
        }

        homeButton.setOnClickListener(view -> {
            Intent intent = new Intent(activity, MainActivity.class);
            activity.startActivity(intent);
        });

        research.setOnClickListener(view -> {
            Intent intent = new Intent(activity, SearchUserActivity.class);
            activity.startActivity(intent);
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

        bookmarkButton.setOnClickListener(view -> {
            Intent intent = new Intent(activity, BookmarksActivity.class);
            activity.startActivity(intent);
        });

        profileButton.setOnClickListener(view -> {
            Intent intent = new Intent(activity, UserProfileActivity.class);
            activity.startActivity(intent);
        });
    }
}
