package com.example.synesthesia;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.synesthesia.fragments.HomeFragment;
import com.example.synesthesia.fragments.SearchUserFragment;
import com.example.synesthesia.fragments.UserInfoFragment;
import com.example.synesthesia.fragments.UserListFragment;
import com.example.synesthesia.fragments.UserProfileFragment;
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
        UserListFragment userListFragment = UserListFragment.newInstance(userId, type);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, userListFragment);

        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void showUserProfileFragment(String userId) {
        UserProfileFragment userProfileFragment = new UserProfileFragment();

        Bundle args = new Bundle();
        args.putString("userId", userId);
        userProfileFragment.setArguments(args);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, userProfileFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void showUserInfoFragment() {
        UserInfoFragment userInfoFragment = new UserInfoFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, userInfoFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
