package com.example.synesthesia;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<String> pseudoList;
    private Map<String, String> userMap;
    private Context context;

    public UserAdapter(Context context, List<String> pseudoList, Map<String, String> userMap) {
        this.context = context;
        this.pseudoList = pseudoList;
        this.userMap = userMap;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        String pseudo = pseudoList.get(position);
        holder.userPseudo.setText(pseudo);

        // Récupérer l'URL associée au pseudo
        String profileImageUrl = userMap.get(pseudo);

        // Charger l'image de profil avec Glide
        if (profileImageUrl != null) {
            Glide.with(context)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.placeholder_image) // Image par défaut
                    .into(holder.userImage);
        } else {
            holder.userImage.setImageResource(R.drawable.placeholder_image); // Si l'URL est introuvable
        }
    }

    @Override
    public int getItemCount() {
        return pseudoList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userPseudo;
        ImageView userImage;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userPseudo = itemView.findViewById(R.id.userPseudo);
            userImage = itemView.findViewById(R.id.musicImageView);
        }
    }
}


