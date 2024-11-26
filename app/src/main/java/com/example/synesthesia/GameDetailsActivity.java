package com.example.synesthesia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.synesthesia.models.GiantBombGame;
import com.example.synesthesia.models.Comment;
import com.example.synesthesia.models.Recommendation;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class GameDetailsActivity extends AppCompatActivity {

    private GiantBombGame game;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EditText commentField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_details);

        game = getIntent().getParcelableExtra("game");

        TextView gameTitle = findViewById(R.id.gameTitle);
        TextView gameDescription = findViewById(R.id.gameDescription);
        ImageView gameImage = findViewById(R.id.gameImage);

        gameTitle.setText(game.getName());
        gameDescription.setText(game.getDescription());

        Glide.with(this)
                .load(game.getImage().getMediumUrl())
                .into(gameImage);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Button recommendButton = findViewById(R.id.recommendButton);
        commentField = findViewById(R.id.commentField);

        recommendButton.setOnClickListener(v -> {
            String comment = commentField.getText().toString().trim();

            submitRecommendation(game, comment.isEmpty() ? "" : comment);
            Intent intent = new Intent(GameDetailsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void submitRecommendation(GiantBombGame game, String commentText) {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");

                        Comment firstComment = new Comment(userId, commentText, new Timestamp(new Date()));

                        List<Comment> commentsList = new ArrayList<>();
                        commentsList.add(firstComment);

                        Recommendation recommendation = new Recommendation(
                                game.getName(),
                                null,
                                game.getImage().getMediumUrl(),
                                userId,
                                username,
                                commentsList,
                                new Timestamp(new Date()),
                                "game",
                                commentText,
                                game.getId()
                        );

                        db.collection("recommendations")
                                .add(recommendation)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(GameDetailsActivity.this, "Recommendation saved successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(GameDetailsActivity.this, "Error saving recommendation", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(GameDetailsActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(GameDetailsActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show());
    }
}
