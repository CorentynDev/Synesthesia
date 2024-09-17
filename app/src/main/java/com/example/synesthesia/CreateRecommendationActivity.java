package com.example.synesthesia;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class CreateRecommendationActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView recommendationImage;
    private EditText recommendationTitleField;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recommendation);

        recommendationImage = findViewById(R.id.recommendationImage);
        recommendationTitleField = findViewById(R.id.recommendationTitleField);
        Button selectImageButton = findViewById(R.id.selectImageButton);
        Button saveRecommendationButton = findViewById(R.id.saveRecommendationButton);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Bouton pour choisir une image
        selectImageButton.setOnClickListener(v -> openImageSelector());

        // Bouton pour sauvegarder la recommandation
        saveRecommendationButton.setOnClickListener(v -> saveRecommendation());
    }

    // Méthode pour ouvrir le sélecteur d'images
    private void openImageSelector() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                recommendationImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Méthode pour sauvegarder la recommandation
    private void saveRecommendation() {
        String title = recommendationTitleField.getText().toString().trim();
        if (title.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Please enter a title and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Générer une date au format dd/MM/yyyy
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        // Uploader l'image sur Firebase Storage
        uploadImageToFirebaseStorage(title, date);
    }

    // Méthode pour uploader l'image sur Firebase Storage
    private void uploadImageToFirebaseStorage(String title, String date) {
        String imageId = UUID.randomUUID().toString();  // Génère un ID unique pour l'image
        StorageReference imageRef = storage.getReference("recommendation_images/" + imageId);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Sauvegarder la recommandation avec l'URL de l'image
                    saveRecommendationToFirestore(title, date, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    Log.e("CreateRecommendation", "Error uploading image", e);
                    Toast.makeText(CreateRecommendationActivity.this, "Error uploading image", Toast.LENGTH_SHORT).show();
                });
    }

    // Méthode pour sauvegarder la recommandation dans Firestore
    private void saveRecommendationToFirestore(String title, String date, String imageUrl) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Recommendation recommendation = new Recommendation(title, date, imageUrl, userId);

        db.collection("recommendations").add(recommendation)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(CreateRecommendationActivity.this, "Recommendation created", Toast.LENGTH_SHORT).show();
                    finish();  // Fermer l'activité après la sauvegarde
                })
                .addOnFailureListener(e -> {
                    Log.e("CreateRecommendation", "Error saving recommendation", e);
                    Toast.makeText(CreateRecommendationActivity.this, "Error saving recommendation", Toast.LENGTH_SHORT).show();
                });
    }

    // Classe modèle pour la recommandation
    public static class Recommendation {
        private String title;
        private String date;
        private String imageUrl;
        private String userId;

        public Recommendation(String title, String date, String imageUrl, String userId) {
            this.title = title;
            this.date = date;
            this.imageUrl = imageUrl;
            this.userId = userId;
        }

        // Getters and setters pour Firestore
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
}
