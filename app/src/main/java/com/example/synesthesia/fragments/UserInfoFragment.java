package com.example.synesthesia.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.synesthesia.MainActivity;
import com.example.synesthesia.R;
import com.example.synesthesia.utilities.UserUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class UserInfoFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editPseudo, editEmail, editPassword;
    private ImageView userProfileImageView;
    private Button savePseudoButton, saveEmailButton, savePasswordButton, changeProfileImageButton, backButton;
    private UserUtils userUtils;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editPseudo = view.findViewById(R.id.editPseudo);
        editEmail = view.findViewById(R.id.editEmail);
        editPassword = view.findViewById(R.id.editPassword);
        userProfileImageView = view.findViewById(R.id.userProfileImageView);
        savePseudoButton = view.findViewById(R.id.savePseudoButton);
        saveEmailButton = view.findViewById(R.id.saveEmailButton);
        savePasswordButton = view.findViewById(R.id.savePasswordButton);
        changeProfileImageButton = view.findViewById(R.id.changeProfileImageButton);
        backButton = view.findViewById(R.id.backButton);

        userUtils = new UserUtils();

        // Charger les données utilisateur actuelles
        loadUserInfo();

        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

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
                        UserUtils.loadImageFromUrl(getContext(), profileImageUrl, userProfileImageView);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Erreur lors du chargement des informations utilisateur.", Toast.LENGTH_SHORT).show());
    }

    private void navigateToMainPage() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onBackPressed();
        }
    }

    private void savePseudo() {
        String newPseudo = editPseudo.getText().toString().trim();
        String userId = userUtils.getCurrentUserId();

        if (newPseudo.isEmpty()) {
            Toast.makeText(getContext(), "Le pseudo ne peut pas être vide.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .update("username", newPseudo)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Pseudo mis à jour.", Toast.LENGTH_SHORT).show();
                    navigateToMainPage();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Erreur lors de la mise à jour du pseudo.", Toast.LENGTH_SHORT).show());
    }

    private void saveEmail() {
        String newEmail = editEmail.getText().toString().trim();

        if (newEmail.isEmpty()) {
            Toast.makeText(getContext(), "L'email ne peut pas être vide.", Toast.LENGTH_SHORT).show();
            return;
        }

        Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).updateEmail(newEmail)
                .addOnSuccessListener(aVoid -> {
                    String userId = userUtils.getCurrentUserId();
                    FirebaseFirestore.getInstance().collection("users").document(userId)
                            .update("email", newEmail)
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(getContext(), "Email mis à jour.", Toast.LENGTH_SHORT).show();
                                navigateToMainPage(); // Redirige après le succès
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Erreur lors de la mise à jour de l'email.", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Erreur lors de la mise à jour de l'email.", Toast.LENGTH_SHORT).show());
    }

    private void savePassword() {
        String newPassword = editPassword.getText().toString().trim();

        if (newPassword.isEmpty()) {
            Toast.makeText(getContext(), "Le mot de passe ne peut pas être vide.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance().getCurrentUser().updatePassword(newPassword)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Mot de passe mis à jour.", Toast.LENGTH_SHORT).show();
                    navigateToMainPage(); // Redirige après le succès
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Erreur lors de la mise à jour du mot de passe.", Toast.LENGTH_SHORT).show());
    }

    private void chooseProfileImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            userUtils.uploadProfileImage(getContext(), imageUri, userProfileImageView);
        }
    }
}