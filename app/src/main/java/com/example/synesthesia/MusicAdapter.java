package com.example.synesthesia;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.synesthesia.models.Artist;
import com.example.synesthesia.models.Album;
import com.example.synesthesia.models.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    private List<Object> items; // Une liste générique pour stocker des artistes, albums, ou pistes
    private Context context;
    private OnItemClickListener listener;

    private int currentlyPlayingPosition = RecyclerView.NO_POSITION;

    // Nouvelle variable pour stocker le ViewHolder en lecture
    private MediaPlayer globalMediaPlayer;


    public MusicAdapter(List<Object> items, Context context) {
        this.items = items != null ? items : new ArrayList<>();
        this.context = context;
    }

    // Méthode pour définir l'écouteur de clics
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Méthode pour mettre à jour les artistes
    public void updateArtists(List<Artist> artists) {
        items.clear();
        items.addAll(artists);
        notifyDataSetChanged();
    }

    // Méthode pour mettre à jour les albums
    public void updateAlbums(List<Album> albums) {
        items.clear();
        items.addAll(albums);
        notifyDataSetChanged();
    }

    // Méthode pour mettre à jour les pistes
    public void updateTracks(List<Track> tracks) {
        items.clear();
        items.addAll(tracks);
        notifyDataSetChanged();
        // Optionnel: vérifiez les données après la mise à jour
        checkTracksData(tracks);
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artiste, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        if (position >= 0 && position < items.size()) {
            Object item = items.get(position);
            if (item instanceof Track) {
                Log.d("MusicAdapter", "Binding track at position " + position);
                Track track = (Track) item;
                holder.bindTrack(track);

                // Définir l’image du bouton lecture/pause en fonction de la position actuellement en lecture
                if (position == currentlyPlayingPosition) {
                    holder.playPauseButton.setImageResource(R.drawable.pause);
                } else {
                    holder.playPauseButton.setImageResource(R.drawable.bouton_de_lecture);
                }

                holder.playPauseButton.setOnClickListener(v -> togglePlayPause(holder, position, track));

                // Ouvrir MusicDetailsActivity au clic sur le morceau
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(context, MusicDetailsActivity.class);
                    intent.putExtra("track", track); // Assurez-vous que Track implémente Serializable ou Parcelable
                    context.startActivity(intent);
                });
            } else if (item instanceof Artist) {
                holder.bindArtist((Artist) item);
                holder.playPauseButton.setVisibility(View.GONE); // Masquer le bouton lecture pour les artistes
            } else if (item instanceof Album) {
                holder.bindAlbum((Album) item);
                holder.playPauseButton.setVisibility(View.GONE); // Masquer le bouton lecture pour les albums
            } else {
                holder.playPauseButton.setVisibility(View.GONE); // Masquer le bouton lecture pour les types inconnus
            }
        } else {
            Log.e("MusicAdapter", "Index hors limites : " + position);
        }
    }

    private void togglePlayPause(MusicViewHolder holder, int position, Track track) {
        if (position == currentlyPlayingPosition) {
            // Si c'est la même piste, mettre en pause ou reprendre
            if (globalMediaPlayer != null) {
                if (globalMediaPlayer.isPlaying()) {
                    globalMediaPlayer.pause();
                    holder.playPauseButton.setImageResource(R.drawable.bouton_de_lecture);
                } else {
                    globalMediaPlayer.start();
                    holder.playPauseButton.setImageResource(R.drawable.pause);
                }
            }
        } else {
            // Démarrer une nouvelle piste
            if (globalMediaPlayer != null) {
                // Si une autre piste est en cours, arrêtez-la
                if (globalMediaPlayer.isPlaying()) {
                    globalMediaPlayer.stop();
                    notifyItemChanged(currentlyPlayingPosition); // Réinitialise l'ancienne piste
                }
                globalMediaPlayer.reset();
            } else {
                globalMediaPlayer = new MediaPlayer();
            }

            try {
                globalMediaPlayer.setDataSource(context, Uri.parse(track.getPreviewUrl()));
                globalMediaPlayer.setOnPreparedListener(mp -> {
                    mp.start();
                    holder.playPauseButton.setImageResource(R.drawable.pause);
                    if (currentlyPlayingPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(currentlyPlayingPosition);
                    }
                    currentlyPlayingPosition = position; // Met à jour la position en cours
                    notifyItemChanged(position); // Met à jour l'icône de la nouvelle piste
                });

                globalMediaPlayer.setOnCompletionListener(mp -> {
                    holder.playPauseButton.setImageResource(R.drawable.bouton_de_lecture);
                    currentlyPlayingPosition = RecyclerView.NO_POSITION; // Réinitialise la position
                    notifyItemChanged(position); // Met à jour l'icône après la fin
                });

                globalMediaPlayer.prepareAsync();
            } catch (IOException e) {
                Log.e("MusicAdapter", "Erreur lors de la lecture de la prévisualisation", e);
            }
        }
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public class MusicViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView titleTextView;
        private TextView artistTextView;
        private ImageButton playPauseButton;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.musicImageView);
            titleTextView = itemView.findViewById(R.id.musicTitleTextView);
            artistTextView = itemView.findViewById(R.id.musicArtistTextView);
            playPauseButton = itemView.findViewById(R.id.playPauseButton);
        }


        public void bindArtist(Artist artist) {
            titleTextView.setText(artist.getName());
            artistTextView.setText(""); // Les artistes n'ont peut-être pas besoin de sous-titre

            if (artist.getImageUrl() != null) {
                Glide.with(context)
                        .load(artist.getImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.placeholder_image);
            }
        }

        public void bindAlbum(Album album) {
            titleTextView.setText(album.getTitle());

            if (album.getArtist() != null) {
                artistTextView.setText(album.getArtist().getName());
            } else {
                artistTextView.setText("Unknown Artist");
            }

            if (album.getCoverXl() != null) {
                Glide.with(context)
                        .load(album.getCoverXl())
                        .placeholder(R.drawable.placeholder_image)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.placeholder_image);
            }
        }

        public void bindTrack(Track track) {
            titleTextView.setText(track.getTitle());
            artistTextView.setText(track.getArtist().getName());

            if (track.getAlbum() != null) {
                Glide.with(context)
                        .load(track.getAlbum().getCoverXl())
                        .placeholder(R.drawable.placeholder_image)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.placeholder_image);
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Object item); // Gestion générique des clics
    }

    public void checkTracksData(List<Track> tracks) {
        for (Track track : tracks) {
            Log.d("MusicAdapter", "Track: " + track.getTitle() + ", Preview URL: " + track.getPreviewUrl());
        }
    }
    public void resetPlayer() {
        if (globalMediaPlayer != null) {
            globalMediaPlayer.stop();
            globalMediaPlayer.release();
            globalMediaPlayer = null;
        }
        currentlyPlayingPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged(); // Met à jour tous les éléments pour réinitialiser les icônes
    }

}

