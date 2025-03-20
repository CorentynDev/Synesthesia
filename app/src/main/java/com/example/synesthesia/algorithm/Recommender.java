package com.example.synesthesia.algorithm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Recommender {

    public List<String> recommendItems(UserItemMatrix matrix, String targetUserId, Set<String> followedUsers, int numRecommendations) {
        CollaborativeFiltering cf = new CollaborativeFiltering();
        Map<String, Double> userSimilarities = cf.calculateUserSimilarity(matrix, targetUserId, followedUsers);

        Map<String, Double> itemScores = new HashMap<>();

        for (String userId : userSimilarities.keySet()) {
            double similarity = userSimilarities.get(userId);
            Map<String, Integer> userRatings = matrix.getUserRatings(userId);

            for (String itemId : userRatings.keySet()) {
                if (!matrix.getUserRatings(targetUserId).containsKey(itemId)) {
                    itemScores.put(itemId, itemScores.getOrDefault(itemId, 0.0) + similarity * userRatings.get(itemId));
                }
            }
        }

        List<String> recommendedItemIds = itemScores.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(numRecommendations)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        Set<String> allItems = matrix.getAllItems();
        List<String> remainingItems = allItems.stream()
                .filter(itemId -> !recommendedItemIds.contains(itemId))
                .collect(Collectors.toList());

        recommendedItemIds.addAll(remainingItems);

        return recommendedItemIds;
    }
}
