package com.example.synesthesia;

import static com.example.synesthesia.R.id.menu_home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.IdRes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.synesthesia.adapters.MainPagerAdapter;
import com.example.synesthesia.fragments.BookmarksFragment;
import com.example.synesthesia.fragments.HomeFragment;
import com.example.synesthesia.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainContainerActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_container);

        // Initialize the ViewPager2 and BottomNavigationView
        viewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set up the ViewPager2 adapter
        setupViewPager();

        // Set up BottomNavigationView listener
        setupBottomNavigationView();
    }

    private void setupViewPager() {
        // Create an adapter with all fragments
        MainPagerAdapter adapter = new MainPagerAdapter(this);
        adapter.addFragment(new HomeFragment());
        adapter.addFragment(new BookmarksFragment());
        adapter.addFragment(new ProfileFragment());

        viewPager.setAdapter(adapter);

        // Synchronize swiping with BottomNavigationView
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    private void setupBottomNavigationView() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_home) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (itemId == R.id.menu_recommend) {
                viewPager.setCurrentItem(1);
                return true;
            } else if (itemId == R.id.menu_bookmarks) {
                viewPager.setCurrentItem(2);
                return true;
            } else if (itemId == R.id.menu_profile) {
                viewPager.setCurrentItem(3);
                return true;
            }
            return false;
        });
    }
}
