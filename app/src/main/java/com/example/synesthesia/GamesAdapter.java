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
import com.example.synesthesia.models.GiantBombGame;

import java.util.List;

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
        private final TextView gameDescriptionTextView;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            gameCoverImageView = itemView.findViewById(R.id.bookThumbnail);
            gameTitleTextView = itemView.findViewById(R.id.bookTitle);
            gameDescriptionTextView = itemView.findViewById(R.id.bookAuthor);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    GiantBombGame selectedGame = games.get(position);
                    Intent intent = new Intent(context, GameDetailsActivity.class);
                    intent.putExtra("game", selectedGame);
                    context.startActivity(intent);
                }
            });
        }

        public void bind(GiantBombGame game) {
            gameTitleTextView.setText(game.getName() != null ? game.getName() : "Titre inconnu");
            gameDescriptionTextView.setText(game.getDescription() != null ? game.getDescription() : "Description indisponible");

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
