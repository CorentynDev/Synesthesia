package com.example.synesthesia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.synesthesia.models.Comment;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentBottomSheet extends BottomSheetDialogFragment {

    private FirebaseFirestore db;
    private String recommendationId;

    public CommentBottomSheet(String recommendationId) {
        this.recommendationId = recommendationId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.comment_modal, container, false);

        EditText commentInput = view.findViewById(R.id.commentInput);
        Button postCommentButton = view.findViewById(R.id.postCommentButton);
        RecyclerView commentsRecyclerView = view.findViewById(R.id.commentsRecyclerView);
        ImageView closeModalButton = view.findViewById(R.id.closeModalButton);

        db = FirebaseFirestore.getInstance();

        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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

        closeModalButton.setOnClickListener(v -> dismiss());

        return view;
    }

    private void loadComments(String recommendationId, List<Comment> commentList, CommentsAdapter adapter) {
        db.collection("recommendations").document(recommendationId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
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
                    .collection("comments").add(comment);
        }
    }
}
