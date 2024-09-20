package com.example.synesthesia;

import android.content.Context;
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
    private List<Track> tracks;
    private boolean isPlaying = false; // État du lecteur (lecture ou pause)

    private MusicViewHolder currentlyPlayingViewHolder;
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
        if (position >= 0 && position < items.size()) { // Vérifiez que l'index est valide
            Object item = items.get(position);
            if (item instanceof Track) {
                Log.d("MusicAdapter", "Binding track at position " + position);
                holder.bindTrack((Track) item);
            } else if (item instanceof Artist) {
                holder.bindArtist((Artist) item);
            } else if (item instanceof Album) {
                holder.bindAlbum((Album) item);
            }
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });

            // Modifier pour gérer le clic sur le bouton de lecture/pause
            //holder.playPauseButton.setOnClickListener(v -> togglePlayPause(holder, (Track) item));
        } else {
            Log.e("MusicAdapter", "Index out of bounds: " + position);
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
        private boolean isPlaying = false;

        private MediaPlayer mediaPlayer;
        private String previewUrl; // URL de la prévisualisation

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.musicImageView);
            titleTextView = itemView.findViewById(R.id.musicTitleTextView);
            artistTextView = itemView.findViewById(R.id.musicArtistTextView);
            playPauseButton = itemView.findViewById(R.id.playPauseButton);

            // Vérifiez si playPauseButton est nul
            if (playPauseButton == null) {
                throw new NullPointerException("ImageButton with ID playPauseButton is null. Check your layout file.");
            }

            playPauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //togglePlayPause();
                }
            });
        }

        private void togglePlayPause(MusicViewHolder holder, Track track) {
            if (holder.isPlaying) {
                holder.playPauseButton.setImageResource(R.drawable.bouton_de_lecture);
                holder.stopPlayback();
            } else {
                holder.playPauseButton.setImageResource(R.drawable.pause);
                // Arrêtez la lecture précédente
                if (currentlyPlayingViewHolder != null && currentlyPlayingViewHolder != holder) {
                    currentlyPlayingViewHolder.stopPlayback();
                    currentlyPlayingViewHolder.playPauseButton.setImageResource(R.drawable.bouton_de_lecture);
                }
                // Démarrer la nouvelle lecture
                holder.startPlayback(track);
                currentlyPlayingViewHolder = holder;
            }
            holder.isPlaying = !holder.isPlaying;
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

            if (album.getCoverUrl() != null) {
                Glide.with(context)
                        .load(album.getCoverUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.placeholder_image);
            }
        }

        public void bindTrack(Track track) {
            titleTextView.setText(track.getTitle());
            artistTextView.setText(track.getArtistName().getName());

            if (track.getAlbum() != null) {
                Glide.with(context)
                        .load(track.getAlbum().getCoverUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.placeholder_image);
            }

            // Assurez-vous que vous définissez correctement l'URL de prévisualisation
            previewUrl = track.getPreviewUrl();
            Log.d("MusicAdapter", "Prévisualisation URL pour la piste " + track.getTitle() + ": " + previewUrl);

            if (previewUrl == null || previewUrl.isEmpty()) {
                Log.w("MusicAdapter", "URL de prévisualisation pour la piste " + track.getTitle() + " est nulle ou vide.");
            }
        }

        public void startPlayback(Track track) {
            if (globalMediaPlayer != null && globalMediaPlayer.isPlaying()) {
                globalMediaPlayer.stop();
                globalMediaPlayer.release();
            }

            globalMediaPlayer = new MediaPlayer();
            previewUrl = track.getPreviewUrl();

            Log.d("MusicAdapter", "Démarrage de la lecture avec l'URL: " + previewUrl);

            if (previewUrl == null || previewUrl.isEmpty()) {
                Log.e("MusicAdapter", "URL de prévisualisation invalide ou nulle.");
                return;
            }

            try {
                globalMediaPlayer.setDataSource(context, Uri.parse(previewUrl));
                globalMediaPlayer.setOnPreparedListener(mp -> {
                    mp.start();
                    Log.d("MusicAdapter", "Lecture démarrée");
                });
                globalMediaPlayer.prepareAsync();
            } catch (IOException e) {
                Log.e("MusicAdapter", "Erreur lors de la lecture de la prévisualisation", e);
            }
        }

        public void stopPlayback() {
            if (globalMediaPlayer != null) {
                globalMediaPlayer.stop();
                globalMediaPlayer.release();
                globalMediaPlayer = null;
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

}
