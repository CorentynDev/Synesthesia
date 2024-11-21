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
import com.example.synesthesia.models.TmdbMovie;
import com.example.synesthesia.models.Comment;
import com.example.synesthesia.models.Recommendation;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MovieDetailsActivity extends AppCompatActivity {

    private TmdbMovie movie;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EditText commentField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        movie = getIntent().getParcelableExtra("movie");

        TextView movieTitle = findViewById(R.id.movieTitle);
        TextView movieOverview = findViewById(R.id.movieOverview);
        ImageView moviePoster = findViewById(R.id.moviePoster);

        movieTitle.setText(movie.getTitle());
        movieOverview.setText(movie.getOverview());

        Glide.with(this)
                .load("https://image.tmdb.org/t/p/w500" + movie.getPosterPath())
                .into(moviePoster);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Button recommendButton = findViewById(R.id.recommendButton);
        commentField = findViewById(R.id.commentField);

        recommendButton.setOnClickListener(v -> {
            String comment = commentField.getText().toString().trim();

            submitRecommendation(movie, comment.isEmpty() ? "" : comment);
            Intent intent = new Intent(MovieDetailsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void submitRecommendation(TmdbMovie movie, String commentText) {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");

                        Comment firstComment = new Comment(userId, commentText, new Timestamp(new Date()));

                        List<Comment> commentsList = new ArrayList<>();
                        commentsList.add(firstComment);

                        Recommendation recommendation = new Recommendation(
                                movie.getTitle(),
                                null,
                                movie.getPosterPath() != null ? "https://image.tmdb.org/t/p/w500" + movie.getPosterPath() : null,
                                userId,
                                username,
                                commentsList,
                                new Timestamp(new Date()),
                                "movie",
                                commentText,
                                movie.getId()
                        );

                        db.collection("recommendations")
                                .add(recommendation)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(MovieDetailsActivity.this, "Recommendation saved successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(MovieDetailsActivity.this, "Error saving recommendation", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(MovieDetailsActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(MovieDetailsActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show());
    }
}
