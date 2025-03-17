package com.example.synesthesia.utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.synesthesia.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class UserUtils {

    private final FirebaseAuth firebaseAuth;
    @SuppressLint("StaticFieldLeak")
    public static FirebaseFirestore db;
    private final FirebaseStorage firebaseStorage;

    public UserUtils() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        this.firebaseStorage = FirebaseStorage.getInstance();
    }

    /**
     * Get the current connected user ID.
     *
     * @return User ID of the connected user as a String.
     * @throws NullPointerException If no user is currently connected.
     */
    public static String getCurrentUserId() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return null;
    }

    /**
     * Download a profile picture to Firebase Storage and update the profile picture on the user interface.
     *
     * @param context             Context in which the method is called.
     * @param imageUri            Image URI to download.
     * @param profileImageView    ImageView where to display the new profile picture after the download.
     */
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

    /**
     * Update the image URL of the current connected user in Firestore, and display the new profile picture on the user interface.
     *
     * @param context              Context in which the method is called.
     * @param imageUrl             The new image URL of the user profile picture.
     * @param profileImageView     ImageView where to display the new user profile picture after the update.
     */
    public void updateUserProfileImage(Context context, String imageUrl, ImageView profileImageView) {
        String userId = getCurrentUserId();

        db.collection("users").document(userId)
                .update("profileImageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> Picasso.get().load(imageUrl).into(profileImageView))
                .addOnFailureListener(e -> Log.e("UpdateProfile", "Error updating profile image", e));
    }

    /**
     * Check if a user is connected to Firebase.
     *
     * @return True if the user is connected, else false.
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
                        Log.d("Firestore", "Document utilisateur trouvé: " + documentSnapshot.getData());

                        userTextView.setText(username);
                        ImagesUtils.loadImage(context, profileImageUrl, profileImageView);
                    }else {
                        Log.e("Firestore", "Aucun document trouvé pour l'utilisateur: " + userId);
                    }
                })
                .addOnFailureListener(e -> {
                    userTextView.setText("Utilisateur inconnu");
                    profileImageView.setImageResource(R.drawable.placeholder_image);
                });
    }

    /**
     * Charge une image de profil à partir d'une URL et l'affiche dans un ImageView.
     *
     * @param context        Le contexte de l'application.
     * @param imageUrl       L'URL de l'image à charger.
     * @param imageView      L'ImageView où afficher l'image.
     */
    public static void loadImageFromUrl(Context context, String imageUrl, ImageView imageView) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context).load(imageUrl).placeholder(R.drawable.rotating_loader).into(imageView);
        } else {
            imageView.setImageResource(R.drawable.placeholder_image);
        }
    }

    public void saveUserToken(String userId, String token) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d("Firestore", "Tentative de sauvegarde du token pour l'utilisateur: " + userId);

        db.collection("users").document(userId).update("fcmToken", token)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Token sauvegardé avec succès dans Firestore"))
                .addOnFailureListener(e -> Log.e("Firestore", "Erreur lors de la sauvegarde du token", e));
    }

    public static Task<String> getPseudo() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return Tasks.forException(new Exception("Utilisateur non connecté"));
        }

        return db.collection("users").document(userId).get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().getString("username");
                    } else {
                        throw Objects.requireNonNull(task.getException());
                    }
                });
    }
}
