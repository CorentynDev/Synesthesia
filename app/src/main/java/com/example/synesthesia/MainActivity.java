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
                        // Handle Film
                        break;
                    case 2:
                        // Handle Jeux Vidéo
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

    public void getRecommendationData() {
        Log.d("MainActivity", "Starting to fetch recommendations");

        swipeRefreshLayout.setRefreshing(true);

        db.collection("recommendations").get().addOnSuccessListener(queryDocumentSnapshots -> {
            Log.d("MainActivity", "Successfully fetched recommendations");
            LinearLayout recommendationList = findViewById(R.id.recommendationList);
            recommendationList.removeAllViews();

            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Recommendation recommendation = document.toObject(Recommendation.class);
                if (recommendation != null) {
                    addRecommendationCard(recommendationList, recommendation, document.getId());
                }
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
                        userTextView.setText(documentSnapshot.getString("username"));
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(this).load(profileImageUrl).into(profileImageView);
                        } else {
                            profileImageView.setImageResource(R.drawable.placeholder_image);
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
            Glide.with(this).load(recommendation.getCoverUrl()).placeholder(R.drawable.placeholder_image).into(coverImageView);
        } else {
            coverImageView.setImageResource(R.drawable.placeholder_image);
        }

        ImageView likeButton = cardView.findViewById(R.id.likeButton);
        TextView likeCounter = cardView.findViewById(R.id.likeCounter);
        TextView commentCounter = cardView.findViewById(R.id.commentCounter);
        ImageView commentButton = cardView.findViewById(R.id.commentButton);

        List<String> likedBy = recommendation.getLikedBy() != null ? recommendation.getLikedBy() : new ArrayList<>();
        likeCounter.setText(String.valueOf(likedBy.size()));

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            final boolean[] isCurrentlyLiked = {isLiked(userId, recommendation)};

            likeButton.setImageResource(isCurrentlyLiked[0] ? R.drawable.given_like : R.drawable.like);

            likeButton.setOnClickListener(v -> {
                boolean newLikeStatus = !isCurrentlyLiked[0];
                updateLikeUI(likeButton, likeCounter, newLikeStatus, likedBy.size());
                updateLikeList(userId, recommendation, newLikeStatus);
                toggleLike(recommendationId, userId, newLikeStatus, () -> {
                    // Callback après la mise à jour de la base de données
                    isCurrentlyLiked[0] = newLikeStatus;
                });
            });
        }

        db.collection("recommendations").document(recommendationId)
                .collection("comments").get()
                .addOnSuccessListener(querySnapshot -> {
                    int commentCount = querySnapshot.size();
                    commentCounter.setText(commentCount + " commentaires");
                })
                .addOnFailureListener(e -> {
                    commentCounter.setText("0 commentaires");
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
            likeButton.setImageResource(R.drawable.given_like);
            likeCounter.setText(String.valueOf(currentLikesCount + 1));
        } else {
            likeButton.setImageResource(R.drawable.like);
            likeCounter.setText(String.valueOf(currentLikesCount - 1));
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