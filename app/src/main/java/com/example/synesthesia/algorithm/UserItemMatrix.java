package com.example.synesthesia.algorithm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class UserItemMatrix {
    private final Map<String, Map<String, Integer>> userItemRatings;

    public UserItemMatrix() {
        userItemRatings = new HashMap<>();
    }

    public void addRating(String userId, String itemId, int rating) {
        userItemRatings.putIfAbsent(userId, new HashMap<>());
        Objects.requireNonNull(userItemRatings.get(userId)).put(itemId, rating);
    }

    public Map<String, Integer> getUserRatings(String userId) {
        return userItemRatings.getOrDefault(userId, new HashMap<>());
    }

    public Set<String> getAllUsers() {
        return userItemRatings.keySet();
    }

    public Set<String> getAllItems() {
        Set<String> items = new HashSet<>();
        for (Map<String, Integer> itemRatings : userItemRatings.values()) {
            items.addAll(itemRatings.keySet());
        }
        return items;
    }

    public void filterUsers(Set<String> followedUsers) {
        userItemRatings.keySet().retainAll(followedUsers);
    }
}
