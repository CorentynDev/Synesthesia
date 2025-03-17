package com.example.synesthesia.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.synesthesia.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.adapters.NotificationAdapter;
import com.example.synesthesia.models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class NotifFragment extends Fragment {
    private RecyclerView notifRecyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private TextView titleTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notif, container, false);

        notifRecyclerView = view.findViewById(R.id.notifRecyclerView);
        notifRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        titleTextView = view.findViewById(R.id.titleTextView);
        titleTextView.setText("Notifications");

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        notifRecyclerView.setAdapter(adapter);

        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(15) // Limiter à 30 notifications
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@NonNull QuerySnapshot snapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e("NotifFragment", "Erreur de récupération", e);
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
