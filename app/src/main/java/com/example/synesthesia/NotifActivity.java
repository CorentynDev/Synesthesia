package com.example.synesthesia;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class NotifActivity extends AppCompatActivity {
    private RecyclerView notifRecyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notif);

        notifRecyclerView = findViewById(R.id.notifRecyclerView);
        notifRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        notifRecyclerView.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).collection("notifications").orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@NonNull QuerySnapshot snapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("NotifActivity", "Erreur de récupération", e);
                    return;
                }

                notificationList.clear();
                for (DocumentSnapshot doc : snapshots) {
                    Notification notification = doc.toObject(Notification.class);
                    notificationList.add(notification);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}