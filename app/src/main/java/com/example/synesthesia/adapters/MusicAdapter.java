package com.example.synesthesia.adapters;

import android.annotation.SuppressLint;
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
import com.example.synesthesia.MainActivity;
import com.example.synesthesia.R;
import com.example.synesthesia.models.Artist;
import com.example.synesthesia.models.Album;
import com.example.synesthesia.models.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    private final List<Object> items;
    private final Context context;

    private int currentlyPlayingPosition = RecyclerView.NO_POSITION;

    private MediaPlayer globalMediaPlayer;
    private OnItemClickListener onItemClickListener;


    public MusicAdapter(List<Object> items, Context context) {
        this.items = items != null ? items : new ArrayList<>();
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void  updateArtists(List<Artist> artists) {
        items.clear();
        items.addAll(artists);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateAlbums(List<Album> albums) {
        items.clear();
        items.addAll(albums);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateTracks(List<Track> tracks) {
        items.clear();
        items.addAll(tracks);
        notifyDataSetChanged();
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

                if (position == currentlyPlayingPosition) {
                    holder.playPauseButton.setImageResource(R.drawable.pause);
                } else {
                    holder.playPauseButton.setImageResource(R.drawable.bouton_de_lecture);
                }

                holder.playPauseButton.setOnClickListener(v -> togglePlayPause(holder, position, track));

                holder.itemView.setOnClickListener(v -> {
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).showMusicDetailsFragment(track);
                    }
                });
            } else if (item instanceof Artist) {
                Artist artist = (Artist) item;
                holder.bindArtist(artist);
                holder.itemView.setOnClickListener(v -> {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(artist);
                    }
                });
                holder.playPauseButton.setVisibility(View.GONE);
            } else if (item instanceof Album) {
                // Code pour les albums
                Album album = (Album) item;
                holder.bindAlbum(album);
                holder.playPauseButton.setVisibility(View.GONE);

                holder.itemView.setOnClickListener(v -> {
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).showAlbumDetailsFragment(album);
                    }
                });
            } else {
                holder.playPauseButton.setVisibility(View.GONE);
            }
        } else {
            Log.e("MusicAdapter", "Index hors limites : " + position);
        }
    }

    private void togglePlayPause(MusicViewHolder holder, int position, Track track) {
        if (position == currentlyPlayingPosition) {
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
            if (globalMediaPlayer != null) {
                if (globalMediaPlayer.isPlaying()) {
                    globalMediaPlayer.stop();
                    notifyItemChanged(currentlyPlayingPosition);
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
                    currentlyPlayingPosition = position;
                    notifyItemChanged(position);
                });

                globalMediaPlayer.setOnCompletionListener(mp -> {
                    holder.playPauseButton.setImageResource(R.drawable.bouton_de_lecture);
                    currentlyPlayingPosition = RecyclerView.NO_POSITION;
                    notifyItemChanged(position);
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
        private final ImageView imageView;
        private final TextView titleTextView;
        private final TextView artistTextView;
        private final ImageButton playPauseButton;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.musicImageView);
            titleTextView = itemView.findViewById(R.id.musicTitleTextView);
            artistTextView = itemView.findViewById(R.id.musicArtistTextView);
            playPauseButton = itemView.findViewById(R.id.playPauseButton);
        }


        public void bindArtist(Artist artist) {
            titleTextView.setText(artist.getName());
            artistTextView.setText("");

            if (artist.getImageUrl() != null) {
                Glide.with(context)
                        .load(artist.getImageUrl())
                        .placeholder(R.drawable.rotating_loader)
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
                        .placeholder(R.drawable.rotating_loader)
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
                        .placeholder(R.drawable.rotating_loader)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.placeholder_image);
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Object item);
    }

    public void checkTracksData(List<Track> tracks) {
        for (Track track : tracks) {
            Log.d("MusicAdapter", "Track: " + track.getTitle() + ", Preview URL: " + track.getPreviewUrl());
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    public void resetPlayer() {
        if (globalMediaPlayer != null) {
            globalMediaPlayer.stop();
            globalMediaPlayer.release();
            globalMediaPlayer = null;
        }
        currentlyPlayingPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

}

