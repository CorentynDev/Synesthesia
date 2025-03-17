package com.example.synesthesia;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import android.Manifest;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.synesthesia.fragments.AlbumDetailsFragment;
import com.example.synesthesia.fragments.ArtistDetailsFragment;
import com.example.synesthesia.fragments.BookDetailsFragment;
import com.example.synesthesia.fragments.BookmarkFragment;
import com.example.synesthesia.fragments.GameDetailsFragment;
import com.example.synesthesia.fragments.HomeFragment;
import com.example.synesthesia.fragments.MovieDetailsFragment;
import com.example.synesthesia.fragments.MusicDetailsFragment;
import com.example.synesthesia.fragments.SearchBookFragment;
import com.example.synesthesia.fragments.SearchGameFragment;
import com.example.synesthesia.fragments.SearchMovieFragment;
import com.example.synesthesia.fragments.SearchMusicFragment;
import com.example.synesthesia.fragments.UserInfoFragment;
import com.example.synesthesia.fragments.UserListFragment;
import com.example.synesthesia.fragments.UserProfileFragment;
import com.example.synesthesia.models.Album;
import com.example.synesthesia.models.Artist;
import com.example.synesthesia.models.Book;
import com.example.synesthesia.models.GiantBombGame;
import com.example.synesthesia.models.TmdbMovie;
import com.example.synesthesia.models.Track;
import com.example.synesthesia.utilities.FooterUtils;
import com.example.synesthesia.utilities.UserUtils;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean o) {
            if (o){
                Toast.makeText(MainActivity.this, "Post  notification permission granted!", Toast.LENGTH_SHORT).show();
            }
        }
    });

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
            activityResultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragmentContainer, new HomeFragment());
            fragmentTransaction.commit();
        }
        FooterUtils.setupFooter(this, R.id.homeButton);

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                new UserUtils().saveUserToken(currentUserId, token);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_info_menu, menu);
        MenuItem parametreItem = menu.findItem(R.id.paramètre); // Remplacez `R.id.parametre` par l'ID réel de l'élément
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.notif) {
            Intent intent = new Intent(this, NotifActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    public void showSearchGameFragment() {
        SearchGameFragment searchGameFragment = new SearchGameFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, searchGameFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void showGameDetailsFragment(GiantBombGame game) {
        GameDetailsFragment gameDetailsFragment = new GameDetailsFragment();

        Bundle args = new Bundle();
        args.putParcelable("game", game);
        gameDetailsFragment.setArguments(args);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, gameDetailsFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void showSearchMovieFragment() {
        SearchMovieFragment searchMovieFragment = new SearchMovieFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, searchMovieFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void showMovieDetailsFragment(TmdbMovie movie) {
        MovieDetailsFragment movieDetailsFragment = new MovieDetailsFragment();

        Bundle args = new Bundle();
        args.putParcelable("movie", movie);
        movieDetailsFragment.setArguments(args);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, movieDetailsFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


}
