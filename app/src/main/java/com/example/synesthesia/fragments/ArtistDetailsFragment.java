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
import com.example.synesthesia.models.Artist;
import com.example.synesthesia.models.Comment;
import com.example.synesthesia.models.Recommendation;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ArtistDetailsFragment extends Fragment {

    private static final String TAG = "ArtistDetailsFragment";

    private DeezerApi deezerApi;
    private String artistImageUrl;
    private EditText commentField;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artist_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Récupération de l'objet passé par arguments
        Bundle args = getArguments();
        Artist artist;
        if (args != null) {
            artist = args.getParcelable("artist");
        } else {
            artist = null;
        }

        if (artist == null) {
            Log.e(TAG, "L'objet Artist est nul");
            Toast.makeText(getContext(), "Erreur: Aucun artiste sélectionné", Toast.LENGTH_SHORT).show();
            return;
        }

        ImageView artistImage = view.findViewById(R.id.artistImage);
        TextView artistName = view.findViewById(R.id.artistName);
        Button backButton = view.findViewById(R.id.backButton);
        Button recommendButton = view.findViewById(R.id.recommendButton);
        commentField = view.findViewById(R.id.commentField);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.deezer.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        deezerApi = retrofit.create(DeezerApi.class);

        String artistId = artist.getId();
        fetchArtistDetails(artistId, artistImage, artistName);

        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        recommendButton.setOnClickListener(v -> {
            String commentText = commentField.getText().toString().trim();
            submitRecommendation(artist, commentText.isEmpty() ? "" : commentText);

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToMainPage();
            }
        });
    }

    private void fetchArtistDetails(String artistId, ImageView artistImage, TextView artistName) {
        Call<Artist> call = deezerApi.getArtistById(artistId);
        call.enqueue(new Callback<Artist>() {
            @Override
            public void onResponse(@NonNull Call<Artist> call, @NonNull Response<Artist> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Artist artistDetails = response.body();
                    artistImageUrl = artistDetails.getImageUrl();
                    loadArtistImage(artistImageUrl, artistImage);
                    artistName.setText(artistDetails.getName());
                } else {
                    Log.e(TAG, "Erreur lors de la réponse : " + response.code() + " - " + response.message());
                    Toast.makeText(getContext(), "Erreur lors de la récupération des détails de l'artiste : " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Artist> call, Throwable t) {
                Log.e(TAG, "Échec de la récupération des détails de l'artiste", t);
                Toast.makeText(getContext(), "Échec de la connexion à l'API", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadArtistImage(String artistUrl, ImageView artistImage) {
        if (artistUrl != null && !artistUrl.isEmpty()) {
            Glide.with(this)
                    .load(artistUrl)
                    .placeholder(R.drawable.rotating_loader)
                    .into(artistImage);
        } else {
            Log.w(TAG, "URL d'image de l'artiste est null ou vide");
            artistImage.setImageResource(R.drawable.placeholder_image);
        }
    }

    private void submitRecommendation(Artist artist, String commentText) {
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

                        String recommendationCoverUrl = artistImageUrl;

                        Log.d(TAG, "URL de couverture de recommandation: " + recommendationCoverUrl);

                        Timestamp recommendationTimestamp = new Timestamp(new Date());
                        String type = "artist";

                        Recommendation recommendation = new Recommendation(
                                artist.getName(),
                                null,
                                recommendationCoverUrl,
                                userId,
                                username,
                                commentsList,
                                recommendationTimestamp,
                                type,
                                commentText,
                                artist.getId()
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
}
