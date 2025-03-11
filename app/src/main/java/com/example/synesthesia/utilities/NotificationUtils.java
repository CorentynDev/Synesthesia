package com.example.synesthesia.utilities;

import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.AccessToken;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationUtils {
    private static final String TAG = "FCM";
    private static final String FCM_ENDPOINT = "https://fcm.googleapis.com/v1/projects/synesthesia-2/messages:send";
    private static final String SERVICE_ACCOUNT_KEY_PATH = "./android_asset/service-account.json"; // Chemin du fichier JSON

    private static AccessToken accessToken;

    // Récupère le token OAuth 2.0
    private static String getAccessToken(Context context) throws IOException {
        if (accessToken == null || accessToken.getExpirationTime().before(new Date())) {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(context.getAssets().open("service-account.json"))
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
            credentials.refreshIfExpired();
            accessToken = credentials.getAccessToken();
        }
        return accessToken.getTokenValue();
    }

    public static void sendNotificationFollow(Context context, String token, String title, String message) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                JSONObject json = new JSONObject();
                JSONObject notification = new JSONObject();
                notification.put("title", title);
                notification.put("body", message);

                JSONObject messageJson = new JSONObject();
                messageJson.put("token", token);
                messageJson.put("notification", notification);

                json.put("message", messageJson);

                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        json.toString()
                );

                // 🔥 Passer le contexte pour obtenir le token
                String tokenAuth = getAccessToken(context);

                Request request = new Request.Builder()
                        .url(FCM_ENDPOINT)
                        .post(body)
                        .addHeader("Authorization", "Bearer " + tokenAuth)
                        .addHeader("Content-Type", "application/json")
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body() != null ? response.body().string() : "Aucune réponse";

                Log.d(TAG, "Code réponse : " + response.code());
                Log.d(TAG, "Message réponse : " + response.message());
                Log.d(TAG, "Corps réponse : " + responseBody);

                if (response.isSuccessful()) {
                    Log.d(TAG, "Notification envoyée avec succès !");
                } else {
                    Log.e(TAG, "Erreur lors de l'envoi de la notification : " + responseBody);
                }

            } catch (Exception e) {
                Log.e(TAG, "Erreur lors de l'envoi de la notification", e);
                e.printStackTrace();
            }
        }).start();
    }
    public static void sendNotificationLike(Context context, String token, String title, String message) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                JSONObject json = new JSONObject();
                JSONObject notification = new JSONObject();
                notification.put("title", title);
                notification.put("body", message);

                JSONObject messageJson = new JSONObject();
                messageJson.put("token", token);
                messageJson.put("notification", notification);

                json.put("message", messageJson);

                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        json.toString()
                );

                // Utilisation du contexte passé en paramètre pour obtenir le token
                String tokenAuth = getAccessToken(context);

                Request request = new Request.Builder()
                        .url(FCM_ENDPOINT)
                        .post(body)
                        .addHeader("Authorization", "Bearer " + tokenAuth)
                        .addHeader("Content-Type", "application/json")
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body() != null ? response.body().string() : "Aucune réponse";

                Log.d(TAG, "Code réponse : " + response.code());
                Log.d(TAG, "Message réponse : " + response.message());
                Log.d(TAG, "Corps réponse : " + responseBody);

                if (response.isSuccessful()) {
                    Log.d(TAG, "Notification envoyée avec succès !");
                } else {
                    Log.e(TAG, "Erreur lors de l'envoi de la notification : " + responseBody);
                }

            } catch (Exception e) {
                Log.e(TAG, "Erreur lors de l'envoi de la notification", e);
                e.printStackTrace();
            }
        }).start();
    }
}