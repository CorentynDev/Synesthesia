package com.example.synesthesia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.synesthesia.models.Comment;
import com.example.synesthesia.models.Recommendation;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Transaction;
import com.squareup.picasso.Picasso;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();

        ImageView profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });

        ImageView bookmarkRecommendationButton = findViewById(R.id.bookmarkButton);
        bookmarkRecommendationButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, BookmarksActivity.class);
            startActivity(intent);
        });

        ImageView createRecommendationButton = findViewById(R.id.createRecommendationButton);
        createRecommendationButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Choisissez un type de recommandation");

            String[] types = {"Musique", "Film", "Jeux Vidéo", "Livre"};

            builder.setItems(types, (dialog, which) -> {
                switch (which) {
                    case 0:
                        Intent musicIntent = new Intent(MainActivity.this, SearchMusicActivity.class);
                        startActivity(musicIntent);
                        break;
                    case 1:
                        // New filmIntent
                        break;
                    case 2:
                        // New videoGameIntent
                        break;
                    case 3:
                        Intent bookIntent = new Intent(MainActivity.this, SearchBookActivity.class);
                        startActivity(bookIntent);
                        break;
                }
            });
            builder.create().show();
        });

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::getRecommendationData);

        getRecommendationData();
        getUserProfile();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    //TODO: Il faut faire un fichier qui regroupe toutes les fonctions utilitaires concernant les recommandations
    /**
     * Les fonctions concernées sont les suivantes : 
     * getRecommendationData()
     * addRecommendationCard()
     */
    public void getRecommendationData() {
        Log.d("MainActivity", "Starting to fetch recommendations");

        swipeRefreshLayout.setRefreshing(true);

        db.collection("recommendations").get().addOnSuccessListener(queryDocumentSnapshots -> {
            Log.d("MainActivity", "Successfully fetched recommendations");
            LinearLayout recommendationList = findViewById(R.id.recommendationList);
            recommendationList.removeAllViews();

            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Recommendation recommendation = document.toObject(Recommendation.class);
                addRecommendationCard(recommendationList, recommendation, document.getId());
            }

            swipeRefreshLayout.setRefreshing(false);
        }).addOnFailureListener(e -> {
            Log.e("FirestoreData", "Error when fetching documents: ", e);
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    public void addRecommendationCard(LinearLayout container, Recommendation recommendation, String recommendationId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.recommendation_card, container, false);

        // Titre
        TextView titleTextView = cardView.findViewById(R.id.recommendationTitle);
        titleTextView.setText(recommendation.getTitle());

        // Date
        TextView dateTextView = cardView.findViewById(R.id.recommendationDate);
        Timestamp timestamp = recommendation.getTimestamp();
        dateTextView.setText(timestamp != null ? getTimeAgo(timestamp) : "Date inconnue");

        // Utilisateur et image de profil
        TextView userTextView = cardView.findViewById(R.id.recommendationUser);
        ImageView profileImageView = cardView.findViewById(R.id.profileImageView);
        loadUserProfile(recommendation.getUserId(), userTextView, profileImageView);

        // Image de couverture
        ImageView coverImageView = cardView.findViewById(R.id.recommendationCover);
        loadImage(recommendation.getCoverUrl(), coverImageView);

        // Bouton de like et de bookmark
        setupLikeAndMarkButtons(cardView, recommendation, recommendationId);

        // Commentaires
        TextView commentCounter = cardView.findViewById(R.id.commentCounter);
        ImageView commentButton = cardView.findViewById(R.id.commentButton);
        loadCommentCount(recommendationId, commentCounter);

        // Gestion de l'ajout des commentaires
        commentButton.setOnClickListener(v -> showCommentModal(recommendationId, commentCounter));

        // Ajout de la vue dans le container
        container.addView(cardView);
    }

    /**
     * Charge les informations de l'utilisateur à partir de la base de données et met à jour l'UI.
     */
    private void loadUserProfile(String userId, TextView userTextView, ImageView profileImageView) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userTextView.setText(documentSnapshot.getString("username"));
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(this).load(profileImageUrl).into(profileImageView);
                        } else {
                            profileImageView.setImageResource(R.drawable.placeholder_image);
                        }
                    } else {
                        userTextView.setText("Utilisateur inconnu");
                        profileImageView.setImageResource(R.drawable.placeholder_image);
                    }
                })
                .addOnFailureListener(e -> {
                    userTextView.setText("Erreur de chargement");
                    profileImageView.setImageResource(R.drawable.placeholder_image);
                });
    }

    /**
     * Charge l'image avec Glide, avec une image par défaut si l'URL est vide ou nulle.
     */
    private void loadImage(String imageUrl, ImageView imageView) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).placeholder(R.drawable.placeholder_image).into(imageView);
        } else {
            imageView.setImageResource(R.drawable.placeholder_image);
        }
    }
  
    private void setupLikeAndMarkButtons(View cardView, Recommendation recommendation, String recommendationId) {
        ImageView likeButton = cardView.findViewById(R.id.likeButton);
        TextView likeCounter = cardView.findViewById(R.id.likeCounter);
        ImageView markButton = cardView.findViewById(R.id.bookmarkRecommendationButton);
        // Assurer que likedBy n'est pas null
        List<String> likedBy = recommendation.getLikedBy() != null ? recommendation.getLikedBy() : new ArrayList<>();
        likeCounter.setText(String.valueOf(likedBy.size())); // Utiliser la taille correcte du tableau

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            final boolean[] isCurrentlyLiked = {isLiked(userId, recommendation)};
            final boolean[] isCurrentlyMarked = {isMarked(userId, recommendation)};

            // Gestion de l'état initial des boutons
            likeButton.setImageResource(isCurrentlyLiked[0] ? R.drawable.given_like : R.drawable.like);
            markButton.setImageResource(isCurrentlyMarked[0] ? R.drawable.bookmark_active : R.drawable.bookmark);

            // Click sur le bouton like
            likeButton.setOnClickListener(v -> {
                boolean newLikeStatus = !isCurrentlyLiked[0];
                updateLikeUI(likeButton, likeCounter, newLikeStatus, likedBy.size());
                updateLikeList(userId, recommendation, newLikeStatus);
                toggleLike(recommendationId, userId, newLikeStatus, () -> isCurrentlyLiked[0] = newLikeStatus);
            });

            // Click sur le bouton bookmark
            markButton.setOnClickListener(v -> {
                boolean newMarkStatus = !isCurrentlyMarked[0];
                updateMarkUI(markButton, newMarkStatus);
                updateMarkList(userId, recommendation, newMarkStatus);
                toggleMark(recommendationId, userId, newMarkStatus, () -> isCurrentlyMarked[0] = newMarkStatus);
            });
        }
    }

    /**
     * Charge le nombre de commentaires à partir de Firestore.
     */
    private void loadCommentCount(String recommendationId, TextView commentCounter) {
        db.collection("recommendations").document(recommendationId)
                .collection("comments").get()
                .addOnSuccessListener(querySnapshot -> {
                    int commentCount = querySnapshot.size();
                    commentCounter.setText(String.valueOf(commentCount));
                })
                .addOnFailureListener(e -> commentCounter.setText("0"));
    }


    private void updateLikeList(String userId, Recommendation recommendation, boolean addLike) {
        List<String> likedBy = recommendation.getLikedBy();
        if (likedBy == null) {
            likedBy = new ArrayList<>();  // Si likedBy est null, initialiser une nouvelle liste
            recommendation.setLikedBy(likedBy);
        }

        if (addLike) {
            if (!likedBy.contains(userId)) {
                likedBy.add(userId);
            }
        } else {
            likedBy.remove(userId);
        }
    }

    private void updateLikeUI(ImageView likeButton, TextView likeCounter, boolean isCurrentlyLiked, int currentLikesCount) {
        if (isCurrentlyLiked) {
            likeButton.setImageResource(R.drawable.given_like);
            likeCounter.setText(String.valueOf(currentLikesCount + 1));
        } else {
            likeButton.setImageResource(R.drawable.like);
            likeCounter.setText(String.valueOf(Math.max(currentLikesCount - 1, 0))); // Eviter d'aller sous 0
        }
    }

    private boolean isLiked(String userId, Recommendation recommendation) {
        List<String> likedBy = recommendation.getLikedBy();
        return likedBy != null && likedBy.contains(userId);
    }

    private void toggleLike(String recommendationId, String userId, boolean isLiked, Runnable onComplete) {
        if (recommendationId == null || userId == null) {
            Log.e("ToggleLike", "Recommendation ID or User ID is null");
            return;
        }

        DocumentReference recommendationRef = db.collection("recommendations").document(recommendationId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(recommendationRef);
            if (!snapshot.exists()) {
                throw new FirebaseFirestoreException("Document does not exist", FirebaseFirestoreException.Code.NOT_FOUND);
            }

            List<String> likedBy = (List<String>) snapshot.get("likedBy");
            if (likedBy == null) {
                likedBy = new ArrayList<>();
            }

            if (isLiked) {
                if (!likedBy.contains(userId)) {
                    likedBy.add(userId);
                }
            } else {
                likedBy.remove(userId);
            }

            transaction.update(recommendationRef, "likedBy", likedBy);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("ToggleLike", "Transaction success!");
            onComplete.run();
        }).addOnFailureListener(e -> {
            Log.e("ToggleLike", "Transaction failure.", e);
            onComplete.run();
        });
    }

    private void updateMarkList(String userId, Recommendation recommendation, boolean addMark) {
        List<String> markedBy = recommendation.getMarkedBy();
        if (markedBy == null) {
            markedBy = new ArrayList<>();
            recommendation.setMarkedBy(markedBy);
        }
        if (addMark) {
            if (!markedBy.contains(userId)) {
                markedBy.add(userId);
            }
        } else {
            markedBy.remove(userId);
        }
    }
    private void updateMarkUI(ImageView markButton, boolean isCurrentlyMarked) {
        if (isCurrentlyMarked) {
            markButton.setImageResource(R.drawable.bookmark_active); // Icône active
        } else {
            markButton.setImageResource(R.drawable.bookmark); // Icône non active
        }
    }
    private boolean isMarked(String userId, Recommendation recommendation) {
        List<String> markedBy = recommendation.getMarkedBy();
        return markedBy != null && markedBy.contains(userId);
    }
    private void toggleMark(String recommendationId, String userId, boolean isMarked, Runnable onComplete) {
        if (recommendationId == null || userId == null) {
            Log.e("ToggleMark", "Recommendation ID or User ID is null");
            return;
        }

        // Référence vers le document de l'utilisateur
        DocumentReference userRef = db.collection("users").document(userId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(userRef);
            if (!snapshot.exists()) {
                throw new FirebaseFirestoreException("User document does not exist", FirebaseFirestoreException.Code.NOT_FOUND);
            }

            List<String> bookmarkedRecommendations = (List<String>) snapshot.get("bookmarkedRecommendations");
            if (bookmarkedRecommendations == null) {
                bookmarkedRecommendations = new ArrayList<>();
            }

            if (isMarked) {
                // Si la recommandation n'est pas encore bookmarkée, on l'ajoute
                if (!bookmarkedRecommendations.contains(recommendationId)) {
                    bookmarkedRecommendations.add(recommendationId);
                }
            } else {
                // Si la recommandation est bookmarkée, on la retire
                bookmarkedRecommendations.remove(recommendationId);
            }

            // Mise à jour du document de l'utilisateur avec la liste modifiée
            transaction.update(userRef, "bookmarkedRecommendations", bookmarkedRecommendations);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("ToggleMark", "Bookmark transaction success!");
            onComplete.run();
        }).addOnFailureListener(e -> {
            Log.e("ToggleMark", "Bookmark transaction failure.", e);
            onComplete.run();
        });
    }

    private void showCommentModal(String recommendationId, TextView commentCounter) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View modalView = inflater.inflate(R.layout.comment_modal, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(modalView);
        AlertDialog dialog = builder.create();
        dialog.show();

        EditText commentInput = modalView.findViewById(R.id.commentInput);
        Button postCommentButton = modalView.findViewById(R.id.postCommentButton);
        RecyclerView commentsRecyclerView = modalView.findViewById(R.id.commentsRecyclerView);
        ImageView closeModalButton = modalView.findViewById(R.id.closeModalButton);

        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<Comment> commentList = new ArrayList<>();
        CommentsAdapter adapter = new CommentsAdapter(commentList);
        commentsRecyclerView.setAdapter(adapter);

        // Charger les commentaires existants
        loadComments(recommendationId, commentList, adapter);

        postCommentButton.setOnClickListener(v -> {
            String commentText = commentInput.getText().toString().trim();
            if (!commentText.isEmpty()) {
                // Passer les paramètres supplémentaires pour mettre à jour dynamiquement
                postComment(recommendationId, commentText, commentCounter, commentList, adapter, commentsRecyclerView);
                commentInput.setText("");
            }
        });

        closeModalButton.setOnClickListener(v -> dialog.dismiss());
    }

    private void loadComments(String recommendationId, List<Comment> commentList, CommentsAdapter adapter) {
        db.collection("recommendations").document(recommendationId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Comment comment = document.toObject(Comment.class);
                        commentList.add(comment);
                    }
                    adapter.notifyDataSetChanged();
               })
                .addOnFailureListener(e -> Log.e("Firestore", "Error loading comments", e));
    }

    private void postComment(String recommendationId, String commentText, TextView commentCounter, List<Comment> commentList, CommentsAdapter adapter, RecyclerView commentsRecyclerView) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            Map<String, Object> comment = new HashMap<>();
            comment.put("userId", userId);
            comment.put("commentText", commentText);
            comment.put("timestamp", FieldValue.serverTimestamp());

            db.collection("recommendations").document(recommendationId)
                    .collection("comments").add(comment)
                    .addOnSuccessListener(documentReference -> {
                        // Créer le nouvel objet commentaire avec Timestamp.now()
                        Comment newComment = new Comment(userId, commentText, Timestamp.now());

                        // Ajouter le nouveau commentaire en haut de la liste
                        commentList.add(0, newComment);
                        adapter.notifyItemInserted(0);  // Notifie l'adaptateur de l'insertion

                        // Mettre à jour le compteur de commentaires
                        int currentCount = Integer.parseInt(commentCounter.getText().toString());
                        commentCounter.setText(String.valueOf(currentCount + 1));

                        // Scroll automatique vers le bas pour voir le nouveau commentaire
                        commentsRecyclerView.scrollToPosition(0);  // Fais défiler vers le haut pour voir le dernier commentaire ajouté
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error adding comment", e));
        }
    }

    private void getUserProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        ImageView profileImageView = findViewById(R.id.profileImageView);
        TextView profileSummary = findViewById(R.id.profileSummary);

        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            profileSummary.setText(documentSnapshot.getString("username"));
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(this).load(profileImageUrl).into(profileImageView);
                            } else {
                                profileImageView.setImageResource(R.drawable.placeholder_image);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("UserProfile", "Error fetching user data", e);
                    });
        }
    }

    private String getTimeAgo(Timestamp timestamp) {
        long time = timestamp.toDate().getTime();
        long now = System.currentTimeMillis();

        if (time > now || time <= 0) {
            return "à l'instant";
        }

        final long diff = now - time;
        if (diff < 60 * 1000) {
            return "Il y a " + diff / 1000 + " seconde(s)";
        } else if (diff < 2 * 60 * 1000) {
            return "Il y a une minute";
        } else if (diff < 50 * 60 * 1000) {
            return "Il y a " + diff / (60 * 1000) + " minutes";
        } else if (diff < 90 * 60 * 1000) {
            return "Il y a une heure";
        } else if (diff < 24 * 60 * 60 * 1000) {
            return "Il y a " + diff / (60 * 60 * 1000) + " heures";
        } else if (diff < 48 * 60 * 60 * 1000) {
            return "Hier";
        } else {
            return "Il y a " + diff / (24 * 60 * 60 * 1000) + " jours";
        }
    }
}