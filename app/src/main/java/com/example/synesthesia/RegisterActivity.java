package com.example.synesthesia;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private EditText emailField;
    private EditText usernameField;
    private EditText passwordField;
    private ImageView profileImageView;
    private EditText profileImageUrlEditText;

    private Uri profileImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        emailField = findViewById(R.id.registerEmail);
        usernameField = findViewById(R.id.registerUsername);
        passwordField = findViewById(R.id.registerPassword);
        Button registerButton = findViewById(R.id.registerButton);
        TextView loginRedirect = findViewById(R.id.loginRedirect);
        profileImageView = findViewById(R.id.profileImageView);
        profileImageUrlEditText = findViewById(R.id.profileImageUrl);
        Button pasteImageUrlButton = findViewById(R.id.pasteImageUrlButton);

        profileImageView.setOnClickListener(v -> openImageChooser());

        registerButton.setOnClickListener(v -> {
            String email = emailField.getText().toString();
            String username = usernameField.getText().toString();
            String password = passwordField.getText().toString();

            if (!email.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
                registerUser(email, username, password);
            } else {
                Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        pasteImageUrlButton.setOnClickListener(v -> {
            String imageUrl = profileImageUrlEditText.getText().toString();
            if (!imageUrl.isEmpty()) {
                new DownloadImageTask().execute(imageUrl);
            } else {
                Toast.makeText(RegisterActivity.this, "Please enter an image URL", Toast.LENGTH_SHORT).show();
            }
        });

        loginRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            profileImageUri = data.getData();
            profileImageView.setImageURI(profileImageUri);
        }
    }

    private void registerUser(String email, String username, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            if (profileImageUri != null) {
                                uploadProfileImage(userId, email, username, profileImageUri);
                            } else {
                                saveUserToFirestore(userId, email, username, null);
                            }
                        }
                    } else {
                        Log.w("RegisterActivity", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadProfileImage(String userId, String email, String username, Uri imageUri) {
        if (imageUri == null) {
            saveUserToFirestore(userId, email, username, null);
            return;
        }

        StorageReference storageRef = storage.getReference();
        StorageReference profileImageRef = storageRef.child("profile_images/" + userId + ".jpg");

        profileImageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveUserToFirestore(userId, email, username, imageUrl);
                    }).addOnFailureListener(e -> {
                        Log.e("UploadProfileImage", "Failed to get download URL", e);
                        Toast.makeText(RegisterActivity.this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                        saveUserToFirestore(userId, email, username, null);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("UploadProfileImage", "Image upload failed", e);
                    Toast.makeText(RegisterActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    saveUserToFirestore(userId, email, username, null);
                });
    }

    private void saveUserToFirestore(String userId, String email, String username, @Nullable String profileImageUrl) {
        User newUser = new User(email, username, profileImageUrl);

        db.collection("users").document(userId).set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterActivity.this, "User profile picture registered successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w("RegisterActivity", "Error adding user to Firestore", e);
                    Toast.makeText(RegisterActivity.this, "Failed to register user", Toast.LENGTH_SHORT).show();
                });
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            String imageUrl = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new URL(imageUrl).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("DownloadImageTask", "Error downloading image", e);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                profileImageView.setImageBitmap(result);
                Uri uri = getImageUri(result);
                if (uri != null) {
                    profileImageUri = uri;
                } else {
                    Toast.makeText(RegisterActivity.this, "Failed to convert image", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(RegisterActivity.this, "Failed to download image", Toast.LENGTH_SHORT).show();
            }
        }

        private Uri getImageUri(Bitmap bitmap) {
            try {
                File file = new File(getExternalCacheDir(), "profile_image.jpg");
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.close();
                return Uri.fromFile(file);
            } catch (Exception e) {
                Log.e("GetImageUri", "Error converting bitmap to URI", e);
                return null;
            }
        }
    }

    public static class User {
        public String email;
        public String username;
        public String profileImageUrl;

        public User(String email, String username, @Nullable String profileImageUrl) {
            this.email = email;
            this.username = username;
            this.profileImageUrl = profileImageUrl;
        }
    }
}
