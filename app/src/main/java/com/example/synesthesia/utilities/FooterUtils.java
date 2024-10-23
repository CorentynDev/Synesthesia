package com.example.synesthesia.utilities;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.example.synesthesia.BookmarksActivity;
import com.example.synesthesia.MainActivity;
import com.example.synesthesia.R;
import com.example.synesthesia.SearchBookActivity;
import com.example.synesthesia.SearchMusicActivity;
import com.example.synesthesia.UserProfileActivity;

public class FooterUtils {

    /**
     * Method to initialize footer redirections.
     * @param activity Current activity.
     */
    public static void setupFooter(Activity activity) {
        ImageView homeButton = activity.findViewById(R.id.homeButton);
        homeButton.setOnClickListener(view -> {
            Intent intent = new Intent(activity, MainActivity.class);
            activity.startActivity(intent);
        });

        ImageView createRecommendationButton = activity.findViewById(R.id.createRecommendationButton);
        createRecommendationButton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Choisissez un type de recommandation");

            String[] types = {"Musique", "Livre"};
            builder.setItems(types, (dialog, which) -> {
                switch (which) {
                    case 0:
                        activity.startActivity(new Intent(activity, SearchMusicActivity.class));
                        break;
                    case 1:
                        activity.startActivity(new Intent(activity, SearchBookActivity.class));
                        break;
                }
            });
            builder.create().show();
        });

        ImageView bookmarkButton = activity.findViewById(R.id.bookmarkButton);
        bookmarkButton.setOnClickListener(view -> {
            Intent intent = new Intent(activity, BookmarksActivity.class);
            activity.startActivity(intent);
        });

        ImageView profileButton = activity.findViewById(R.id.profileButton);
        profileButton.setOnClickListener(view -> {
            Intent intent = new Intent(activity, UserProfileActivity.class);
            activity.startActivity(intent);
        });
    }
}
