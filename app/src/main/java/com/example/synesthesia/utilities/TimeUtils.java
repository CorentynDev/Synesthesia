package com.example.synesthesia.utilities;

import android.annotation.SuppressLint;

import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;

public class TimeUtils {

    /**
     * Return a string that represents the time since the post (comments or recommendation).
     *
     * @param timestamp  Timestamp from which the past time is calculated.
     * @return           A formatted string that indicates the time since the post has been done.
     */
    @SuppressLint("SimpleDateFormat")
    public static String getTimeAgo(Timestamp timestamp) {
        long time = timestamp.toDate().getTime();
        long now = System.currentTimeMillis();

        if (time > now || time <= 0) {
            return "À l'instant";
        }

        final long diff = now - time;

        long seconds = diff / 1000;
        long minutes = diff / (60 * 1000);
        long hours = diff / (60 * 60 * 1000);
        long days = diff / (24 * 60 * 60 * 1000);
        long months = days / 30;
        long years = days / 365;

        if (seconds < 60) {
            return seconds == 1 ? "Il y a 1 seconde" : "Il y a " + seconds + " secondes";
        } else if (minutes < 2) {
            return "Il y a une minute";
        } else if (minutes < 50) {
            return "Il y a " + minutes + " minutes";
        } else if (hours < 2) {
            return "Il y a une heure";
        } else if (hours < 24) {
            return "Il y a " + hours + " heures";
        } else if (days < 30) {
            return days == 1 ? "Hier" : "Il y a " + days + " jours";
        } else if (months < 12) {
            return months == 1 ? "Il y a 1 mois" : "Il y a " + months + " mois";
        } else if (years < 5) {
            return years == 1 ? "Il y a 1 an" : "Il y a " + years + " ans";
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MM yyyy");
            return "Posté le " + dateFormat.format(timestamp);
        }
    }
}
