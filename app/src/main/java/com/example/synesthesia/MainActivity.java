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
            // Création de la fenêtre modale avec les options de types de recommandation
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Choisissez un type de recommandation");

            // Options de type de recommandation
            String[] types = {"Musique", "Film", "Jeux Vidéo", "Livre"};

            // Gestion du clic sur l'une des options
            builder.setItems(types, (dialog, which) -> {
                switch (which) {
                    case 0: // Musique
                        // Lancer une activité pour la création de recommandation musicale (si implémentée plus tard)
                        break;
                    case 1: // Film
                        // Lancer une activité pour la création de recommandation de films (si implémentée plus tard)
                        break;
                    case 2: // Jeux Vidéo
                        // Lancer une activité pour la création de recommandation de jeux vidéo (si implémentée plus tard)
                        break;
                    case 3: // Livre
                        // Lancer l'Activity pour la recherche de livres
                        Intent intent = new Intent(MainActivity.this, SearchBookActivity.class);
                        startActivity(intent);
                        break;
                }
            });
            // Afficher la fenêtre modale
            builder.create().show();
        });


        // Lire les données
        getRecommendationData();

        // Appelle la méthode pour récupérer les infos de l'utilisateur
        getUserProfile();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Redirection vers LoginActivity si l'utilisateur n'est pas connecté
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void getRecommendationData() {
        Log.d("MainActivity", "Starting to fetch recommendations");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("recommendations").get().addOnSuccessListener(queryDocumentSnapshots -> {
            Log.d("MainActivity", "Successfully fetched recommendations");
            LinearLayout recommendationList = findViewById(R.id.recommendationList);
            recommendationList.removeAllViews();

            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Log.d("MainActivity", "Document ID: " + document.getId());
                Recommendation recommendation = document.toObject(Recommendation.class);
                if (recommendation != null) {
                    Log.d("MainActivity", "Recommendation loaded: " + recommendation.getTitle());
                    // Utilise un code simplifié pour ajouter la carte, sans autres appels Firestore
                    addRecommendationCard(recommendationList, recommendation);
                } else {
                    Log.e("MainActivity", "Failed to parse recommendation");
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("FirestoreData", "Error when fetching documents: ", e);
        });
    }

    private void addSimpleRecommendationCard(LinearLayout container, Recommendation recommendation) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.recommendation_card, container, false);

        TextView titleTextView = cardView.findViewById(R.id.recommendationTitle);
        titleTextView.setText(recommendation.getTitle());

        // Ajouter la carte à la vue parent
        container.addView(cardView);
    }

    public void addRecommendationCard(LinearLayout container, Recommendation recommendation) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.recommendation_card, container, false);

        TextView titleTextView = cardView.findViewById(R.id.recommendationTitle);
        titleTextView.setText(recommendation.getTitle());

        TextView dateTextView = cardView.findViewById(R.id.recommendationDate);
        dateTextView.setText(recommendation.getDate());

        TextView userTextView = cardView.findViewById(R.id.recommendationUser);

        // Rechercher le pseudo de l'utilisateur avec l'ID utilisateur
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

        // Configurer le bouton de like
        ImageView likeButton = cardView.findViewById(R.id.likeButton);
        TextView likeCounter = cardView.findViewById(R.id.likeCounter);
        ImageView commentButton = cardView.findViewById(R.id.commentButton);

        // Configurer l'affichage du nombre de likes
        likeCounter.setText(String.valueOf(recommendation.getLikesCount()));

        likeButton.setOnClickListener(v -> {
            // Vérifier si l'utilisateur a déjà aimé cette recommandation
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                toggleLike(recommendation.getId(), userId, !isLiked(userId, recommendation));
            }
        });

        commentButton.setOnClickListener(v -> {
            showCommentModal(recommendation.getId());
        });

        // Ajoutez la carte à la vue parent
        container.addView(cardView);
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

            // Lire les données actuelles
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

            // Mettre à jour les données
            transaction.update(recommendationRef, "likesCount", likesCount);
            transaction.update(recommendationRef, "likedBy", likedBy);

            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("ToggleLike", "Transaction success!");
            // Vous pouvez également mettre à jour l'UI ici si nécessaire
        }).addOnFailureListener(e -> {
            Log.e("ToggleLike", "Transaction failure.", e);
        });
    }

    private void showCommentModal(String recommendationId) {
        // Inflate the modal view
        LayoutInflater inflater = LayoutInflater.from(this);
        View modalView = inflater.inflate(R.layout.comment_modal, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(modalView);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Récupérer les vues de la modale
        EditText commentInput = modalView.findViewById(R.id.commentInput);
        Button postCommentButton = modalView.findViewById(R.id.postCommentButton);
        RecyclerView commentsRecyclerView = modalView.findViewById(R.id.commentsRecyclerView);
        ImageView closeModalButton = modalView.findViewById(R.id.closeModalButton);

        // Configurer le RecyclerView pour afficher les commentaires
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<Comment> commentList = new ArrayList<>();
        CommentsAdapter adapter = new CommentsAdapter(commentList);
        commentsRecyclerView.setAdapter(adapter);

        // Charger les commentaires depuis Firestore
        loadComments(recommendationId, commentList, adapter);

        // Gestion du clic pour poster un commentaire
        postCommentButton.setOnClickListener(v -> {
            String commentText = commentInput.getText().toString().trim();
            if (!commentText.isEmpty()) {
                postComment(recommendationId, commentText);
                commentInput.setText(""); // Vider le champ après publication
            }
        });

        // Gestion du clic pour fermer la modale
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

            // Créer un objet commentaire
            Map<String, Object> comment = new HashMap<>();
            comment.put("userId", userId);
            comment.put("commentText", commentText);
            comment.put("timestamp", FieldValue.serverTimestamp());

            // Ajouter le commentaire à la collection Firestore
            db.collection("recommendations").document(recommendationId)
                    .collection("comments").add(comment)
                    .addOnSuccessListener(documentReference -> {
                        // Mise à jour du compteur de commentaires
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

    // Méthode pour récupérer et afficher le pseudonyme de l'utilisateur
    private void getUserProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // Référence à la TextView pour le pseudonyme
        TextView profileSummary = findViewById(R.id.profileSummary);
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Rechercher les informations de l'utilisateur dans Firestore
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Récupérer le pseudonyme de l'utilisateur
                            String username = documentSnapshot.getString("username");

                            // Afficher le pseudonyme dans la TextView
                            profileSummary.setText("Welcome, " + username + "!");
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
