package com.example.synesthesia;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.utilities.FooterUtils;
import com.example.synesthesia.utilities.UserUtils;

import java.util.ArrayList;

public class UserProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView userProfileImageView;
    private TextView userPseudoTextView;
    private TextView userEmailTextView;
    private UserUtils userUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        FooterUtils.setupFooter(this);

        userProfileImageView = findViewById(R.id.userProfileImageView);
        userPseudoTextView = findViewById(R.id.userPseudoTextView);
        userEmailTextView = findViewById(R.id.userEmailTextView);
        TextView userPasswordTextView = findViewById(R.id.userPasswordTextView);
        RecyclerView userRecommendationsRecyclerView = findViewById(R.id.userRecommendationsRecyclerView);

        userRecommendationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecommendationAdapter recommendationAdapter = new RecommendationAdapter(new ArrayList<>());
        userRecommendationsRecyclerView.setAdapter(recommendationAdapter);

        userUtils = new UserUtils();

        userUtils.loadUserData(userProfileImageView, userPseudoTextView, userEmailTextView);

        userPseudoTextView.setOnClickListener(v -> userUtils.showEditPseudoDialog(this, userPseudoTextView));
        userEmailTextView.setOnClickListener(v -> userUtils.showEditEmailDialog(this, userEmailTextView));
        userProfileImageView.setOnClickListener(v -> showEditProfileImageDialog());
        userPasswordTextView.setOnClickListener(v -> userUtils.showChangePasswordDialog(this));
    }

    private void showEditProfileImageDialog() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            userUtils.uploadProfileImage(this, imageUri, userProfileImageView);
        }
    }
}
