package com.example.synesthesia;

import android.content.Context;
import android.content.Intent;
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
    private Context context;

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

    class BookViewHolder extends RecyclerView.ViewHolder {

        private ImageView bookCoverImageView;
        private TextView bookTitleTextView;
        private TextView bookAuthorTextView;
        private TextView bookPublishedDateTextView;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCoverImageView = itemView.findViewById(R.id.bookThumbnail);
            bookTitleTextView = itemView.findViewById(R.id.bookTitle);
            bookAuthorTextView = itemView.findViewById(R.id.bookAuthor);
            bookPublishedDateTextView = itemView.findViewById(R.id.bookDate);

            // Quand l'utilisateur clique sur un livre
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Book selectedBook = books.get(position);
                    // Ouvre BookDetailsActivity
                    Intent intent = new Intent(context, BookDetailsActivity.class);
                    intent.putExtra("book", selectedBook);
                    context.startActivity(intent);
                }
            });
        }

        public void bind(Book book) {
            if (book.getVolumeInfo() != null) {
                bookTitleTextView.setText(book.getVolumeInfo().getTitle() != null ? book.getVolumeInfo().getTitle() : "Titre inconnu");
                bookAuthorTextView.setText(book.getVolumeInfo().getAuthors() != null && !book.getVolumeInfo().getAuthors().isEmpty() ? book.getVolumeInfo().getAuthors().get(0) : "Auteur inconnu");
                bookPublishedDateTextView.setText(book.getVolumeInfo().getPublishedDate() != null ? book.getVolumeInfo().getPublishedDate() : "Date inconnue");

                if (book.getVolumeInfo().getImageLinks() != null) {
                    Glide.with(bookCoverImageView.getContext())
                            .load(book.getVolumeInfo().getImageLinks().getThumbnail())
                            .placeholder(R.color.gray_medium) // Image de remplacement pendant le chargement
                            .error(R.color.red) // Image de remplacement en cas d'erreur
                            .into(bookCoverImageView);
                } else {
                    // Si les liens d'image sont nuls, mettre une image de remplacement ou un placeholder
                    bookCoverImageView.setImageResource(R.drawable.placeholder_image); // Assure-toi d'avoir une image placeholder dans res/drawable
                }
            } else {
                // Gérer le cas où book.getVolumeInfo() est nul
                bookTitleTextView.setText("Titre inconnu");
                bookAuthorTextView.setText("Auteur inconnu");
                bookPublishedDateTextView.setText("Date inconnue");
                bookCoverImageView.setImageResource(R.drawable.placeholder_image);
            }
        }
    }
}
