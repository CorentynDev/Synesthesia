package com.example.synesthesia.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.synesthesia.MainActivity;
import com.example.synesthesia.R;
import com.example.synesthesia.models.GiantBombGame;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GamesAdapter extends RecyclerView.Adapter<GamesAdapter.GameViewHolder> {

    private final List<GiantBombGame> games;
    private final Context context;

    public GamesAdapter(List<GiantBombGame> games, Context context) {
        this.games = games;
        this.context = context;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        GiantBombGame game = games.get(position);
        holder.bind(game);
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    class GameViewHolder extends RecyclerView.ViewHolder {

        private final ImageView gameCoverImageView;
        private final TextView gameTitleTextView;
        private final TextView gameDeveloperTextView;
        private final TextView gameReleaseDateTextView;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            gameCoverImageView = itemView.findViewById(R.id.bookThumbnail);
            gameTitleTextView = itemView.findViewById(R.id.bookTitle);
            gameDeveloperTextView = itemView.findViewById(R.id.bookAuthor);
            gameReleaseDateTextView = itemView.findViewById(R.id.bookDate);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    GiantBombGame selectedGame = games.get(position);
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).showGameDetailsFragment(selectedGame);
                    }
                }
            });
        }

        public void bind(GiantBombGame game) {
            gameTitleTextView.setText(game.getName() != null ? game.getName() : "Titre inconnu");

            // Affichage des développeurs
            if (game.getDevelopers() != null && !game.getDevelopers().isEmpty()) {
                gameDeveloperTextView.setText(TextUtils.join(", ", game.getDevelopers()));
            } else {
                gameDeveloperTextView.setText("Développeur inconnu");
            }

            // Format de la date de sortie
            if (game.getOriginalReleaseDate() != null) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date date = dateFormat.parse(game.getOriginalReleaseDate());
                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                    gameReleaseDateTextView.setText(displayFormat.format(date));
                } catch (Exception e) {
                    gameReleaseDateTextView.setText("Date inconnue");
                }
            } else {
                gameReleaseDateTextView.setText("Date inconnue");
            }

            // Chargement de l'image avec Glide
            if (game.getImage() != null && game.getImage().getMediumUrl() != null) {
                Glide.with(gameCoverImageView.getContext())
                        .load(game.getImage().getMediumUrl())
                        .placeholder(R.drawable.image_progress)
                        .error(R.drawable.placeholder_image)
                        .into(gameCoverImageView);
            } else {
                gameCoverImageView.setImageResource(R.drawable.placeholder_image);
            }
        }
    }
}
