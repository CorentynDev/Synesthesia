package com.example.synesthesia.fragments;

import android.os.Bundle;
import android.util.Log;
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
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.synesthesia.MainActivity;
import com.example.synesthesia.R;
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

public class BookDetailsFragment extends Fragment {

    private Book book;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EditText commentField;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_book_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FooterUtils.setupFooter(requireActivity(), R.id.createRecommendationButton);

        Bundle args = getArguments();
        if (args != null) {
            book = args.getParcelable("book");
        }

        if (book == null) {
            Log.e("BookDetailsFragment", "Book object is null");
            Toast.makeText(getContext(), "Erreur: Aucun livre sélectionné", Toast.LENGTH_SHORT).show();
            return;
        }

        TextView bookTitle = view.findViewById(R.id.bookTitle);
        TextView bookAuthor = view.findViewById(R.id.bookAuthor);
        TextView bookPublishedDate = view.findViewById(R.id.bookPublishedDate);
        TextView bookDescription = view.findViewById(R.id.bookDescription);
        ImageView bookImage = view.findViewById(R.id.bookImage);

        bookTitle.setText(book.getVolumeInfo().getTitle());
        bookAuthor.setText(book.getVolumeInfo().getAuthors() != null ? book.getVolumeInfo().getAuthors().get(0) : "Inconnu");
        bookPublishedDate.setText(book.getVolumeInfo().getPublishedDate());
        bookDescription.setText(book.getVolumeInfo().getDescription() != null ? book.getVolumeInfo().getDescription() : "Pas de description");

        if (book.getVolumeInfo().getImageLinks() != null) {
            Glide.with(this).load(book.getBestImageUrl()).placeholder(R.drawable.rotating_loader).into(bookImage);
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Button recommendButton = view.findViewById(R.id.recommendButton);
        commentField = view.findViewById(R.id.commentField);

        recommendButton.setOnClickListener(v -> {
            String comment = commentField.getText().toString().trim();
            submitRecommendation(book, comment.isEmpty() ? "" : comment);
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToMainPage();
            }
        });

        Button backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void submitRecommendation(Book book, String commentText) {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");

                        Comment firstComment = new Comment(userId, commentText, new Timestamp(new Date()));

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
                                    Toast.makeText(getContext(), "Recommendation saved with ID: " + recommendationId, Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error saving recommendation", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error fetching user data", Toast.LENGTH_SHORT).show());
    }
}