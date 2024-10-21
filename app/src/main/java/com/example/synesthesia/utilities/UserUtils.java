package com.example.synesthesia.utilities;

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

import androidx.annotation.Nullable;

import com.example.synesthesia.R;
import com.example.synesthesia.RecommendationAdapter;
import com.example.synesthesia.models.Recommendation;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

public class UserUtils {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;

    public UserUtils() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.firebaseStorage = FirebaseStorage.getInstance();
    }

    public String getUserId() {
        return Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
    }

    // Méthode pour charger les données de l'utilisateur
    public void loadUserData(ImageView profileImageView, TextView pseudoTextView, TextView emailTextView) {
        String userId = getUserId();

        firestore.collection("users").document(userId).get()
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

    // Mise à jour du pseudo de l'utilisateur
    public void updateUserPseudo(Context context, String newPseudo, TextView pseudoTextView) {
        String userId = getUserId();

        firestore.collection("users").document(userId)
                .update("username", newPseudo)
                .addOnSuccessListener(aVoid -> pseudoTextView.setText(newPseudo))
                .addOnFailureListener(e -> Log.e("UpdateProfile", "Error updating username", e));
    }

    // Mise à jour de l'email de l'utilisateur
    public void updateUserEmail(Context context, String newEmail, TextView emailTextView) {
        String userId = getUserId();

        firestore.collection("users").document(userId)
                .update("email", newEmail)
                .addOnSuccessListener(aVoid -> emailTextView.setText(newEmail))
                .addOnFailureListener(e -> Log.e("UpdateProfile", "Error updating email", e));
    }

    // Méthode pour uploader une nouvelle image de profil
    public void uploadProfileImage(Context context, Uri imageUri, ImageView profileImageView) {
        if (imageUri != null) {
            String userId = getUserId();
            StorageReference fileReference = firebaseStorage.getReference()
                    .child("profile_images").child(userId + ".jpg");

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl()
                            .addOnSuccessListener(uri -> updateUserProfileImage(context, uri.toString(), profileImageView)))
                    .addOnFailureListener(e -> Log.e("UploadImage", "Error uploading image", e));
        }
    }

    // Mise à jour de l'URL de l'image de profil dans Firestore
    public void updateUserProfileImage(Context context, String imageUrl, ImageView profileImageView) {
        String userId = getUserId();

        firestore.collection("users").document(userId)
                .update("profileImageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> Picasso.get().load(imageUrl).into(profileImageView))
                .addOnFailureListener(e -> Log.e("UpdateProfile", "Error updating profile image", e));
    }

    // Méthode pour charger les recommandations de l'utilisateur
    public void loadUserRecommendations(RecommendationAdapter recommendationAdapter) {
        String userId = getUserId();

        firestore.collection("recommendations")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recommendation> recommendations = queryDocumentSnapshots.toObjects(Recommendation.class);
                    recommendationAdapter.setRecommendations(recommendations);
                });
    }

    // Méthode pour afficher le dialogue de changement de pseudo
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

    // Méthode pour afficher le dialogue de changement d'email
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

    // Méthode pour afficher le dialogue de changement de mot de passe
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

    // Méthode pour changer le mot de passe de l'utilisateur
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
}
