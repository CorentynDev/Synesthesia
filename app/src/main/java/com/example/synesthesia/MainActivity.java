package com.example.synesthesia;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.synesthesia.fragments.AlbumDetailsFragment;
import com.example.synesthesia.fragments.ArtistDetailsFragment;
import com.example.synesthesia.fragments.BookDetailsFragment;
import com.example.synesthesia.fragments.BookmarkFragment;
import com.example.synesthesia.fragments.HomeFragment;
import com.example.synesthesia.fragments.MusicDetailsFragment;
import com.example.synesthesia.fragments.SearchBookFragment;
import com.example.synesthesia.fragments.SearchMusicFragment;
import com.example.synesthesia.fragments.UserInfoFragment;
import com.example.synesthesia.fragments.UserListFragment;
import com.example.synesthesia.fragments.UserProfileFragment;
import com.example.synesthesia.models.Album;
import com.example.synesthesia.models.Artist;
import com.example.synesthesia.models.Book;
import com.example.synesthesia.models.Track;
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

    public void showBookmarksFragment() {
        BookmarkFragment bookmarkFragment = new BookmarkFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, bookmarkFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void showArtistDetailsFragment(Artist artist) {
        ArtistDetailsFragment artistDetailsFragment = new ArtistDetailsFragment();

        // Passez l'objet Artist au fragment
        Bundle args = new Bundle();
        args.putParcelable("artist", artist);
        artistDetailsFragment.setArguments(args);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, artistDetailsFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void showSearchMusicFragment() {
        SearchMusicFragment searchMusicFragment = new SearchMusicFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, searchMusicFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void showAlbumDetailsFragment(Album album) {
        AlbumDetailsFragment albumDetailsFragment = new AlbumDetailsFragment();

        Bundle args = new Bundle();
        args.putParcelable("album", album);
        albumDetailsFragment.setArguments(args);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, albumDetailsFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void showMusicDetailsFragment(Track track) {
        MusicDetailsFragment musicDetailsFragment = new MusicDetailsFragment();

        Bundle args = new Bundle();
        args.putParcelable("track", track);
        musicDetailsFragment.setArguments(args);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, musicDetailsFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void navigateToMainPage() {
        HomeFragment homeFragment = new HomeFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, homeFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void showSearchBookFragment() {
        SearchBookFragment searchBookFragment = new SearchBookFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, searchBookFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void showBookDetailsFragment(Book book) {
        BookDetailsFragment bookDetailsFragment = new BookDetailsFragment();

        Bundle args = new Bundle();
        args.putParcelable("book", book);
        bookDetailsFragment.setArguments(args);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, bookDetailsFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}
