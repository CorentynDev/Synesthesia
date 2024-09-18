package com.example.synesthesia.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
        args.putParcelable("book", book);  // Utiliser putParcelable() ici
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_search_result, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Récupérer l'objet Book depuis les arguments
        book = getArguments().getParcelable("book");  // Utiliser getParcelable() ici

        // Initialiser les éléments de la vue
        TextView titleTextView = view.findViewById(R.id.bookTitle);
        TextView authorTextView = view.findViewById(R.id.bookAuthor);
        TextView publishedDateTextView = view.findViewById(R.id.bookDate);
        ImageView bookImageView = view.findViewById(R.id.bookThumbnail);

        // Remplir les données du livre
        if (book != null) {
            titleTextView.setText(book.getVolumeInfo().getTitle());
            authorTextView.setText(book.getVolumeInfo().getAuthors() != null ? book.getVolumeInfo().getAuthors().get(0) : "Inconnu");
            publishedDateTextView.setText(book.getVolumeInfo().getPublishedDate());
            if (book.getVolumeInfo().getImageLinks() != null) {
                Glide.with(requireContext())
                        .load(book.getVolumeInfo().getImageLinks().getThumbnail())
                        .into(bookImageView);
            }
        }

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
                    Toast.makeText(getContext(), "Recommandation créée", Toast.LENGTH_SHORT).show();
                    dismiss(); // Ferme la fenêtre modale après la création
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur lors de la création de la recommandation", Toast.LENGTH_SHORT).show();
                });
    }
}
