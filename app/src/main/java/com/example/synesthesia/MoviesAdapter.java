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
import com.example.synesthesia.models.TmdbMovie;

import java.util.List;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MovieViewHolder> {

    private final List<TmdbMovie> movies;
    private final Context context;

    public MoviesAdapter(List<TmdbMovie> movies, Context context) {
        this.movies = movies;
        this.context = context;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        TmdbMovie movie = movies.get(position);
        holder.bind(movie);
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {

        private final ImageView moviePosterImageView;
        private final TextView movieTitleTextView;
        private final TextView movieOverviewTextView;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            moviePosterImageView = itemView.findViewById(R.id.bookThumbnail);
            movieTitleTextView = itemView.findViewById(R.id.bookTitle);
            movieOverviewTextView = itemView.findViewById(R.id.bookAuthor);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    TmdbMovie selectedMovie = movies.get(position);
                    Intent intent = new Intent(context, MovieDetailsActivity.class);
                    intent.putExtra("movie", selectedMovie);
                    context.startActivity(intent);
                }
            });
        }

        public void bind(TmdbMovie movie) {
            movieTitleTextView.setText(movie.getTitle() != null ? movie.getTitle() : "Titre inconnu");
            movieOverviewTextView.setText(movie.getOverview() != null ? movie.getOverview() : "Description indisponible");

            if (movie.getPosterPath() != null) {
                String posterUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();
                Glide.with(moviePosterImageView.getContext())
                        .load(posterUrl)
                        .placeholder(R.drawable.image_progress)
                        .error(R.drawable.placeholder_image)
                        .into(moviePosterImageView);
            } else {
                moviePosterImageView.setImageResource(R.drawable.placeholder_image);
            }
        }
    }
}
