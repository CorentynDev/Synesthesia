package com.example.synesthesia.utilities;

import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.synesthesia.R;
import com.example.synesthesia.models.Recommendation;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

public class BookmarkUtils {

    private final FirebaseFirestore db;

    public BookmarkUtils(FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Met à jour la liste des utilisateurs qui ont marqué une recommandation.
     *
     * @param userId          ID de l'utilisateur qui marque ou retire la marque de la recommandation.
     * @param recommendation  Objet Recommendation contenant les informations de la recommandation.
     * @param addMark         Booléen indiquant si l'utilisateur marque ou retire la recommandation.
     */
    public void updateMarkList(String userId, @NonNull Recommendation recommendation, boolean addMark) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> markedBy = (List<String>) documentSnapshot.get("bookmarkedRecommendations");
                        if (markedBy == null) {
                            markedBy = new ArrayList<>();
                        }
                        if (addMark) {
                            if (!markedBy.contains(userId)) {
                                markedBy.add(userId);
                            }
                        } else {
                            markedBy.remove(userId);
                        }
                        // On peut sauvegarder la liste mise à jour dans Firebase ici si besoin
                    }
                });
    }

    /**
     * Met à jour l'interface utilisateur pour afficher l'état de la marque.
     *
     * @param markButton        ImageView représentant le bouton de marque.
     * @param isCurrentlyMarked Booléen indiquant si la recommandation est actuellement marquée.
     */
    public void updateMarkUI(ImageView markButton, boolean isCurrentlyMarked) {
        if (isCurrentlyMarked) {
            markButton.setImageResource(R.drawable.bookmark_active); // Icône active
        } else {
            markButton.setImageResource(R.drawable.bookmark); // Icône non active
        }
    }

    /**
     * Vérifie si un utilisateur a marqué une recommandation.
     *
     * @param userId          ID de l'utilisateur à vérifier.
     * @param recommendation  Objet Recommendation contenant les informations de la recommandation.
     * @return                True si l'utilisateur a marqué la recommandation, sinon false.
     */
    public boolean isMarked(String userId, @NonNull Recommendation recommendation) {
        // Manipulation directe de Firebase ou des structures locales pour vérifier la présence de l'utilisateur
        DocumentReference userRef = db.collection("users").document(userId);
        // On peut récupérer l'information directement depuis la base de données si nécessaire.
        final boolean[] isMarked = {false};

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> markedBy = (List<String>) documentSnapshot.get("bookmarkedRecommendations");
                isMarked[0] = markedBy != null && markedBy.contains(userId);
            }
        }).addOnFailureListener(e -> Log.e("isMarked", "Erreur lors de la récupération des données de l'utilisateur", e));

        return isMarked[0];
    }

    /**
     * Activer ou désactiver la marque d'une recommandation pour un utilisateur spécifique.
     *
     * @param recommendationId  ID de la recommandation à marquer ou démarquer.
     * @param userId            ID de l'utilisateur pour lequel la marque est modifiée.
     * @param isMarked          Booléen indiquant si la recommandation est actuellement marquée.
     * @param onComplete        Runnable à exécuter après la transaction, succès ou non.
     */
    public void toggleMark(String recommendationId, String userId, boolean isMarked, Runnable onComplete) {
        if (recommendationId == null || userId == null) {
            Log.e("ToggleMark", "Recommendation ID ou User ID est nul");
            return;
        }

        DocumentReference userRef = db.collection("users").document(userId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(userRef);
            if (!snapshot.exists()) {
                throw new FirebaseFirestoreException("Le document utilisateur n'existe pas", FirebaseFirestoreException.Code.NOT_FOUND);
            }

            List<String> bookmarkedRecommendations = (List<String>) snapshot.get("bookmarkedRecommendations");
            if (bookmarkedRecommendations == null) {
                bookmarkedRecommendations = new ArrayList<>();
            }

            if (isMarked) {
                if (!bookmarkedRecommendations.contains(recommendationId)) {
                    bookmarkedRecommendations.add(recommendationId);
                }
            } else {
                bookmarkedRecommendations.remove(recommendationId);
            }

            transaction.update(userRef, "bookmarkedRecommendations", bookmarkedRecommendations);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("ToggleMark", "Transaction de marque réussie!");
            onComplete.run();
        }).addOnFailureListener(e -> {
            Log.e("ToggleMark", "Échec de la transaction de marque.", e);
            onComplete.run();
        });
    }
}
