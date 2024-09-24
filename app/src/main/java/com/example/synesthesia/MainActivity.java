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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.synesthesia.models.Comment;
import com.example.synesthesia.models.Recommendation;
import com.google.android.material.button.MaterialButton;
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
import com.squareup.picasso.Picasso;

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

        ImageView profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
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
                        // Lancer une activité pour la création de recommandation de films (si implémentée plus tard)
                        break;
                    case 2:
                        // Lancer une activité pour la création de recommandation de jeux vidéo (si implémentée plus tard)
                        break;
                    case 3:
                        Intent bookIntent = new Intent(MainActivity.this, SearchBookActivity.class);
                        startActivity(bookIntent);
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
                    String recommendationId = document.getId();
                    Log.d("MainActivity", "Recommendation loaded: " + recommendation.getTitle() + " with ID: " + recommendationId);

                    addRecommendationCard(recommendationList, recommendation, recommendationId);
                } else {
                    Log.e("MainActivity", "Failed to parse recommendation");
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("FirestoreData", "Error when fetching documents: ", e);
        });
    }

    public void addRecommendationCard(LinearLayout container, Recommendation recommendation, String recommendationId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.recommendation_card, container, false);

        TextView titleTextView = cardView.findViewById(R.id.recommendationTitle);
        titleTextView.setText(recommendation.getTitle());

        TextView dateTextView = cardView.findViewById(R.id.recommendationDate);
        Timestamp timestamp = recommendation.getTimestamp();
        if (timestamp != null) {
            dateTextView.setText(getTimeAgo(timestamp));
        } else {
            dateTextView.setText("Date inconnue");
        }

        TextView userTextView = cardView.findViewById(R.id.recommendationUser);
        ImageView profileImageView = cardView.findViewById(R.id.profileImageView);
        db.collection("users").document(recommendation.getUserId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        userTextView.setText(username);
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        if (profileImageUrl != null) {
                            Picasso.get().load(profileImageUrl).into(profileImageView);
                        }
                    } else {
                        userTextView.setText("Utilisateur inconnu");
                    }
                })
                .addOnFailureListener(e -> {
                    userTextView.setText("Erreur de chargement");
                });

        ImageView coverImageView = cardView.findViewById(R.id.recommendationCover);
        if (recommendation.getCoverUrl() != null && !recommendation.getCoverUrl().isEmpty()) {
            Glide.with(this)
                    .load(recommendation.getCoverUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .into(coverImageView);
        } else {
            coverImageView.setImageResource(R.drawable.placeholder_image);
        }

        ImageView likeButton = cardView.findViewById(R.id.likeButton);
        TextView likeCounter = cardView.findViewById(R.id.likeCounter);
        ImageView commentButton = cardView.findViewById(R.id.commentButton);

        final List<String> likedBy;
        if (recommendation.getLikedBy() == null) {
            likedBy = new ArrayList<>();
            recommendation.setLikedBy(likedBy);
        } else {
            likedBy = recommendation.getLikedBy();
        }

        likeCounter.setText(String.valueOf(likedBy.size()));

        likeButton.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                boolean isCurrentlyLiked = isLiked(userId, recommendation);

                updateLikeUI(likeButton, likeCounter, isCurrentlyLiked, likedBy.size());

                toggleLike(recommendationId, userId, !isCurrentlyLiked);

                updateLikeList(userId, recommendation, !isCurrentlyLiked);
            }
        });

        commentButton.setOnClickListener(v -> {
            showCommentModal(recommendationId);
        });

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
                    assert queryDocumentSnapshots != null;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Comment comment = doc.toObject(Comment.class);
                        String userId = comment.getUserId(); // Récupérer l'ID de l'utilisateur

                        // Récupérer les informations de l'utilisateur
                        db.collection("users").document(userId).get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        String username = userDoc.getString("username");
                                        String profileImageUrl = userDoc.getString("profileImageUrl");

                                        // Créer un objet Comment avec les informations de l'utilisateur
                                        comment.setUsername(username); // Assurez-vous que Comment a un champ pour le nom
                                        comment.setProfileImageUrl(profileImageUrl); // Assurez-vous que Comment a un champ pour l'image

                                        commentList.add(comment);
                                        adapter.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(e1 -> {
                                    Log.e("UserProfile", "Error fetching user data", e1);
                                });
                    }
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
                        // Pas besoin de mettre à jour le nombre de commentaires
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
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                            String username = documentSnapshot.getString("username");

                            if (username != null && !username.isEmpty()) {
                                profileSummary.setText(username);
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

    private String getTimeAgo(Timestamp timestamp) {
        long time = timestamp.toDate().getTime();
        long now = System.currentTimeMillis();

        if (time > now || time <= 0) {
            return "à l'instant";
        }

        final long diff = now - time;
        if (diff < 60 * 1000) { // moins d'une minute
            return "Il y a " + diff / 1000 + " secondes";
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