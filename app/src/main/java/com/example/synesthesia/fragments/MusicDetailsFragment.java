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
import com.example.synesthesia.api.DeezerApi;
import com.example.synesthesia.models.Comment;
import com.example.synesthesia.models.Recommendation;
import com.example.synesthesia.models.Track;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MusicDetailsFragment extends Fragment {

    private static final String TAG = "MusicDetailsFragment";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EditText commentField;
    private DeezerApi deezerApi;
    private String coverImageUrl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialisation de Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Récupération de l'objet passé par arguments
        Bundle args = getArguments();
        Track track;
        if (args != null) {
            track = args.getParcelable("track");
        } else {
            track = null;
        }

        // Vérification de l'objet track
        if (track == null) {
            Log.e(TAG, "L'objet Track est nul");
            Toast.makeText(getContext(), "Erreur: Aucun morceau sélectionné", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialisation des vues
        ImageView trackImage = view.findViewById(R.id.musicImage);
        TextView musicTitle = view.findViewById(R.id.musicTitle);
        TextView musicArtist = view.findViewById(R.id.musicArtist);
        TextView musicDuration = view.findViewById(R.id.musicDuration);
        commentField = view.findViewById(R.id.commentField);
        Button recommendButton = view.findViewById(R.id.recommendButton);
        Button backButton = view.findViewById(R.id.backButton);

        // Vérification de l'initialisation des vues
        if (trackImage == null || musicTitle == null || musicArtist == null || musicDuration == null || commentField == null || recommendButton == null || backButton == null) {
            Log.e(TAG, "Une ou plusieurs vues sont nulles");
            Toast.makeText(getContext(), "Erreur d'initialisation des vues", Toast.LENGTH_SHORT).show();
            return;
        }

        // Remplissage des données de la musique
        musicTitle.setText(track.getTitle());
        musicArtist.setText(track.getArtist().getName());
        musicDuration.setText(formatDuration(track.getDuration()) + " minutes");

        // Chargement de l'image de couverture
        final String[] coverUrl = {null};
        if (track.getAlbum() != null) {
            coverUrl[0] = track.getAlbum().getCoverXl();
        } else if (track.getArtist() != null) {
            coverUrl[0] = track.getArtist().getImageUrl();
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.deezer.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        deezerApi = retrofit.create(DeezerApi.class);

        String idMusic = track.getId();
        fetchTrackDetails(idMusic, trackImage);

        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        recommendButton.setOnClickListener(v -> {
            String commentText = commentField.getText().toString().trim();
            submitRecommendation(track, commentText.isEmpty() ? "" : commentText);

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToMainPage();
            }
        });
    }

    private void fetchTrackDetails(String idMusic, ImageView trackImage) {
        Call<Track> call = deezerApi.getTrackById(idMusic);
        call.enqueue(new Callback<Track>() {
            @Override
            public void onResponse(@NonNull Call<Track> call, @NonNull Response<Track> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Récupération des détails de la piste directement depuis le corps de la réponse
                    Track trackDetails = response.body();

                    // Chargement de l'image de couverture
                    coverImageUrl = trackDetails.getAlbum().getCoverXl(); // Assigner à coverImageUrl
                    loadCoverImage(coverImageUrl, trackImage);
                } else {
                    // Afficher un message d'erreur si la réponse ne contient pas les détails attendus
                    Log.e(TAG, "Erreur lors de la réponse : " + response.code() + " - " + response.message());
                    Toast.makeText(getContext(), "Erreur lors de la récupération des détails de la musique : " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Track> call, Throwable t) {
                Log.e(TAG, "Échec de la récupération des détails de la musique", t);
                Toast.makeText(getContext(), "Échec de la connexion à l'API", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCoverImage(String coverUrl, ImageView trackImage) {
        if (coverUrl != null && !coverUrl.isEmpty()) {
            Glide.with(this)
                    .load(coverUrl)
                    .placeholder(R.drawable.rotating_loader)
                    .into(trackImage);
        } else {
            Log.w(TAG, "URL de couverture est null ou vide");
            trackImage.setImageResource(R.drawable.placeholder_image);
        }
    }

    private void submitRecommendation(Track track, String commentText) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Utilisateur non authentifié", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");

                        Comment firstComment = new Comment(userId, commentText, new Timestamp(new Date()));

                        List<Comment> commentsList = new ArrayList<>();
                        commentsList.add(firstComment);

                        // Utiliser coverImageUrl récupéré dans fetchTrackDetails
                        String recommendationCoverUrl = coverImageUrl;

                        Log.d(TAG, "URL de couverture de recommandation: " + recommendationCoverUrl);

                        Timestamp recommendationTimestamp = new Timestamp(new Date());
                        String type = "music";

                        Recommendation recommendation = new Recommendation(
                                track.getTitle(),
                                null,
                                recommendationCoverUrl,
                                userId,
                                username,
                                commentsList,
                                recommendationTimestamp,
                                type,
                                commentText,
                                track.getId()
                        );

                        db.collection("recommendations")
                                .add(recommendation)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(getContext(), "Recommandation enregistrée", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Erreur lors de l'enregistrement de la recommandation", e);
                                    Toast.makeText(getContext(), "Erreur lors de l'enregistrement de la recommandation", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.e(TAG, "Utilisateur non trouvé");
                        Toast.makeText(getContext(), "Utilisateur non trouvé", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de la récupération des données de l'utilisateur", e);
                    Toast.makeText(getContext(), "Erreur lors de la récupération des données de l'utilisateur", Toast.LENGTH_SHORT).show();
                });
    }

    @NonNull
    private String formatDuration(int durationInSeconds) {
        int minutes = durationInSeconds / 60;
        int seconds = durationInSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
