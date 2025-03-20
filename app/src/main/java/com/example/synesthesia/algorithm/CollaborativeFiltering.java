package com.example.synesthesia.algorithm;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CollaborativeFiltering {

    public Map<String, Double> calculateUserSimilarity(@NonNull UserItemMatrix matrix, String targetUserId, @NonNull Set<String> followedUsers) {
        Map<String, Double> similarities = new HashMap<>();
        Map<String, Integer> targetUserRatings = matrix.getUserRatings(targetUserId);

        for (String userId : followedUsers) {
            if (!userId.equals(targetUserId)) {
                double similarity = cosineSimilarity(targetUserRatings, matrix.getUserRatings(userId));
                similarities.put(userId, similarity);
            }
        }

        return similarities;
    }

    private double cosineSimilarity(@NonNull Map<String, Integer> ratings1, Map<String, Integer> ratings2) {
        double dotProduct = 0;
        double norm1 = 0;
        double norm2 = 0;

        for (String itemId : ratings1.keySet()) {
            if (ratings2.containsKey(itemId)) {
                dotProduct += ratings1.get(itemId) * ratings2.get(itemId);
            }
        }

        for (int rating : ratings1.values()) {
            norm1 += Math.pow(rating, 2);
        }

        for (int rating : ratings2.values()) {
            norm2 += Math.pow(rating, 2);
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
