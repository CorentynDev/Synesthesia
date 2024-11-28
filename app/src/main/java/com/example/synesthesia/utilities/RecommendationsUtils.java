package com.example.synesthesia.utilities;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.synesthesia.R;
import com.example.synesthesia.UserProfileActivity;
import com.example.synesthesia.api.DeezerApiClient;
import com.example.synesthesia.api.DeezerApi;
import com.example.synesthesia.models.Recommendation;
import com.example.synesthesia.models.Track;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendationsUtils {

    private final FirebaseFirestore db;
    private final LikeUtils likeUtils;
    private final BookmarkUtils bookmarkUtils;
    private final CommentUtils commentUtils;

    public RecommendationsUtils(FirebaseFirestore db) {
        this.db = db;
        this.likeUtils = new LikeUtils(db);
        this.bookmarkUtils = new BookmarkUtils(db);
        this.commentUtils = new CommentUtils(db);
    }

    /**
     * Get recommendations data from Firestore and display it as a list.
     *
     * @param context                Context in which method is called (an activity).
     * @param recommendationList     LinearLayout in which recommendations cards will be added.
     * @param swipeRefreshLayout     SwipeRefreshLayout used to allow user to refresh the list.
     */
    public void getRecommendationData(Context context, LinearLayout recommendationList,
                                      @NonNull SwipeRefreshLayout swipeRefreshLayout,
                                      boolean filterFollowed) {
        Log.d("RecommendationsUtils", "Starting to fetch recommendations");

        swipeRefreshLayout.setRefreshing(true);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "Utilisateur non connecté.", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        String userId = currentUser.getUid();

        if (filterFollowed) {
            // Charger uniquement les recommandations des utilisateurs suivis
            db.collection("followers")
                    .document(userId)
                    .collection("following")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<String> followedUsers = new ArrayList<>();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            followedUsers.add(document.getId()); // Chaque document dans "following" représente un utilisateur suivi
                        }

                        if (!followedUsers.isEmpty()) {
                            // Maintenant récupérer les recommandations des utilisateurs suivis
                            db.collection("recommendations")
                                    .whereIn("userId", followedUsers) // Filtrer les recommandations par les utilisateurs suivis
                                    .orderBy("timestamp", Query.Direction.DESCENDING)
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        populateRecommendations(context, recommendationList, queryDocumentSnapshots, swipeRefreshLayout);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FirestoreData", "Error when fetching recommendations: ", e);
                                        swipeRefreshLayout.setRefreshing(false);
                                    });
                        } else {
                            Toast.makeText(context, "Vous ne suivez encore personne.", Toast.LENGTH_SHORT).show();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("RecommendationsUtils", "Error fetching following users.", e);
                        swipeRefreshLayout.setRefreshing(false);
                    });
        } else {
            // Charger toutes les recommandations sans filtre
            db.collection("recommendations")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        populateRecommendations(context, recommendationList, queryDocumentSnapshots, swipeRefreshLayout);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreData", "Error when fetching recommendations: ", e);
                        swipeRefreshLayout.setRefreshing(false);
                    });
        }
    }

    /**
     * Helper method to populate recommendations list.
     */
    private void populateRecommendations(Context context, LinearLayout recommendationList, QuerySnapshot queryDocumentSnapshots, @NonNull SwipeRefreshLayout swipeRefreshLayout) {
        Log.d("RecommendationsUtils", "Successfully fetched recommendations");
        recommendationList.removeAllViews();

        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
            Recommendation recommendation = document.toObject(Recommendation.class);
            addRecommendationCard(context, recommendationList, recommendation, document.getId());
        }

        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Add a recommendation card to a context (an activity).
     *
     * @param context               Context in which the method is called (the activity).
     * @param container             LinearLayout in which the recommendation card will be added.
     * @param recommendation        The object Recommendation that contains the several details to display on the card.
     * @param recommendationId      ID of the recommendation, used for the interactions (like, comment, bookmark).
     */
    public void addRecommendationCard(Context context, LinearLayout container, @NonNull Recommendation recommendation, String recommendationId) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View cardView = inflater.inflate(R.layout.recommendation_card, container, false);

        TextView titleTextView = cardView.findViewById(R.id.recommendationTitle);
        titleTextView.setText(recommendation.getTitle());

        TextView dateTextView = cardView.findViewById(R.id.recommendationDate);
        Timestamp timestamp = recommendation.getTimestamp();
        String timeAgo = TimeUtils.getTimeAgo(timestamp);
        dateTextView.setText(timeAgo);

        TextView userTextView = cardView.findViewById(R.id.recommendationUser);
        ImageView profileImageView = cardView.findViewById(R.id.profileImageView);
        UserUtils.loadUserProfile(context, recommendation.getUserId(), userTextView, profileImageView);

        // Add onClickListener for userTextView to navigate to UserProfileActivity
        userTextView.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("userId", recommendation.getUserId()); // Pass user ID to the activity
            context.startActivity(intent);
        });

        ImageView typeIconImageView = cardView.findViewById(R.id.recommendationTypeIcon);
        ImageView playPauseButton = cardView.findViewById(R.id.playPauseButton);

        ImageView coverImageView = cardView.findViewById(R.id.recommendationCover);
        ImagesUtils.loadImage(context, recommendation.getCoverUrl(), coverImageView);

        setupLikeAndMarkButtons(cardView, recommendation, recommendationId);

        TextView userNote = cardView.findViewById(R.id.userRating);
        TextView recommendationLink = cardView.findViewById(R.id.recommendationLink);

        // Gestion dynamique de l'affichage du commentaire utilisateur
        if (recommendation.getUserNote() != null && !recommendation.getUserNote().isEmpty()) {
            userNote.setText(recommendation.getUserNote());
            userNote.setVisibility(View.VISIBLE);
        } else {
            userNote.setVisibility(View.GONE);
        }

        // Configuration du lien vers la description du film ou jeu
        String recommendationType = recommendation.getType();
        // Configuration du lien vers la description du livre
        if ("book".equals(recommendationType)) {
            recommendationLink.setVisibility(View.VISIBLE);
            recommendationLink.setOnClickListener(v -> {
                // URL base pour les livres (exemple : utiliser Google Books ou un autre service)
                String baseUrl = "https://books.google.com/";
                String bookId = recommendation.getArticleId();
                if (bookId != null && !bookId.isEmpty()) {
                    String bookUrl = baseUrl + "books?id=" + bookId;

                    // Ouvrir le lien dans le navigateur
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(bookUrl));
                    context.startActivity(browserIntent);
                } else {
                    Toast.makeText(context, "ID du livre introuvable", Toast.LENGTH_SHORT).show();
                }
            });
        } else if ("movie".equals(recommendationType)) {
            recommendationLink.setVisibility(View.VISIBLE);
            recommendationLink.setOnClickListener(v -> {
                String baseUrl = "https://www.themoviedb.org/movie/";
                String movieId = recommendation.getArticleId();
                if (movieId != null && !movieId.isEmpty()) {
                    String movieUrl = baseUrl + movieId;

                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(movieUrl));
                    context.startActivity(browserIntent);
                } else {
                    Toast.makeText(context, "ID du film introuvable", Toast.LENGTH_SHORT).show();
                }
            });
        } else if ("game".equals(recommendationType)) {
            recommendationLink.setVisibility(View.VISIBLE);
            recommendationLink.setOnClickListener(v -> {
                String gameUrl = "https://www.giantbomb.com/" + recommendation.getTitle().replace(" ", "-").toLowerCase() + "/3030-85417/";

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(gameUrl));
                context.startActivity(browserIntent);
            });
        } else if ("music".equals(recommendationType)) {
            // Ajouter le bouton "Voir plus" pour les musiques
            recommendationLink.setVisibility(View.VISIBLE);
            recommendationLink.setText("Voir plus");

            recommendationLink.setOnClickListener(v -> {
                String articleId = recommendation.getArticleId();
                if (articleId != null && !articleId.isEmpty()) {
                    // Utilisation de Deezer pour ouvrir l'album, l'artiste ou la chanson
                    String deezerUrl = "https://www.deezer.com/fr/track/" + articleId;  // Assurez-vous que l'ID corresponde à Deezer

                    // Ouvrir l'URL Deezer dans le navigateur
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(deezerUrl));
                    context.startActivity(browserIntent);
                } else {
                    Toast.makeText(context, "ID de la musique introuvable", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            recommendationLink.setVisibility(View.GONE);
        }

        TextView commentCounter = cardView.findViewById(R.id.commentCounter);
        ImageView commentButton = cardView.findViewById(R.id.commentButton);

        commentUtils.loadCommentCount(recommendationId, commentCounter);

        commentButton.setOnClickListener(v -> commentUtils.showCommentModal(context, recommendationId, commentCounter));

        if ("book".equals(recommendationType)) {
            typeIconImageView.setImageResource(R.drawable.book);
        } else if ("music".equals(recommendationType)) {
            typeIconImageView.setImageResource(R.drawable.musical_note);
            playPauseButton.setImageResource(R.drawable.bouton_de_lecture);
            String articleId = recommendation.getArticleId();
            fetchPreviewFromDeezer(context, playPauseButton, articleId);
        } else if ("artist".equals(recommendationType)) {
            typeIconImageView.setImageResource(R.drawable.artist);
        } else if ("album".equals(recommendationType)) {
            typeIconImageView.setImageResource(R.drawable.music_album);
        } else if ("film".equals(recommendationType)) {
            typeIconImageView.setImageResource(R.drawable.film);
        } else if ("game".equals(recommendationType)) {
            typeIconImageView.setImageResource(R.drawable.console);
        } else {
            typeIconImageView.setVisibility(View.GONE);
        }

        container.addView(cardView);
    }

    /**
     * Configure the several buttons (like and bookmark).
     *
     * @param cardView             Vue of the recommendation card that contains the buttons to configure.
     * @param recommendation       The Recommendation object for which the buttons are configured.
     * @param recommendationId     ID of the recommendation, used for the interactions.
     */
    private void setupLikeAndMarkButtons(@NonNull View cardView, @NonNull Recommendation recommendation, String recommendationId) {
        ImageView likeButton = cardView.findViewById(R.id.likeButton);
        TextView likeCounter = cardView.findViewById(R.id.likeCounter);
        ImageView markButton = cardView.findViewById(R.id.bookmarkRecommendationButton);

        List<String> likedBy = recommendation.getLikedBy() != null ? recommendation.getLikedBy() : new ArrayList<>();
        likeCounter.setText(String.valueOf(likedBy.size()));

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Initialize the like button
            final boolean[] isCurrentlyLiked = {likeUtils.isLiked(userId, recommendation)};
            likeButton.setImageResource(isCurrentlyLiked[0] ? R.drawable.given_like : R.drawable.like);

            // Récupérer les bookmarks de l'utilisateur et initialiser l'état du bouton
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<String> bookmarkedRecommendations = (List<String>) documentSnapshot.get("bookmarkedRecommendations");
                            boolean isBookmarked = bookmarkedRecommendations != null && bookmarkedRecommendations.contains(recommendationId);

                            markButton.setImageResource(isBookmarked ? R.drawable.bookmark_active : R.drawable.bookmark);

                            final boolean[] isCurrentlyMarked = {isBookmarked};
                            markButton.setOnClickListener(v -> {
                                boolean newMarkStatus = !isCurrentlyMarked[0];
                                bookmarkUtils.updateMarkUI(markButton, newMarkStatus);
                                bookmarkUtils.updateMarkList(userId, recommendation, newMarkStatus);
                                bookmarkUtils.toggleMark(recommendationId, userId, newMarkStatus, () -> isCurrentlyMarked[0] = newMarkStatus);
                            });
                        }
                    })
                    .addOnFailureListener(e -> Log.e("setupLikeAndMarkButtons", "Error fetching user bookmarks", e));

            likeButton.setOnClickListener(v -> {
                boolean newLikeStatus = !isCurrentlyLiked[0];
                likeUtils.updateLikeUI(likeButton, likeCounter, newLikeStatus, likedBy.size());
                likeUtils.updateLikeList(userId, recommendation, newLikeStatus);
                likeUtils.toggleLike(recommendationId, userId, newLikeStatus, () -> isCurrentlyLiked[0] = newLikeStatus);
            });
        }
    }

    /**
     * Fetch the preview URL from Deezer using their API.
     *
     * @param context      Context in which the method is called.
     * @param playButton   ImageView for play/pause button.
     * @param articleId    ID of the article to search on Deezer.
     */
    private void fetchPreviewFromDeezer(Context context, ImageView playButton, String articleId) {
        if (articleId == null || articleId.isEmpty()) {
            Log.e("DeezerAPI", "Article ID is null or empty, skipping API call.");
            playButton.setVisibility(View.GONE);
            return;
        }

        DeezerApi deezerApi = DeezerApiClient.getDeezerApi();

        // Effectuer un appel à l'API Deezer pour récupérer la piste via l'article ID
        deezerApi.getTrackById(articleId).enqueue(new Callback<Track>() {
            @Override
            public void onResponse(Call<Track> call, Response<Track> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Track track = response.body();
                    String previewUrl = track.getPreviewUrl();

                    if (previewUrl != null && !previewUrl.isEmpty()) {
                        setupPlayPauseButton(context, playButton, previewUrl);
                    } else {
                        Log.e("DeezerAPI", "No preview URL available for the given article ID.");
                        playButton.setVisibility(View.GONE);
                    }
                } else {
                    Log.e("DeezerAPI", "Failed to fetch track data: " + response.message());
                    playButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<Track> call, Throwable t) {
                Log.e("DeezerAPI", "API request failed: " + t.getMessage());
                playButton.setVisibility(View.GONE);
            }
        });
    }


    // Variables globales nécessaires
    public static MediaPlayer globalMediaPlayer; // MediaPlayer partagé
    public static ImageView currentlyPlayingButton; // Bouton actuellement actif
    public static String currentlyPlayingUrl; // URL en cours de lecture

    /**
     * Set up the play/pause button to play the preview from Deezer.
     *
     * @param context      Context of the application.
     * @param playButton   ImageView to be configured as play/pause.
     * @param previewUrl   The preview URL obtained from Deezer API.
     */
    private void setupPlayPauseButton(Context context, ImageView playButton, String previewUrl) {
        playButton.setOnClickListener(v -> {
            try {
                // Si le bouton est déjà actif (musique en cours de lecture ou pause sur la même URL)
                if (currentlyPlayingButton == playButton && currentlyPlayingUrl != null && currentlyPlayingUrl.equals(previewUrl)) {
                    if (globalMediaPlayer != null) {
                        if (globalMediaPlayer.isPlaying()) {
                            // Pause la musique si elle est en lecture
                            globalMediaPlayer.pause();
                            playButton.setImageResource(R.drawable.bouton_de_lecture);
                        } else {
                            // Relance la musique si elle est en pause
                            globalMediaPlayer.start();
                            playButton.setImageResource(R.drawable.pause);
                        }
                    }
                    return; // Pas besoin de reconfigurer le MediaPlayer
                }

                // Si une autre musique est en cours, arrêter et réinitialiser le MediaPlayer
                if (globalMediaPlayer != null) {
                    if (globalMediaPlayer.isPlaying()) {
                        globalMediaPlayer.stop();
                    }
                    globalMediaPlayer.reset();

                    // Réinitialiser le bouton précédemment actif
                    if (currentlyPlayingButton != null) {
                        currentlyPlayingButton.setImageResource(R.drawable.bouton_de_lecture);
                    }
                } else {
                    // Créer une nouvelle instance de MediaPlayer si elle n'existe pas
                    globalMediaPlayer = new MediaPlayer();
                }

                // Configurer le MediaPlayer pour la nouvelle URL
                globalMediaPlayer.setDataSource(previewUrl);
                globalMediaPlayer.prepare(); // Préparation synchrone

                // Jouer la musique
                globalMediaPlayer.start();
                playButton.setImageResource(R.drawable.pause);

                // Mettre à jour les références globales
                currentlyPlayingButton = playButton;
                currentlyPlayingUrl = previewUrl;

                // Réinitialiser le bouton à la fin de la musique
                globalMediaPlayer.setOnCompletionListener(mp -> {
                    playButton.setImageResource(R.drawable.bouton_de_lecture);
                    currentlyPlayingButton = null;
                    currentlyPlayingUrl = null;
                });

            } catch (Exception e) {
                Log.e("DeezerAPI", "Error setting up media player: " + e.getMessage());
            }
        });
    }

}
