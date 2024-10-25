package com.example.synesthesia.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.synesthesia.R;
import com.example.synesthesia.models.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class BookRecommendationDialog extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_search_result, container, false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        assert getArguments() != null;
        Book book = getArguments().getParcelable("book");

        TextView titleTextView = view.findViewById(R.id.bookTitle);
        TextView authorTextView = view.findViewById(R.id.bookAuthor);
        TextView publishedDateTextView = view.findViewById(R.id.bookDate);
        ImageView bookImageView = view.findViewById(R.id.bookThumbnail);

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
}
