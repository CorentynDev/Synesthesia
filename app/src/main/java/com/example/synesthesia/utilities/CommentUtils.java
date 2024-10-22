package com.example.synesthesia.utilities;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.R;
import com.example.synesthesia.models.Comment;
import com.example.synesthesia.CommentsAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentUtils {

    private final FirebaseFirestore db;

    public CommentUtils(FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Charge le nombre de commentaires à partir de Firestore.
     */
    public void loadCommentCount(String recommendationId, TextView commentCounter) {
        db.collection("recommendations").document(recommendationId)
                .collection("comments").get()
                .addOnSuccessListener(querySnapshot -> {
                    int commentCount = querySnapshot.size();
                    commentCounter.setText(String.valueOf(commentCount));
                })
                .addOnFailureListener(e -> commentCounter.setText("0"));
    }

    /**
     * Affiche une modal pour ajouter un commentaire.
     */
    public void showCommentModal(Context context, String recommendationId, TextView commentCounter) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View modalView = inflater.inflate(R.layout.comment_modal, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(modalView);
        AlertDialog dialog = builder.create();
        dialog.show();

        EditText commentInput = modalView.findViewById(R.id.commentInput);
        Button postCommentButton = modalView.findViewById(R.id.postCommentButton);
        RecyclerView commentsRecyclerView = modalView.findViewById(R.id.commentsRecyclerView);
        ImageView closeModalButton = modalView.findViewById(R.id.closeModalButton);

        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        List<Comment> commentList = new ArrayList<>();
        CommentsAdapter adapter = new CommentsAdapter(commentList);
        commentsRecyclerView.setAdapter(adapter);

        // Charger les commentaires existants
        loadComments(recommendationId, commentList, adapter);

        postCommentButton.setOnClickListener(v -> {
            String commentText = commentInput.getText().toString().trim();
            if (!commentText.isEmpty()) {
                postComment(recommendationId, commentText, commentCounter, commentList, adapter, commentsRecyclerView);
                commentInput.setText("");
            }
        });

        closeModalButton.setOnClickListener(v -> dialog.dismiss());
    }

    /**
     * Charge les commentaires d'une recommandation.
     */
    public void loadComments(String recommendationId, List<Comment> commentList, CommentsAdapter adapter) {
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

    /**
     * Ajoute un nouveau commentaire à Firestore.
     */
    public void postComment(String recommendationId, String commentText, TextView commentCounter, List<Comment> commentList, CommentsAdapter adapter, RecyclerView commentsRecyclerView) {
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
                        adapter.notifyItemInserted(0);

                        // Mettre à jour le compteur de commentaires
                        int currentCount = Integer.parseInt(commentCounter.getText().toString());
                        commentCounter.setText(String.valueOf(currentCount + 1));

                        // Scroll automatique vers le haut pour voir le nouveau commentaire ajouté
                        commentsRecyclerView.scrollToPosition(0);
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error adding comment", e));
        }
    }
}
