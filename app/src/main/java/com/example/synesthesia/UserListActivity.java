package com.example.synesthesia;

import static com.example.synesthesia.utilities.UserUtils.db;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.synesthesia.adapters.UserListAdapter;
import com.example.synesthesia.utilities.FooterUtils;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity implements UserListAdapter.OnUserClickListener {

    private RecyclerView userRecyclerView;
    private UserListAdapter adapter;
    private List<String> userIdList = new ArrayList<>();
    private String userId;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        FooterUtils.setupFooter(this, R.id.profileButton);

        TextView titleTextView = findViewById(R.id.titleTextView);
        userRecyclerView = findViewById(R.id.userRecyclerView);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserListAdapter(userIdList, this);
        userRecyclerView.setAdapter(adapter);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        type = intent.getStringExtra("type");

        if ("followers".equals(type)) {
            titleTextView.setText("Followers");
            loadFollowers();
        } else if ("following".equals(type)) {
            titleTextView.setText("Following");
            loadFollowing();
        }
    }

    private void loadFollowers() {
        db.collection("followers")
                .document(userId)
                .collection("followers")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        userIdList.add(doc.getId());
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("LoadFollowers", "Erreur lors du chargement des followers", e));
    }

    private void loadFollowing() {
        db.collection("followers")
                .document(userId)
                .collection("following")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        userIdList.add(doc.getId());
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("LoadFollowing", "Erreur lors du chargement des following", e));
    }

    @Override
    public void onUserClick(String userId) {
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }
}