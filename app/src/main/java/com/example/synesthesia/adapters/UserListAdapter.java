package com.example.synesthesia.adapters;

import static com.example.synesthesia.utilities.UserUtils.db;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.synesthesia.R;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private List<String> userIdList;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(String userId);
    }

    public UserListAdapter(List<String> userIdList, OnUserClickListener listener) {
        this.userIdList = userIdList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        String userId = userIdList.get(position);
        holder.bind(userId, listener);
    }

    @Override
    public int getItemCount() {
        return userIdList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private ImageView userImageView;
        private TextView userPseudo;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userImageView = itemView.findViewById(R.id.userImageView);
            userPseudo = itemView.findViewById(R.id.userPseudo);
        }

        public void bind(String userId, OnUserClickListener listener) {
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            userPseudo.setText(documentSnapshot.getString("username"));

                            String imageUrl = documentSnapshot.getString("profileImageUrl");
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(itemView.getContext())
                                        .load(imageUrl)
                                        .placeholder(R.drawable.rotating_loader)
                                        .into(userImageView);
                            }
                        }
                    });

            itemView.setOnClickListener(v -> listener.onUserClick(userId));
        }
    }
}