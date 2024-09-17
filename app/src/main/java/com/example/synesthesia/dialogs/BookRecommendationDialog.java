package com.example.synesthesia.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.synesthesia.R;
import com.example.synesthesia.models.Book;
import com.example.synesthesia.models.Recommendation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class BookRecommendationDialog extends DialogFragment {

    private Book book;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public static BookRecommendationDialog newInstance(Book book) {
        BookRecommendationDialog dialog = new BookRecommendationDialog();
        Bundle args = new Bundle();
        args.putSerializable("book", book);
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_book_recommendation, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        book = (Book) getArguments().getSerializable("book");

        TextView titleTextView = view.findViewById(R.id.bookTitleTextView);
        TextView authorTextView = view.findViewById(R.id.bookAuthorTextView);
        TextView publishedDateTextView = view.findViewById(R.id.bookPublishedDateTextView);
        ImageView bookImageView = view.findViewById(R.id.bookImageView);
        EditText commentEditText = view.findViewById(R.id.commentEditText);
        Button recommendButton = view.findViewById(R.id.recommendButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);

        titleTextView.setText(book.getVolumeInfo().getTitle());
        authorTextView.setText(book.getVolumeInfo().getAuthors() != null
                ? book.getVolumeInfo().getAuthors().get(0) : "Inconnu");
        publishedDateTextView.setText(book.getVolumeInfo().getPublishedDate());
        if (book.getVolumeInfo().getImageLinks() != null) {
            Glide.with(requireContext())
                    .load(book.getVolumeInfo().getImageLinks().getThumbnail())
                    .into(bookImageView);
        }

        recommendButton.setOnClickListener(v -> {
            String comment = commentEditText.getText().toString();
            submitRecommendation(book, comment);
            dismiss();
        });

        cancelButton.setOnClickListener(v -> dismiss());

        return view;
    }

    private void submitRecommendation(Book book, String comment) {
        String userId = mAuth.getCurrentUser().getUid();

        // Créer un objet de recommandation avec les détails
        Recommendation recommendation = new Recommendation(
                book.getVolumeInfo().getTitle(),
                book.getVolumeInfo().getAuthors() != null ? book.getVolumeInfo().getAuthors().get(0) : "Inconnu",
                book.getVolumeInfo().getPublishedDate(),
                comment,
                userId
        );

        // Ajouter la recommandation à Firestore
        db.collection("recommendations")
                .add(recommendation)
                .addOnSuccessListener(documentReference -> {
                    // Optionnel : gérer le succès de l'ajout
                    // Par exemple, afficher un message de succès ou mettre à jour l'interface utilisateur
                })
                .addOnFailureListener(e -> {
                    // Optionnel : gérer l'échec de l'ajout
                    // Par exemple, afficher un message d'erreur
                });
    }
}
