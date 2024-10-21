package com.example.synesthesia;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.models.Recommendation;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView userProfileImageView;
    private TextView userPseudoTextView;
    private TextView userEmailTextView;
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
        TextView userPasswordTextView = findViewById(R.id.userPasswordTextView);
        RecyclerView userRecommendationsRecyclerView = findViewById(R.id.userRecommendationsRecyclerView);

        userRecommendationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recommendationAdapter = new RecommendationAdapter(new ArrayList<>());
        userRecommendationsRecyclerView.setAdapter(recommendationAdapter);

        loadUserData();

        userPseudoTextView.setOnClickListener(v -> showEditPseudoDialog());
        userEmailTextView.setOnClickListener(v -> showEditEmailDialog());
        userProfileImageView.setOnClickListener(v -> showEditProfileImageDialog());
        userPasswordTextView.setOnClickListener(v -> showChangePasswordDialog());

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

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        EditText currentPassword = dialogView.findViewById(R.id.currentPassword);
        EditText newPassword = dialogView.findViewById(R.id.newPassword);
        EditText confirmNewPassword = dialogView.findViewById(R.id.confirmNewPassword);
        Button confirmButton = dialogView.findViewById(R.id.confirmPasswordChangeButton);

        AlertDialog dialog = builder.create();

        confirmButton.setOnClickListener(v -> {
            String currentPass = currentPassword.getText().toString();
            String newPass = newPassword.getText().toString();
            String confirmPass = confirmNewPassword.getText().toString();

            if (newPass.equals(confirmPass)) {
                // Appeler la méthode pour changer le mot de passe
                updatePassword(currentPass, newPass);
                dialog.dismiss();
            } else {
                Toast.makeText(UserProfileActivity.this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void updatePassword(String currentPassword, String newPassword) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String email = Objects.requireNonNull(auth.getCurrentUser()).getEmail();

        // Ré-authentifier l'utilisateur avant de changer le mot de passe
        AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(email), currentPassword);

        auth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Ré-authentification réussie, changement de mot de passe
                auth.getCurrentUser().updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(UserProfileActivity.this, "Mot de passe changé avec succès", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(UserProfileActivity.this, "Erreur lors du changement de mot de passe", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(UserProfileActivity.this, "Échec de la ré-authentification. Vérifiez le mot de passe actuel.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}