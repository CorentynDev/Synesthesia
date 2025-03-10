package com.example.synesthesia;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.synesthesia.fragments.HomeFragment;
import com.example.synesthesia.fragments.SearchUserFragment;
import com.example.synesthesia.utilities.FooterUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragmentContainer, new HomeFragment());
            fragmentTransaction.commit();
        }
        FooterUtils.setupFooter(this, R.id.homeButton);
    }

    public void showUserListFragment(String userId, String type) {
        SearchUserFragment searchUserFragment = SearchUserFragment.newInstance(userId, type);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, searchUserFragment);

        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
