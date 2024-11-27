package com.example.synesthesia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.synesthesia.models.Book;
import com.example.synesthesia.models.Comment;
import com.example.synesthesia.models.Recommendation;
import com.example.synesthesia.utilities.FooterUtils;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class BookDetailsActivity extends AppCompatActivity {

    private Book book;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EditText commentField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        FooterUtils.setupFooter(this, R.id.createRecommendationButton);

        book = getIntent().getParcelableExtra("book");

        TextView bookTitle = findViewById(R.id.bookTitle);
        TextView bookAuthor = findViewById(R.id.bookAuthor);
        TextView bookPublishedDate = findViewById(R.id.bookPublishedDate);
        TextView bookDescription = findViewById(R.id.bookDescription);
        ImageView bookImage = findViewById(R.id.bookImage);

        bookTitle.setText(book.getVolumeInfo().getTitle());
        bookAuthor.setText(book.getVolumeInfo().getAuthors() != null ? book.getVolumeInfo().getAuthors().get(0) : "Inconnu");
        bookPublishedDate.setText(book.getVolumeInfo().getPublishedDate());
        bookDescription.setText(book.getVolumeInfo().getDescription() != null ? book.getVolumeInfo().getDescription() : "Pas de description");

        if (book.getVolumeInfo().getImageLinks() != null) {
            Glide.with(this).load(book.getVolumeInfo().getImageLinks().getThumbnail()).into(bookImage);
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Button recommendButton = findViewById(R.id.recommendButton);
        commentField = findViewById(R.id.commentField);

        recommendButton.setOnClickListener(v -> {
            String comment = commentField.getText().toString().trim();

            submitRecommendation(book, comment.isEmpty() ? "" : comment);
            Intent intent = new Intent(BookDetailsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void submitRecommendation(Book book, String commentText) {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");

                        Comment firstComment = new Comment(userId, commentText, new com.google.firebase.Timestamp(new Date()));

                        List<Comment> commentsList = new ArrayList<>();
                        commentsList.add(firstComment);

                        Timestamp recommendationTimestamp = new Timestamp(new Date());
                        String type = "book";

                        String userNote = commentField.getText().toString().trim();

                        Recommendation recommendation = new Recommendation(
                                book.getVolumeInfo().getTitle(),
                                book.getVolumeInfo().getPublishedDate(),
                                book.getVolumeInfo().getImageLinks() != null ? book.getVolumeInfo().getImageLinks().getThumbnail() : null,
                                userId,
                                username,
                                commentsList,
                                recommendationTimestamp,
                                type,
                                userNote,
                                book.getId()
                        );

                        Log.i("BOOK ID", "Voici l'id : " + book.getId());

                        db.collection("recommendations")
                                .add(recommendation)
                                .addOnSuccessListener(documentReference -> {
                                    String recommendationId = documentReference.getId();
                                    Toast.makeText(BookDetailsActivity.this, "Recommendation saved with ID: " + recommendationId, Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(BookDetailsActivity.this, "Error saving recommendation", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(BookDetailsActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(BookDetailsActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show());
    }
}
