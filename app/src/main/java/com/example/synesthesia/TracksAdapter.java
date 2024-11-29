package com.example.synesthesia;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.models.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TracksAdapter extends RecyclerView.Adapter<TracksAdapter.TrackViewHolder> {

    private List<Track> tracks;
    private Context context;
    private int currentlyPlayingPosition = RecyclerView.NO_POSITION;
    private MediaPlayer mediaPlayer;

    public TracksAdapter() {
        this.tracks = new ArrayList<>();
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks != null ? tracks : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_track, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track track = tracks.get(position);
        holder.trackTitleTextView.setText(track.getTitle());
        holder.trackDurationTextView.setText(formatDuration(track.getDuration()));

        // Définir l'image du bouton lecture/pause
        if (position == currentlyPlayingPosition && mediaPlayer != null && mediaPlayer.isPlaying()) {
            holder.playPauseButton.setImageResource(R.drawable.pause);
        } else {
            holder.playPauseButton.setImageResource(R.drawable.bouton_de_lecture);
        }

        holder.playPauseButton.setOnClickListener(v -> togglePlayPause(holder, position, track));

        // Optionnel: clic sur la piste pour afficher plus de détails ou jouer la prévisualisation
        holder.itemView.setOnClickListener(v -> {
            // Par exemple, démarrer une activité de détails de piste
            Intent intent = new Intent(context, MusicDetailsActivity.class);
            intent.putExtra("track", track);
            context.startActivity(intent);
        });
    }

    private void togglePlayPause(TrackViewHolder holder, int position, Track track) {
        if (position == currentlyPlayingPosition) {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    holder.playPauseButton.setImageResource(R.drawable.bouton_de_lecture);
                } else {
                    mediaPlayer.start();
                    holder.playPauseButton.setImageResource(R.drawable.pause);
                }
            }
        } else {
            if (mediaPlayer != null) {
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
                notifyItemChanged(currentlyPlayingPosition);
            }

            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(context, Uri.parse(track.getPreviewUrl()));
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(mp -> {
                    mp.start();
                    holder.playPauseButton.setImageResource(R.drawable.pause);
                    if (currentlyPlayingPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(currentlyPlayingPosition);
                    }
                    currentlyPlayingPosition = position;
                    notifyItemChanged(position);
                });
                mediaPlayer.setOnCompletionListener(mp -> {
                    holder.playPauseButton.setImageResource(R.drawable.bouton_de_lecture);
                    currentlyPlayingPosition = RecyclerView.NO_POSITION;
                    notifyItemChanged(position);
                });
            } catch (IOException e) {
                Log.e("TracksAdapter", "Erreur lors de la lecture de la prévisualisation", e);
                Toast.makeText(context, "Erreur lors de la lecture de la prévisualisation", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String formatDuration(int durationInSeconds) {
        int minutes = durationInSeconds / 60;
        int seconds = durationInSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    public class TrackViewHolder extends RecyclerView.ViewHolder {
        TextView trackTitleTextView;
        TextView trackDurationTextView;
        ImageButton playPauseButton;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            trackTitleTextView = itemView.findViewById(R.id.trackTitleTextView);
            trackDurationTextView = itemView.findViewById(R.id.trackDurationTextView);
            playPauseButton = itemView.findViewById(R.id.trackPlayPauseButton);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void resetPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        currentlyPlayingPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }
}
