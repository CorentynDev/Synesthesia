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
import com.example.synesthesia.models.Game;
import java.util.List;

public class GamesAdapter extends RecyclerView.Adapter<GamesAdapter.GameViewHolder> {

    private List<Game> games;
    private Context context;

    public GamesAdapter(List<Game> games, Context context) {
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
        Game game = games.get(position);
        holder.bind(game);
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    class GameViewHolder extends RecyclerView.ViewHolder {

        private ImageView gameCoverImageView;
        private TextView gameTitleTextView;
        private TextView gameReleasedDateTextView;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            //gameCoverImageView = itemView.findViewById(R.id.gameCover);
            gameTitleTextView = itemView.findViewById(R.id.gameTitle);
            //gameReleasedDateTextView = itemView.findViewById(R.id.gameReleasedDate);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Game selectedGame = games.get(position);
                    //Intent intent = new Intent(context, GameDetailsActivity.class);
                    //intent.putExtra("game", selectedGame);
                    //context.startActivity(intent);
                }
            });
        }

        public void bind(Game game) {
            gameTitleTextView.setText(game.getName());
            gameReleasedDateTextView.setText(game.getReleased() != null ? game.getReleased() : "Inconnu");

            if (game.getCover() != null) {
                Glide.with(gameCoverImageView.getContext())
                        .load(game.getCover().getUrl())
                        .placeholder(R.color.gray_medium)
                        .error(R.color.red)
                        .into(gameCoverImageView);
            }
        }
    }
}
