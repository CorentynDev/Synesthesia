package com.example.synesthesia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText emailField;
    private EditText usernameField;
    private EditText passwordField;
    private Button registerButton;
    private TextView loginRedirect; // TextView pour rediriger vers LoginActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialisation de Firebase Auth et Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Références des champs
        emailField = findViewById(R.id.registerEmail);
        usernameField = findViewById(R.id.registerUsername);
        passwordField = findViewById(R.id.registerPassword);
        registerButton = findViewById(R.id.registerButton);
        loginRedirect = findViewById(R.id.loginRedirect); // Initialiser le TextView de redirection

        // Lors du clic sur le bouton d'inscription
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

        // Lors du clic sur le texte de redirection vers l'écran de connexion
        loginRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    // Méthode pour inscrire un utilisateur
    private void registerUser(String email, String username, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Inscription réussie, ajouter l'utilisateur à Firestore
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            // Créer un objet utilisateur avec l'email et le pseudonyme
                            User newUser = new User(email, username);

                            // Ajouter l'utilisateur à la collection "users" dans Firestore
                            db.collection("users").document(userId).set(newUser)
                                    .addOnSuccessListener(aVoid -> {
                                        // Rediriger vers MainActivity après l'inscription
                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Log.w("RegisterActivity", "Error adding user to Firestore", e));
                        }
                    } else {
                        // Si l'inscription échoue
                        Log.w("RegisterActivity", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Classe utilisateur pour stocker les données dans Firestore
    public class User {
        public String email;
        public String username;

        public User(String email, String username) {
            this.email = email;
            this.username = username;
        }
    }
}
