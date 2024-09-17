package com.example.synesthesia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.synesthesia.models.Book;

import java.util.List;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BookViewHolder> {

    private List<Book> books;
    private OnBookSelectedListener onBookSelectedListener;

    public interface OnBookSelectedListener {
        void onBookSelected(Book book);
    }

    public BooksAdapter(List<Book> books, OnBookSelectedListener onBookSelectedListener) {
        this.books = books;
        this.onBookSelectedListener = onBookSelectedListener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_book_recommendation, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bind(book);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    class BookViewHolder extends RecyclerView.ViewHolder {

        private ImageView bookCoverImageView;
        private TextView bookTitleTextView;
        private TextView bookAuthorTextView;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCoverImageView = itemView.findViewById(R.id.bookImageView);
            bookTitleTextView = itemView.findViewById(R.id.bookTitleTextView);
            bookAuthorTextView = itemView.findViewById(R.id.bookAuthorTextView);

            itemView.setOnClickListener(v -> {
                if (onBookSelectedListener != null) {
                    onBookSelectedListener.onBookSelected(books.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Book book) {
            bookTitleTextView.setText(book.getVolumeInfo().getTitle());
            bookAuthorTextView.setText(book.getVolumeInfo().getAuthors() != null ? book.getVolumeInfo().getAuthors().get(0) : "Unknown");

            // Charger l'image de couverture avec Glide
            Glide.with(bookCoverImageView.getContext())
                    .load(book.getVolumeInfo().getImageLinks().getThumbnail())
                    .placeholder(R.color.gray_medium) // Image de remplacement pendant le chargement
                    .error(R.color.red) // Image de remplacement en cas d'erreur
                    .into(bookCoverImageView);
        }
    }
}
