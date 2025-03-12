package com.example.synesthesia.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.synesthesia.MainActivity;
import com.example.synesthesia.R;
import com.example.synesthesia.models.Book;

import java.util.List;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BookViewHolder> {

    private List<Book> books;  // List des livres
    private final Context context;

    public BooksAdapter(List<Book> books, Context context) {
        this.books = books;
        this.context = context;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
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

    // Ajouter de nouveaux livres à la liste
    public void addBooks(List<Book> newBooks) {
        int startPosition = books.size();  // Position de départ des nouveaux livres
        books.addAll(newBooks);  // Ajouter les livres à la liste
        notifyItemRangeInserted(startPosition, newBooks.size());  // Notifier l'adaptateur du changement
    }

    class BookViewHolder extends RecyclerView.ViewHolder {

        private final ImageView bookCoverImageView;
        private final TextView bookTitleTextView;
        private final TextView bookAuthorTextView;
        private final TextView bookPublishedDateTextView;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCoverImageView = itemView.findViewById(R.id.bookThumbnail);
            bookTitleTextView = itemView.findViewById(R.id.bookTitle);
            bookAuthorTextView = itemView.findViewById(R.id.bookAuthor);
            bookPublishedDateTextView = itemView.findViewById(R.id.bookDate);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Book selectedBook = books.get(position);
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).showBookDetailsFragment(selectedBook);
                    }
                }
            });
        }

        public void bind(Book book) {
            if (book.getVolumeInfo() != null) {
                bookTitleTextView.setText(book.getVolumeInfo().getTitle() != null ? book.getVolumeInfo().getTitle() : "Titre inconnu");
                bookAuthorTextView.setText(book.getVolumeInfo().getAuthors() != null && !book.getVolumeInfo().getAuthors().isEmpty() ? book.getVolumeInfo().getAuthors().get(0) : "Auteur inconnu");
                bookPublishedDateTextView.setText(book.getVolumeInfo().getPublishedDate() != null ? book.getVolumeInfo().getPublishedDate() : "Date inconnue");

                if (book.getVolumeInfo().getImageLinks() != null && book.getVolumeInfo().getImageLinks().getThumbnail() != null) {
                    String thumbnailUrl = book.getVolumeInfo().getImageLinks().getThumbnail();
                    Log.d("BooksAdapter", "Thumbnail URL: " + thumbnailUrl);

                    Glide.with(bookCoverImageView.getContext())
                            .load(thumbnailUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .skipMemoryCache(false)
                            .placeholder(R.drawable.rotating_loader)
                            .error(R.drawable.placeholder_image)
                            .into(bookCoverImageView);
                } else {
                    bookCoverImageView.setImageResource(R.drawable.placeholder_image);
                }
            } else {
                bookTitleTextView.setText("Titre inconnu");
                bookAuthorTextView.setText("Auteur inconnu");
                bookPublishedDateTextView.setText("Date inconnue");
                bookCoverImageView.setImageResource(R.drawable.placeholder_image);
            }
        }
    }
}
