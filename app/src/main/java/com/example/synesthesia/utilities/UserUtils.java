package com.example.synesthesia.utilities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.synesthesia.LoginActivity;
import com.example.synesthesia.MainActivity;
import com.example.synesthesia.R;
import com.example.synesthesia.RecommendationAdapter;
import com.example.synesthesia.models.Recommendation;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

public class UserUtils {

    private final FirebaseAuth firebaseAuth;
    private static FirebaseFirestore db;
    private final FirebaseStorage firebaseStorage;

    public UserUtils() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.firebaseStorage = FirebaseStorage.getInstance();
    }

    public String getCurrentUserId() {
        return Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
    }

    public void loadUserData(ImageView profileImageView, TextView pseudoTextView, TextView emailTextView) {
        String userId = getCurrentUserId();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        String pseudo = documentSnapshot.getString("username");
                        String email = documentSnapshot.getString("email");

                        if (profileImageUrl != null) {
                            Picasso.get().load(profileImageUrl).into(profileImageView);
                        }

                        pseudoTextView.setText(pseudo);
                        emailTextView.setText(email);
                    }
                });
    }

    public void updateUserPseudo(Context context, String newPseudo, TextView pseudoTextView) {
        String userId = getCurrentUserId();

        db.collection("users").document(userId)
                .update("username", newPseudo)
                .addOnSuccessListener(aVoid -> pseudoTextView.setText(newPseudo))
                .addOnFailureListener(e -> Log.e("UpdateProfile", "Error updating username", e));
    }

    public void updateUserEmail(Context context, String newEmail, TextView emailTextView) {
        String userId = getCurrentUserId();

        db.collection("users").document(userId)
                .update("email", newEmail)
                .addOnSuccessListener(aVoid -> emailTextView.setText(newEmail))
                .addOnFailureListener(e -> Log.e("UpdateProfile", "Error updating email", e));
    }

    public void uploadProfileImage(Context context, Uri imageUri, ImageView profileImageView) {
        if (imageUri != null) {
            String userId = getCurrentUserId();
            StorageReference fileReference = firebaseStorage.getReference()
                    .child("profile_images").child(userId + ".jpg");

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl()
                            .addOnSuccessListener(uri -> updateUserProfileImage(context, uri.toString(), profileImageView)))
                    .addOnFailureListener(e -> Log.e("UploadImage", "Error uploading image", e));
        }
    }

    public void updateUserProfileImage(Context context, String imageUrl, ImageView profileImageView) {
        String userId = getCurrentUserId();

        db.collection("users").document(userId)
                .update("profileImageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> Picasso.get().load(imageUrl).into(profileImageView))
                .addOnFailureListener(e -> Log.e("UpdateProfile", "Error updating profile image", e));
    }

    public void loadUserRecommendations(RecommendationAdapter recommendationAdapter) {
        String userId = getCurrentUserId();

        db.collection("recommendations")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recommendation> recommendations = queryDocumentSnapshots.toObjects(Recommendation.class);
                    recommendationAdapter.setRecommendations(recommendations);
                });
    }

    public void showEditPseudoDialog(Context context, TextView pseudoTextView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Username");

        final EditText input = new EditText(context);
        input.setText(pseudoTextView.getText());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newPseudo = input.getText().toString();
            updateUserPseudo(context, newPseudo, pseudoTextView);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void showEditEmailDialog(Context context, TextView emailTextView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Email");

        final EditText input = new EditText(context);
        input.setText(emailTextView.getText());
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newEmail = input.getText().toString();
            updateUserEmail(context, newEmail, emailTextView);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void showChangePasswordDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
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
                updatePassword(context, currentPass, newPass);
                dialog.dismiss();
            } else {
                Toast.makeText(context, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    public void updatePassword(Context context, String currentPassword, String newPassword) {
        String email = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getEmail();
        AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(email), currentPassword);

        firebaseAuth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                firebaseAuth.getCurrentUser().updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(context, "Mot de passe changé avec succès", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Erreur lors du changement de mot de passe", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(context, "Échec de la ré-authentification. Vérifiez le mot de passe actuel.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Récupère et affiche le profil utilisateur (image et résumé) dans les vues données.
     */
    public void getUserProfile(ImageView profileImageView, TextView profileSummary) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            profileSummary.setText(documentSnapshot.getString("username"));
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(profileImageView.getContext())
                                        .load(profileImageUrl)
                                        .into(profileImageView);
                            } else {
                                profileImageView.setImageResource(R.drawable.placeholder_image);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("UserProfile", "Erreur lors de la récupération des données utilisateur", e);
                    });
        }
    }

    /**
     * Vérifie si un utilisateur est connecté à Firebase.
     *
     * @return true si l'utilisateur est connecté, false sinon.
     */
    public boolean isUserLoggedIn() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null;
    }

    /**
     * Charge le profil de l'utilisateur et met à jour les TextView et ImageView correspondants.
     *
     * @param context le contexte de l'application
     * @param userId l'ID de l'utilisateur
     * @param userTextView le TextView pour afficher le nom d'utilisateur
     * @param profileImageView l'ImageView pour afficher l'image de profil
     */
    @SuppressLint("SetTextI18n")
    public static void loadUserProfile(Context context, String userId, TextView userTextView, ImageView profileImageView) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                        userTextView.setText(username);
                        ImagesUtils.loadImage(context, profileImageUrl, profileImageView);
                    }
                })
                .addOnFailureListener(e -> {
                    userTextView.setText("Utilisateur inconnu");
                    profileImageView.setImageResource(R.drawable.placeholder_image);
                });
    }
}
