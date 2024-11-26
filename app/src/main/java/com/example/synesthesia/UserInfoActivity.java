package com.example.synesthesia;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.synesthesia.utilities.UserUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserInfoActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editPseudo, editEmail, editPassword;
    private ImageView userProfileImageView;
    private Button savePseudoButton, saveEmailButton, savePasswordButton, changeProfileImageButton;
    private UserUtils userUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        editPseudo = findViewById(R.id.editPseudo);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        userProfileImageView = findViewById(R.id.userProfileImageView);
        savePseudoButton = findViewById(R.id.savePseudoButton);
        saveEmailButton = findViewById(R.id.saveEmailButton);
        savePasswordButton = findViewById(R.id.savePasswordButton);
        changeProfileImageButton = findViewById(R.id.changeProfileImageButton);

        userUtils = new UserUtils();

        // Charger les données utilisateur actuelles
        loadUserInfo();

        // Bouton pour sauvegarder le pseudo
        savePseudoButton.setOnClickListener(v -> savePseudo());

        // Bouton pour sauvegarder l'e-mail
        saveEmailButton.setOnClickListener(v -> saveEmail());

        // Bouton pour changer le mot de passe
        savePasswordButton.setOnClickListener(v -> savePassword());

        // Bouton pour changer l'image de profil
        changeProfileImageButton.setOnClickListener(v -> chooseProfileImage());
    }

    private void loadUserInfo() {
        String currentUserId = userUtils.getCurrentUserId();

        UserUtils.db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        editPseudo.setText(documentSnapshot.getString("username"));
                        editEmail.setText(documentSnapshot.getString("email"));
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        UserUtils.loadImageFromUrl(this, profileImageUrl, userProfileImageView);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur lors du chargement des informations utilisateur.", Toast.LENGTH_SHORT).show());
    }

    private void navigateToMainPage() {
        Intent intent = new Intent(UserInfoActivity.this, MainActivity.class); // Assurez-vous que MainActivity est votre page principale
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Ferme l'activité actuelle pour éviter un retour accidentel
    }

    private void savePseudo() {
        String newPseudo = editPseudo.getText().toString().trim();
        String userId = userUtils.getCurrentUserId();

        if (newPseudo.isEmpty()) {
            Toast.makeText(this, "Le pseudo ne peut pas être vide.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .update("username", newPseudo)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Pseudo mis à jour.", Toast.LENGTH_SHORT).show();
                    navigateToMainPage(); // Redirige après le succès
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur lors de la mise à jour du pseudo.", Toast.LENGTH_SHORT).show());
    }

    private void saveEmail() {
        String newEmail = editEmail.getText().toString().trim();

        if (newEmail.isEmpty()) {
            Toast.makeText(this, "L'email ne peut pas être vide.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance().getCurrentUser().updateEmail(newEmail)
                .addOnSuccessListener(aVoid -> {
                    String userId = userUtils.getCurrentUserId();
                    FirebaseFirestore.getInstance().collection("users").document(userId)
                            .update("email", newEmail)
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(this, "Email mis à jour.", Toast.LENGTH_SHORT).show();
                                navigateToMainPage(); // Redirige après le succès
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Erreur lors de la mise à jour de l'email.", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur lors de la mise à jour de l'email.", Toast.LENGTH_SHORT).show());
    }

    private void savePassword() {
        String newPassword = editPassword.getText().toString().trim();

        if (newPassword.isEmpty()) {
            Toast.makeText(this, "Le mot de passe ne peut pas être vide.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance().getCurrentUser().updatePassword(newPassword)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Mot de passe mis à jour.", Toast.LENGTH_SHORT).show();
                    navigateToMainPage(); // Redirige après le succès
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur lors de la mise à jour du mot de passe.", Toast.LENGTH_SHORT).show());
    }


    private void chooseProfileImage() {
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
