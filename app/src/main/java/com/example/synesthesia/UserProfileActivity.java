package com.example.synesthesia;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.models.Recommendation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView userProfileImageView;
    private TextView userPseudoTextView, userEmailTextView;
    private RecommendationAdapter recommendationAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        userProfileImageView = findViewById(R.id.userProfileImageView);
        userPseudoTextView = findViewById(R.id.userPseudoTextView);
        userEmailTextView = findViewById(R.id.userEmailTextView);
        RecyclerView userRecommendationsRecyclerView = findViewById(R.id.userRecommendationsRecyclerView);

        userRecommendationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recommendationAdapter = new RecommendationAdapter(new ArrayList<>());
        userRecommendationsRecyclerView.setAdapter(recommendationAdapter);

        loadUserData();

        userPseudoTextView.setOnClickListener(v -> showEditPseudoDialog());
        userEmailTextView.setOnClickListener(v -> showEditEmailDialog());
        userProfileImageView.setOnClickListener(v -> showEditProfileImageDialog());
    }

    private void loadUserData() {
        String userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        String pseudo = documentSnapshot.getString("username");
                        String email = documentSnapshot.getString("email");

                        if (profileImageUrl != null) {
                            Picasso.get().load(profileImageUrl).into(userProfileImageView);
                        }

                        userPseudoTextView.setText(pseudo);
                        userEmailTextView.setText(email);
                    }
                });

        loadUserRecommendations();
    }

    private void loadUserRecommendations() {
        String userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        firestore.collection("recommendations")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recommendation> recommendations = queryDocumentSnapshots.toObjects(Recommendation.class);
                    recommendationAdapter.setRecommendations(recommendations);
                });
    }

    private void showEditPseudoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Username");

        // Configure the input
        final EditText input = new EditText(this);
        input.setText(userPseudoTextView.getText());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newPseudo = input.getText().toString();
            updateUserPseudo(newPseudo);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateUserPseudo(String newPseudo) {
        String userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        firestore.collection("users").document(userId)
                .update("username", newPseudo)
                .addOnSuccessListener(aVoid -> userPseudoTextView.setText(newPseudo))
                .addOnFailureListener(e -> Log.e("UpdateProfile", "Error updating username", e));
    }

    private void showEditEmailDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Email");

        // Configure the input
        final EditText input = new EditText(this);
        input.setText(userEmailTextView.getText());
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newEmail = input.getText().toString();
            updateUserEmail(newEmail);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateUserEmail(String newEmail) {
        String userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        firestore.collection("users").document(userId)
                .update("email", newEmail)
                .addOnSuccessListener(aVoid -> userEmailTextView.setText(newEmail))
                .addOnFailureListener(e -> Log.e("UpdateProfile", "Error updating email", e));
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
            uploadProfileImage(imageUri);
        }
    }

    private void uploadProfileImage(Uri imageUri) {
        if (imageUri != null) {
            String userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
            StorageReference fileReference = FirebaseStorage.getInstance().getReference()
                    .child("profile_images").child(userId + ".jpg");

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl()
                            .addOnSuccessListener(uri -> updateUserProfileImage(uri.toString())))
                    .addOnFailureListener(e -> Log.e("UploadImage", "Error uploading image", e));
        }
    }

    private void updateUserProfileImage(String imageUrl) {
        String userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        firestore.collection("users").document(userId)
                .update("profileImageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> Picasso.get().load(imageUrl).into(userProfileImageView))
                .addOnFailureListener(e -> Log.e("UpdateProfile", "Error updating profile image", e));
    }
}