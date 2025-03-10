package com.example.synesthesia.adapters;

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
import com.example.synesthesia.MovieDetailsActivity;
import com.example.synesthesia.R;
import com.example.synesthesia.api.TmdbApiClient;
import com.example.synesthesia.api.TmdbApiService;
import com.example.synesthesia.models.CreditsResponse;
import com.example.synesthesia.models.TmdbMovie;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;

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
            // Affichage du réalisateur
            fetchDirector(movie.getId(), movieOverviewTextView);

            // Affichage de la date formatée
            TextView movieDateTextView = itemView.findViewById(R.id.bookDate);
            movieDateTextView.setText(movie.getReleaseDate() != null ? formatDate(movie.getReleaseDate()) : "Date inconnue");

            // Affichage de l'image
            if (movie.getPosterPath() != null) {
                String posterUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();
                Glide.with(moviePosterImageView.getContext())
                        .load(posterUrl)
                        .placeholder(R.drawable.rotating_loader)
                        .error(R.drawable.placeholder_image)
                        .into(moviePosterImageView);
            } else {
                moviePosterImageView.setImageResource(R.drawable.placeholder_image);
            }
        }
    }

    public static String formatDate(String originalDate) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        SimpleDateFormat outputFormat = new SimpleDateFormat("d MMMM yyyy", Locale.FRENCH);

        try {
            Date date = inputFormat.parse(originalDate);

            assert date != null;
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return originalDate;
        }
    }

    private void fetchDirector(String movieId, TextView directorTextView) {
        TmdbApiService apiService = TmdbApiClient.getRetrofitInstance().create(TmdbApiService.class);
        String apiKey = "f07ebbaf992b26f432b9ba90fa71ea8d";

        apiService.getMovieCredits(movieId, apiKey, "fr-FR").enqueue(new retrofit2.Callback<CreditsResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreditsResponse> call, @NonNull retrofit2.Response<CreditsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CreditsResponse.Crew> crewList = response.body().getCrew();
                    String directorName = "Réalisateur inconnu";

                    for (CreditsResponse.Crew crew : crewList) {
                        if ("Director".equals(crew.getJob())) {
                            directorName = crew.getName();
                            break;
                        }
                    }

                    String finalDirectorName = directorName;
                    directorTextView.post(() -> directorTextView.setText("Réalisé par : " + finalDirectorName));
                } else {
                    directorTextView.post(() -> directorTextView.setText("Réalisateur inconnu"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreditsResponse> call, @NonNull Throwable t) {
                directorTextView.post(() -> directorTextView.setText("Erreur lors du chargement"));
            }
        });
    }
}
