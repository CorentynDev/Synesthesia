package com.example.synesthesia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.models.Recommendation;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.example.synesthesia.models.Comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();

        Button createRecommendationButton = findViewById(R.id.createRecommendationButton);
        createRecommendationButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Choisissez un type de recommandation");

            String[] types = {"Musique", "Film", "Jeux Vidéo", "Livre"};

            builder.setItems(types, (dialog, which) -> {
                switch (which) {
                    case 0: // Musique
                        Intent musicIntent = new Intent(MainActivity.this, SearchMusicActivity.class);
                        startActivity(musicIntent);
                        break;
                    case 1:
                        // Lancer une activité pour la création de recommandation de films (si implémentée plus tard)
                        break;
                    case 2:
                        // Lancer une activité pour la création de recommandation de jeux vidéo (si implémentée plus tard)
                        Intent game_intent = new Intent(MainActivity.this, SearchGameActivity.class);
                        startActivity(game_intent);
                        break;
                    case 3: // Livre
                        // Lancer l'Activity pour la recherche de livres
                        Intent book_intent = new Intent(MainActivity.this, SearchBookActivity.class);
                        startActivity(book_intent);
                        break;
                }
            });
            builder.create().show();
        });

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

    public void getRecommendationData() {
        Log.d("MainActivity", "Starting to fetch recommendations");
        db.collection("recommendations").get().addOnSuccessListener(queryDocumentSnapshots -> {
            Log.d("MainActivity", "Successfully fetched recommendations");
            LinearLayout recommendationList = findViewById(R.id.recommendationList);
            recommendationList.removeAllViews();

            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Log.d("MainActivity", "Document ID: " + document.getId());

                Recommendation recommendation = document.toObject(Recommendation.class);

                if (recommendation != null) {
                    recommendation.setId(document.getId());
                    Log.d("MainActivity", "Recommendation loaded: " + recommendation.getTitle() + " with ID: " + recommendation.getId());

                    addRecommendationCard(recommendationList, recommendation);
                } else {
                    Log.e("MainActivity", "Failed to parse recommendation");
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("FirestoreData", "Error when fetching documents: ", e);
        });
    }

    public void addRecommendationCard(LinearLayout container, Recommendation recommendation) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.recommendation_card, container, false);

        // Récupérer et afficher le titre de la recommandation
        TextView titleTextView = cardView.findViewById(R.id.recommendationTitle);
        titleTextView.setText(recommendation.getTitle());

        // Récupérer et afficher la date de la recommandation
        TextView dateTextView = cardView.findViewById(R.id.recommendationDate);
        dateTextView.setText(recommendation.getDate());

        // Récupérer et afficher le pseudo utilisateur
        TextView userTextView = cardView.findViewById(R.id.recommendationUser);
        db.collection("users").document(recommendation.getUserId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        userTextView.setText(username);
                    } else {
                        userTextView.setText("Utilisateur inconnu");
                    }
                })
                .addOnFailureListener(e -> {
                    userTextView.setText("Erreur de chargement");
                });

        // Récupérer et afficher l'image de couverture
        ImageView coverImageView = cardView.findViewById(R.id.recommendationCover);
        if (recommendation.getCoverUrl() != null && !recommendation.getCoverUrl().isEmpty()) {
            Glide.with(this)
                    .load(recommendation.getCoverUrl()) // Utiliser coverUrl pour charger l'image
                    .placeholder(R.drawable.placeholder_image) // Image de remplacement pendant le chargement
                    .into(coverImageView);
        } else {
            coverImageView.setImageResource(R.drawable.placeholder_image); // Image par défaut si l'URL est vide ou manquante
        }

        // Gestion du bouton "like"
        ImageView likeButton = cardView.findViewById(R.id.likeButton);
        TextView likeCounter = cardView.findViewById(R.id.likeCounter);
        ImageView commentButton = cardView.findViewById(R.id.commentButton);

        // Initialisation des likes
        List<String> likedBy = recommendation.getLikedBy();
        if (likedBy == null) {
            likedBy = new ArrayList<>();
            recommendation.setLikedBy(likedBy);
        }

        likeCounter.setText(String.valueOf(recommendation.getLikesCount()));

        likeButton.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                boolean isCurrentlyLiked = isLiked(userId, recommendation);

                updateLikeUI(likeButton, likeCounter, isCurrentlyLiked, recommendation.getLikesCount());

                toggleLike(recommendation.getId(), userId, !isCurrentlyLiked);

                // Utiliser updateLikeList pour gérer likedBy
                updateLikeList(userId, recommendation, !isCurrentlyLiked);
                recommendation.setLikesCount(isCurrentlyLiked ? recommendation.getLikesCount() - 1 : recommendation.getLikesCount() + 1);
            }
        });

        // Gestion des commentaires
        commentButton.setOnClickListener(v -> {
            showCommentModal(recommendation.getId());
        });

        // Ajouter la carte à la vue conteneur
        container.addView(cardView);
    }

    private void updateLikeList(String userId, Recommendation recommendation, boolean addLike) {
        List<String> likedBy = recommendation.getLikedBy();
        if (likedBy == null) {
            likedBy = new ArrayList<>();
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
            likeButton.setImageResource(R.drawable.like); // Icône "non liké"
            likeCounter.setText(String.valueOf(currentLikesCount - 1)); // Diminue le compteur
        } else {
            likeButton.setImageResource(R.drawable.given_like); // Icône "liké"
            likeCounter.setText(String.valueOf(currentLikesCount + 1)); // Augmente le compteur
        }
    }

    private boolean isLiked(String userId, Recommendation recommendation) {
        List<String> likedBy = recommendation.getLikedBy();
        return likedBy != null && likedBy.contains(userId);
    }

    private void toggleLike(String recommendationId, String userId, boolean isLiked) {
        if (recommendationId == null || userId == null) {
            Log.e("ToggleLike", "Recommendation ID or User ID is null");
            return;
        }

        DocumentReference recommendationRef = db.collection("recommendations").document(recommendationId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(recommendationRef);
            if (!snapshot.exists()) {
                throw new FirebaseFirestoreException("Document does not exist", FirebaseFirestoreException.Code.NOT_FOUND);
            }

            int likesCount = snapshot.getLong("likesCount").intValue();
            List<String> likedBy = (List<String>) snapshot.get("likedBy");

            if (likedBy == null) {
                likedBy = new ArrayList<>();
            }

            if (isLiked) {
                if (!likedBy.contains(userId)) {
                    likedBy.add(userId);
                    likesCount++;
                }
            } else {
                if (likedBy.contains(userId)) {
                    likedBy.remove(userId);
                    likesCount--;
                }
            }

            transaction.update(recommendationRef, "likesCount", likesCount);
            transaction.update(recommendationRef, "likedBy", likedBy);

            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("ToggleLike", "Transaction success!");
        }).addOnFailureListener(e -> {
            Log.e("ToggleLike", "Transaction failure.", e);
        });
    }

    private void showCommentModal(String recommendationId) {
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

        loadComments(recommendationId, commentList, adapter);

        postCommentButton.setOnClickListener(v -> {
            String commentText = commentInput.getText().toString().trim();
            if (!commentText.isEmpty()) {
                postComment(recommendationId, commentText);
                commentInput.setText("");
            }
        });

        closeModalButton.setOnClickListener(v -> dialog.dismiss());
    }

    private void loadComments(String recommendationId, List<Comment> commentList, CommentsAdapter adapter) {
        db.collection("recommendations").document(recommendationId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("Firestore", "Error loading comments", e);
                        return;
                    }

                    commentList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Comment comment = doc.toObject(Comment.class);
                        commentList.add(comment);
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void postComment(String recommendationId, String commentText) {
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
                        updateCommentCount(recommendationId, 1);
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error adding comment", e));
        }
    }

    private void updateCommentCount(String recommendationId, int countChange) {
        DocumentReference recommendationRef = db.collection("recommendations").document(recommendationId);
        recommendationRef.update("commentsCount", FieldValue.increment(countChange))
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Comment count updated"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating comment count", e));
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
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                            String username = documentSnapshot.getString("username");

                            if (username != null && !username.isEmpty()) {
                                profileSummary.setText("Welcome, " + username + "!");
                            }

                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.placeholder_image)
                                        .into(profileImageView);
                            }
                        } else {
                            Log.d("UserProfile", "No such document");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("UserProfile", "Error fetching user data", e);
                    });
        }
    }
}
