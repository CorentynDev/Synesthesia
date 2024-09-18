package com.example.synesthesia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.synesthesia.models.Book;
import com.example.synesthesia.models.Recommendation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class BookDetailsActivity extends AppCompatActivity {

    private Book book;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        // Récupérer les informations du livre passé par Intent
        book = getIntent().getParcelableExtra("book");

        // Initialiser et remplir les vues avec les données du livre
        TextView bookTitle = findViewById(R.id.bookTitle);
        TextView bookAuthor = findViewById(R.id.bookAuthor);
        TextView bookPublishedDate = findViewById(R.id.bookPublishedDate);
        TextView bookDescription = findViewById(R.id.bookDescription);
        ImageView bookImage = findViewById(R.id.bookImage);

        bookTitle.setText(book.getVolumeInfo().getTitle());
        bookAuthor.setText(book.getVolumeInfo().getAuthors() != null ? book.getVolumeInfo().getAuthors().get(0) : "Inconnu");
        bookPublishedDate.setText(book.getVolumeInfo().getPublishedDate());
        bookDescription.setText(book.getVolumeInfo().getDescription() != null ? book.getVolumeInfo().getDescription() : "Pas de description");

        // Charger l'image de couverture
        if (book.getVolumeInfo().getImageLinks() != null) {
            Glide.with(this).load(book.getVolumeInfo().getImageLinks().getThumbnail()).into(bookImage);
        }

        // Initialisation de Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Bouton pour soumettre une recommandation
        Button submitButton = findViewById(R.id.saveButton);
        EditText commentField = findViewById(R.id.commentField);

        submitButton.setOnClickListener(v -> {
            String comment = commentField.getText().toString().trim();
            if (!comment.isEmpty()) {
                submitRecommendation(book, comment);
            } else {
                Toast.makeText(BookDetailsActivity.this, "Please enter a comment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitRecommendation(Book book, String comment) {
        String userId = mAuth.getCurrentUser().getUid();

        Recommendation recommendation = new Recommendation(
                book.getVolumeInfo().getTitle(),
                book.getVolumeInfo().getAuthors() != null ? book.getVolumeInfo().getAuthors().get(0) : "Inconnu",
                book.getVolumeInfo().getPublishedDate(),
                comment,
                userId
        );

        db.collection("recommendations")
                .add(recommendation)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(BookDetailsActivity.this, "Recommendation saved", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity after saving
                })
                .addOnFailureListener(e -> Toast.makeText(BookDetailsActivity.this, "Error saving recommendation", Toast.LENGTH_SHORT).show());
    }
}
