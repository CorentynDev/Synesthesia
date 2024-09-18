package com.example.synesthesia;

import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

            // Permettre la soumission de la recommandation même sans commentaire
            submitRecommendation(book, comment.isEmpty() ? "" : comment);
        });
    }

    private void submitRecommendation(Book book, String commentText) {
        String userId = mAuth.getCurrentUser().getUid();

        // Récupérer le pseudo de l'utilisateur à partir de Firestore
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");

                        // Créer le premier commentaire (note de l'utilisateur)
                        Comment firstComment = new Comment(userId, commentText, new com.google.firebase.Timestamp(new Date()));

                        // Créer une liste de commentaires avec le premier commentaire
                        List<Comment> commentsList = new ArrayList<>();
                        commentsList.add(firstComment);

                        // Créer un objet Recommendation avec les informations nécessaires
                        Recommendation recommendation = new Recommendation(
                                null,  // ID sera généré automatiquement par Firestore
                                book.getVolumeInfo().getTitle(),
                                book.getVolumeInfo().getPublishedDate(),
                                book.getVolumeInfo().getImageLinks() != null ? book.getVolumeInfo().getImageLinks().getThumbnail() : null,
                                userId,
                                username,
                                commentsList  // Liste de commentaires, contenant la note
                        );

                        // Ajouter la recommandation à Firestore
                        db.collection("recommendations")
                                .add(recommendation)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(BookDetailsActivity.this, "Recommendation saved", Toast.LENGTH_SHORT).show();
                                    finish(); // Ferme l'activité après l'enregistrement
                                })
                                .addOnFailureListener(e -> Toast.makeText(BookDetailsActivity.this, "Error saving recommendation", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(BookDetailsActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(BookDetailsActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show());
    }

}
