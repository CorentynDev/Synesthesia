package com.example.synesthesia.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.synesthesia.api.TmdbApiClient;
import com.example.synesthesia.api.TmdbApiService;
import com.example.synesthesia.models.Comment;
import com.example.synesthesia.models.CreditsResponse;
import com.example.synesthesia.models.Genre;
import com.example.synesthesia.models.GenreResponse;
import com.example.synesthesia.models.Recommendation;
import com.example.synesthesia.models.TmdbMovie;
import com.example.synesthesia.models.Video;
import com.example.synesthesia.models.VideoResponse;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailsFragment extends Fragment {

    private TmdbMovie movie;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EditText commentField;
    private final Map<Integer, String> genreMap = new HashMap<>();
    private TextView directorTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Récupération du film depuis les arguments
        Bundle args = getArguments();
        if (args != null) {
            movie = args.getParcelable("movie");
        }

        if (movie == null) {
            Toast.makeText(getContext(), "Erreur : Film non disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialisation des vues
        TextView movieTitle = view.findViewById(R.id.movieTitle);
        TextView movieOverview = view.findViewById(R.id.movieOverview);
        TextView movieGenres = view.findViewById(R.id.movieGenres);
        TextView actorsList = view.findViewById(R.id.actorsList);
        ImageView moviePoster = view.findViewById(R.id.moviePoster);
        Button trailerButton = view.findViewById(R.id.trailerButton);
        Button recommendButton = view.findViewById(R.id.recommendButton);
        Button backButton = view.findViewById(R.id.backButton);
        commentField = view.findViewById(R.id.commentField);
        TextView movieDate = view.findViewById(R.id.movieDate);
        directorTextView = view.findViewById(R.id.movieDirector);

        movieTitle.setText(movie.getTitle());
        movieOverview.setText(movie.getOverview());

        fetchGenres(movie.getGenreIds());
        fetchActors(movie.getId());
        fetchDirector(movie.getId());

        String formattedDate = formatDate(movie.getReleaseDate());
        movieDate.setText("Date de sortie : " + formattedDate);

        String actors = movie.getActors() != null ? String.join(", ", movie.getActors()) : "Acteurs non disponibles";
        actorsList.setText("Acteurs principaux : " + actors);

        Glide.with(this)
                .load("https://image.tmdb.org/t/p/w500" + movie.getPosterPath())
                .placeholder(R.drawable.rotating_loader)
                .into(moviePoster);

        fetchTrailerUrl(movie.getId());

        trailerButton.setOnClickListener(v -> {
            String trailerUrl = movie.getTrailerUrl();
            if (trailerUrl != null && !trailerUrl.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl));
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Bande-annonce non disponible", Toast.LENGTH_SHORT).show();
            }
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recommendButton.setOnClickListener(v -> {
            String comment = commentField.getText().toString().trim();
            submitRecommendation(movie, comment.isEmpty() ? "" : comment);
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToMainPage();
            }
        });

        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        setupOverviewExpandable();
    }

    private void setupOverviewExpandable() {
        TextView movieOverview = requireView().findViewById(R.id.movieOverview);
        TextView seeMoreButton = requireView().findViewById(R.id.seeMoreButton);

        movieOverview.post(() -> {
            if (movieOverview.getLineCount() > 4) {
                seeMoreButton.setVisibility(View.VISIBLE);

                movieOverview.setMaxLines(4);
                movieOverview.setEllipsize(TextUtils.TruncateAt.END);

                seeMoreButton.setText("Voir plus");
                seeMoreButton.setOnClickListener(v -> {
                    movieOverview.setMaxLines(Integer.MAX_VALUE);
                    movieOverview.setEllipsize(null);
                    seeMoreButton.setVisibility(View.VISIBLE);
                    seeMoreButton.setText("Voir moins");
                });
            } else {
                seeMoreButton.setVisibility(View.GONE);
            }

            seeMoreButton.setOnClickListener(v -> {
                if ("Voir moins".equals(seeMoreButton.getText().toString())) {
                    movieOverview.setMaxLines(4);
                    movieOverview.setEllipsize(TextUtils.TruncateAt.END);
                    seeMoreButton.setText("Voir plus");
                } else {
                    movieOverview.setMaxLines(Integer.MAX_VALUE);
                    movieOverview.setEllipsize(null);
                    seeMoreButton.setText("Voir moins");
                }
            });
        });
    }

    private void fetchTrailerUrl(String movieId) {
        TmdbApiService apiService = TmdbApiClient.getRetrofitInstance().create(TmdbApiService.class);
        apiService.getMovieVideos(movieId, "f07ebbaf992b26f432b9ba90fa71ea8d", "fr").enqueue(new Callback<VideoResponse>() {
            @Override
            public void onResponse(@NonNull Call<VideoResponse> call, @NonNull Response<VideoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Video video : response.body().getResults()) {
                        if ("YouTube".equalsIgnoreCase(video.getSite()) && "Trailer".equalsIgnoreCase(video.getType())) {
                            movie.setTrailerUrl("https://www.youtube.com/watch?v=" + video.getKey());
                            return;
                        }
                    }
                }
                movie.setTrailerUrl(null);
            }

            @Override
            public void onFailure(@NonNull Call<VideoResponse> call, @NonNull Throwable t) {
                movie.setTrailerUrl(null);
            }
        });
    }

    private void fetchGenres(List<Integer> genreIds) {
        TmdbApiService apiService = TmdbApiClient.getRetrofitInstance().create(TmdbApiService.class);

        apiService.getGenres("f07ebbaf992b26f432b9ba90fa71ea8d", "fr").enqueue(new Callback<GenreResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenreResponse> call, @NonNull Response<GenreResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Genre> fetchedGenres = response.body().getGenres();

                    // Construire une carte des genres avec leurs IDs
                    for (Genre genre : fetchedGenres) {
                        genreMap.put(genre.getId(), genre.getName());
                    }

                    // Associer les noms des genres à partir des IDs
                    List<String> genreNames = new ArrayList<>();
                    for (Integer genreId : genreIds) {
                        if (genreMap.containsKey(genreId)) {
                            genreNames.add(genreMap.get(genreId));
                        }
                    }

                    movie.setGenres(genreNames);

                    // Mettre à jour l'interface utilisateur
                    TextView genresTextView = requireView().findViewById(R.id.movieGenres);
                    genresTextView.setText("Genres : " + String.join(", ", genreNames));
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenreResponse> call, @NonNull Throwable t) {
                TextView genresTextView = requireView().findViewById(R.id.movieGenres);
                genresTextView.setText("Genres : non disponibles");
            }
        });
    }

    private void fetchActors(String movieId) {
        TmdbApiService apiService = TmdbApiClient.getRetrofitInstance().create(TmdbApiService.class);
        apiService.getMovieCredits(movieId, "f07ebbaf992b26f432b9ba90fa71ea8d", "fr").enqueue(new Callback<CreditsResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreditsResponse> call, @NonNull Response<CreditsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> actorNames = new ArrayList<>();
                    for (CreditsResponse.Cast cast : response.body().getCast()) {
                        actorNames.add(cast.getName());
                        if (actorNames.size() == 5) break;
                    }
                    movie.setActors(actorNames);
                    updateActorsView(actorNames);
                } else {
                    updateActorsView(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreditsResponse> call, @NonNull Throwable t) {
                updateActorsView(null);
            }
        });
    }

    private void updateActorsView(List<String> actorNames) {
        TextView actorsList = requireView().findViewById(R.id.actorsList);
        if (actorNames != null && !actorNames.isEmpty()) {
            actorsList.setText("Acteurs principaux : " + String.join(", ", actorNames));
        } else {
            actorsList.setText("Acteurs principaux : non disponibles");
        }
    }

    public static String formatDate(String originalDate) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("d MMMM yyyy", Locale.FRENCH);

        try {
            Date date = inputFormat.parse(originalDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return originalDate;
        }
    }

    private void submitRecommendation(TmdbMovie movie, String commentText) {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");

                        Comment firstComment = new Comment(userId, commentText, new Timestamp(new Date()));

                        List<Comment> commentsList = new ArrayList<>();
                        commentsList.add(firstComment);

                        Recommendation recommendation = new Recommendation(
                                movie.getTitle(),
                                null,
                                movie.getPosterPath() != null ? "https://image.tmdb.org/t/p/w500" + movie.getPosterPath() : null,
                                userId,
                                username,
                                commentsList,
                                new Timestamp(new Date()),
                                "movie",
                                commentText,
                                movie.getId()
                        );

                        db.collection("recommendations")
                                .add(recommendation)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(getContext(), "Recommandation enregistrée avec succès", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(getContext(), "Utilisateur introuvable", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Erreur de connexion à la base de données", Toast.LENGTH_SHORT).show());
    }

    private void fetchDirector(String movieId) {
        TmdbApiService apiService = TmdbApiClient.getRetrofitInstance().create(TmdbApiService.class);
        String apiKey = "f07ebbaf992b26f432b9ba90fa71ea8d";

        apiService.getMovieCredits(movieId, apiKey, "fr-FR").enqueue(new Callback<CreditsResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreditsResponse> call, @NonNull Response<CreditsResponse> response) {
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
                    directorTextView.post(() -> directorTextView.setText("Réalisé par " + finalDirectorName));
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
