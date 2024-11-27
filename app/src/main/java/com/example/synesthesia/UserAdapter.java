package com.example.synesthesia;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.synesthesia.models.User;

import java.util.List;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> userList;  // Liste d'objets User
    private Context context;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position); // Récupère l'objet User

        holder.userPseudo.setText(user.getPseudo()); // Affiche le pseudo
        // Charge l'image de profil avec Glide
        String profileImageUrl = user.getProfileImageUrl();
        if (profileImageUrl != null) {
            Glide.with(context)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.userImage);
        } else {
            holder.userImage.setImageResource(R.drawable.placeholder_image);
        }

        // Ajout du OnClickListener pour rediriger vers la page de profil
        holder.itemView.setOnClickListener(v -> {
            Log.d("UserAdapter", "User ID: " + user.getId());
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("userId", user.getId());  // Passe l'ID de l'utilisateur à l'activité de profil
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
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