package com.example.library_management;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.library_management.ui.BooksFragment;
import com.example.library_management.ui.DashboardFragment;
import com.example.library_management.ui.MyBooksFragment;
import com.example.library_management.ui.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class HomeActivity extends AppCompatActivity implements
        BooksFragment.BookActionListener {

    private static final int ADD_BOOK_REQUEST = 1001;

    private BottomNavigationView bottomNavigationView;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_new);

        userEmail = getIntent().getStringExtra("email");
        String startFragment = getIntent().getStringExtra("startFragment");

        // Debug logging
        android.util.Log.d("HomeActivity", "userEmail: " + userEmail);
        android.util.Log.d("HomeActivity", "startFragment: " + startFragment);

        initializeViews();

        if (findViewById(R.id.fragmentContainer) == null) {
            Toast.makeText(this, "Error: App layout is corrupted", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if ("books".equals(startFragment)) {
            android.util.Log.d("HomeActivity", "Loading BooksFragment");
            replaceFragment(new BooksFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_books);
        } else {
            android.util.Log.d("HomeActivity", "Loading default fragment (Dashboard)");
            loadDefaultFragment();
        }

        // Setup bottom navigation after a short delay to ensure fragment transaction completes
        new android.os.Handler().postDelayed(this::setupBottomNavigation, 100);
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_dashboard) {
                        selectedFragment = DashboardFragment.newInstance(userEmail);
                    } else if (itemId == R.id.nav_books) {
                        selectedFragment = new BooksFragment();
                    } else if (itemId == R.id.nav_my_books) {

                    if (userEmail == null || userEmail.isEmpty()) {
                        Toast.makeText(HomeActivity.this,
                                "Please log in to view your books",
                                Toast.LENGTH_LONG).show();
                            return false;
                        }

                    selectedFragment = MyBooksFragment.newInstance(userEmail);

                    } else if (itemId == R.id.nav_profile) {
                    selectedFragment = DashboardFragment.newInstance(userEmail);
                    }

                    if (selectedFragment != null) {
                        replaceFragment(selectedFragment);
                        return true;
                    }

                    return false;
            }
        });
    }

    private void loadDefaultFragment() {
        replaceFragment(DashboardFragment.newInstance(userEmail));
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
    }

    public String getUserEmail() {
        return userEmail;
    }

    private void replaceFragment(Fragment fragment) {
        try {
            android.util.Log.d("HomeActivity", "replaceFragment called with: " + fragment.getClass().getSimpleName());
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.setCustomAnimations(
                    R.anim.fade_in, R.anim.fade_out,
                    R.anim.fade_in, R.anim.fade_out
            );

            fragmentTransaction.replace(R.id.fragmentContainer, fragment);
            fragmentTransaction.commit();

        } catch (Exception e) {
            Toast.makeText(this, "Error loading page: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // -------------------------
    //   Fragment Callbacks
    // -------------------------

    @Override
    public void onAddBookClicked() {
        Intent intent = new Intent(this, AddBookActivity.class);
        startActivityForResult(intent, ADD_BOOK_REQUEST);
    }

    @Override
    public void onBorrowBookClicked() {
        Intent intent = new Intent(this, BorrowBookActivity.class);
        intent.putExtra("email", userEmail);
        startActivity(intent);
    }

    @Override
    public void onMyBooksClicked() {
        bottomNavigationView.setSelectedItemId(R.id.nav_my_books);
        replaceFragment(MyBooksFragment.newInstance(userEmail));
    }

    @Override
    public void onBackToDashboardClicked() {
        android.util.Log.d("HomeActivity", "onBackToDashboardClicked called - navigating to dashboard");
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
        replaceFragment(DashboardFragment.newInstance(userEmail));
        android.widget.Toast.makeText(this, "Back to Dashboard", android.widget.Toast.LENGTH_SHORT).show();
    }


    // Public getter for bottom navigation view
    public BottomNavigationView getBottomNavigationView() {
        return bottomNavigationView;
    }

    // -------------------------
    //   Refreshing Fragments
    // -------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // Handle specific request types
            if (requestCode == ADD_BOOK_REQUEST) {
                // Book was successfully added/collected
                // Refresh the BooksFragment to show the new book
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment instanceof BooksFragment) {
            ((BooksFragment) currentFragment).refreshBooks();
                }
                Toast.makeText(this, "Book added to your collection!", Toast.LENGTH_SHORT).show();
            } else {
                // General refresh for other activities (like borrowing)
                refreshCurrentFragment();

                // If user just borrowed a book, switch to My Books tab to show it immediately
                if (data != null && "borrow".equals(data.getStringExtra("action"))) {
        bottomNavigationView.setSelectedItemId(R.id.nav_my_books);
                }
            }
        }
    }

    private void refreshCurrentFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

        if (currentFragment instanceof BooksFragment) {
            ((BooksFragment) currentFragment).refreshBooks();
        } else if (currentFragment instanceof DashboardFragment) {
            replaceFragment(DashboardFragment.newInstance(userEmail));
        } else if (currentFragment instanceof MyBooksFragment) {
            ((MyBooksFragment) currentFragment).loadBorrowedBook();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCurrentFragment();
    }
}
