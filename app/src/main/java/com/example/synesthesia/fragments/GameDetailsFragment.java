package com.example.synesthesia.fragments;

import android.os.Bundle;
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
import com.example.synesthesia.models.GiantBombGame;
import com.example.synesthesia.models.Comment;
import com.example.synesthesia.models.Recommendation;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class GameDetailsFragment extends Fragment {

    private GiantBombGame game;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EditText commentField;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialisation des composants UI
        TextView gameTitle = view.findViewById(R.id.gameTitle);
        TextView gameDescription = view.findViewById(R.id.gameDescription);
        ImageView gameImage = view.findViewById(R.id.gameImage);
        Button recommendButton = view.findViewById(R.id.recommendButton);
        Button backButton = view.findViewById(R.id.backButton);
        commentField = view.findViewById(R.id.commentField);

        // Récupération de l'objet jeu
        Bundle args = getArguments();
        if (args != null) {
            game = args.getParcelable("game");
        }

        // Vérification de nullité
        if (game == null) {
            Toast.makeText(getContext(), "Les détails du jeu sont indisponibles.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Affichage des détails du jeu
        gameTitle.setText(game.getName() != null ? game.getName() : "Titre inconnu");
        gameDescription.setText(game.getDescription() != null ? game.getDescription() : "Description indisponible");

        if (game.getImage() != null && game.getImage().getMediumUrl() != null) {
            Glide.with(this)
                    .load(game.getImage().getMediumUrl())
                    .placeholder(R.drawable.rotating_loader)
                    .into(gameImage);
        } else {
            gameImage.setImageResource(R.drawable.placeholder_image);
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recommendButton.setOnClickListener(v -> {
            String comment = commentField.getText().toString().trim();
            submitRecommendation(game, comment.isEmpty() ? "" : comment);

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToMainPage();
            }
        });

        backButton.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void submitRecommendation(GiantBombGame game, String commentText) {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");

                        Comment firstComment = new Comment(userId, commentText, new Timestamp(new Date()));

                        List<Comment> commentsList = new ArrayList<>();
                        commentsList.add(firstComment);

                        Recommendation recommendation = new Recommendation(
                                game.getName(),
                                null,
                                game.getImage() != null ? game.getImage().getMediumUrl() : null,
                                userId,
                                username,
                                commentsList,
                                new Timestamp(new Date()),
                                "game",
                                commentText,
                                game.getId()
                        );

                        db.collection("recommendations")
                                .add(recommendation)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(getContext(), "Recommendation saved successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error saving recommendation", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error fetching user data", Toast.LENGTH_SHORT).show());
    }
}
