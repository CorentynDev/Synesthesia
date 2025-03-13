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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.synesthesia.MainActivity;
import com.example.synesthesia.R;
import com.example.synesthesia.adapters.TracksAdapter;
import com.example.synesthesia.api.DeezerApi;
import com.example.synesthesia.models.Album;
import com.example.synesthesia.models.Comment;
import com.example.synesthesia.models.Recommendation;
import com.example.synesthesia.models.Track;
import com.example.synesthesia.models.TrackResponse;
import com.example.synesthesia.utilities.FooterUtils;
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

public class AlbumDetailsFragment extends Fragment {

    private static final String TAG = "AlbumDetailsFragment";

    private ImageView albumCoverImageView;
    private TextView albumTitleTextView;
    private TextView albumArtistTextView;
    private TextView albumTracksCountTextView;
    private RecyclerView tracksRecyclerView;
    private EditText commentField;
    private Button recommendButton;
    private Button backButton;
    private Album album;
    private TracksAdapter tracksAdapter;
    private DeezerApi deezerApi;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_album_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FooterUtils.setupFooter(getActivity(), R.id.createRecommendationButton);

        // Initialize FirebaseAuth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialiser les vues
        albumCoverImageView = view.findViewById(R.id.albumCoverImageView);
        albumTitleTextView = view.findViewById(R.id.albumTitleTextView);
        albumArtistTextView = view.findViewById(R.id.albumArtistTextView);
        albumTracksCountTextView = view.findViewById(R.id.albumTracksCountTextView);
        tracksRecyclerView = view.findViewById(R.id.tracksRecyclerView);
        backButton = view.findViewById(R.id.backButton);
        commentField = view.findViewById(R.id.commentField);
        recommendButton = view.findViewById(R.id.recommendButton);

        // Récupérer l'objet Album depuis les arguments
        Bundle args = getArguments();
        if (args != null) {
            album = args.getParcelable("album");
        }

        if (album == null) {
            Log.e(TAG, "Album object is null");
            Toast.makeText(getContext(), "Erreur: Aucun album sélectionné", Toast.LENGTH_SHORT).show();
            return;
        }

        // Afficher les détails de l'album
        displayAlbumDetails();

        // Configurer Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.deezer.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        deezerApi = retrofit.create(DeezerApi.class);

        // Configurer le RecyclerView pour les pistes
        tracksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tracksAdapter = new TracksAdapter();
        tracksRecyclerView.setAdapter(tracksAdapter);

        // Charger les pistes de l'album
        loadAlbumTracks();

        // Configurer le bouton de retour
        backButton.setOnClickListener(v -> getActivity().onBackPressed());

        recommendButton.setOnClickListener(v -> {
            String commentText = commentField.getText().toString().trim();
            submitRecommendation(album, commentText.isEmpty() ? "" : commentText);

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToMainPage();
            }
        });
    }

    private void submitRecommendation(Album album, String commentText) {
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

                        // Récupérer l'URL de couverture de l'album
                        String recommendationCoverUrl = album.getCoverXl(); // Utiliser la couverture XL de l'album

                        Log.d(TAG, "Recommendation Cover URL: " + recommendationCoverUrl);

                        Timestamp recommendationTimestamp = new Timestamp(new Date());
                        String type = "album"; // Changer le type à "album"

                        String userNote = commentField.getText().toString().trim();

                        // Créer l'objet Recommendation pour l'album
                        Recommendation recommendation = new Recommendation(
                                album.getTitle(), // Utiliser le titre de l'album
                                null, // Laisser null si aucune description supplémentaire n'est nécessaire
                                recommendationCoverUrl,
                                userId,
                                username,
                                commentsList,
                                recommendationTimestamp,
                                type,
                                userNote,
                                album.getId()
                        );

                        // Enregistrer la recommandation dans Firestore
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

    private void displayAlbumDetails() {
        albumTitleTextView.setText(album.getTitle());
        albumArtistTextView.setText(album.getArtist() != null ? album.getArtist().getName() : "Artiste inconnu");
        albumTracksCountTextView.setText(album.getNbTracks() + " pistes");

        String coverUrl = album.getCoverXl(); // Assurez-vous d'utiliser la bonne méthode pour obtenir l'URL
        if (coverUrl != null && !coverUrl.isEmpty()) {
            Glide.with(this)
                    .load(coverUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(albumCoverImageView);
        } else {
            albumCoverImageView.setImageResource(R.drawable.placeholder_image);
        }
    }

    private void loadAlbumTracks() {
        String tracklistUrl = album.getTracklist(); // Assurez-vous que le modèle Album a une méthode getTracklist()

        if (tracklistUrl == null || tracklistUrl.isEmpty()) {
            Log.e(TAG, "Tracklist URL est null ou vide");
            Toast.makeText(getContext(), "Aucune piste disponible pour cet album", Toast.LENGTH_SHORT).show();
            return;
        }

        // Extraire l'endpoint relatif pour Retrofit (enlever le domaine)
        String relativeUrl = tracklistUrl.replace("https://api.deezer.com/", "");

        Call<TrackResponse> call = deezerApi.getAlbumTracks(relativeUrl);
        call.enqueue(new Callback<TrackResponse>() {
            @Override
            public void onResponse(@NonNull Call<TrackResponse> call, @NonNull Response<TrackResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Track> tracks = response.body().getData();
                    tracksAdapter.setTracks(tracks);
                } else {
                    Log.e(TAG, "Réponse non réussie lors du chargement des pistes: " + response.code());
                    Toast.makeText(getContext(), "Erreur lors du chargement des pistes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TrackResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Échec du chargement des pistes", t);
                Toast.makeText(getContext(), "Erreur réseau lors du chargement des pistes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (tracksAdapter != null) {
            tracksAdapter.resetPlayer(); // Arrêtez le lecteur audio
        }
    }
}
