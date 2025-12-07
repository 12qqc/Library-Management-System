package com.example.library_management.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;

import com.example.library_management.DatabaseHelper;
import com.example.library_management.R;

public class DashboardFragment extends Fragment {

    private static final String ARG_EMAIL = "email";
    private String userEmail;

    private TextView userEmailText, totalBooksText, borrowedBooksText, overdueBooksText;
    private MaterialButton logoutButton, btnBackToHome;
    private CardView statsCard, borrowedBooksCard, overdueWarningCard, myCollectionCard;
    private DatabaseHelper db;

    public DashboardFragment() { }

    public static DashboardFragment newInstance(String email) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userEmail = getArguments().getString(ARG_EMAIL);
        }
        db = new DatabaseHelper(requireContext());
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        initializeViews(view);
        loadDashboardData();
        setupClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        userEmailText = view.findViewById(R.id.userEmailText);
        totalBooksText = view.findViewById(R.id.totalBooksText);
        borrowedBooksText = view.findViewById(R.id.borrowedBooksText);
        overdueBooksText = view.findViewById(R.id.overdueBooksText);
        logoutButton = view.findViewById(R.id.logoutButton);
        btnBackToHome = view.findViewById(R.id.btnBackToHome);
        statsCard = view.findViewById(R.id.statsCard);
        borrowedBooksCard = view.findViewById(R.id.borrowedBooksCard);
        overdueWarningCard = view.findViewById(R.id.overdueWarningCard);
        myCollectionCard = view.findViewById(R.id.myCollectionCard);

        android.util.Log.d("DashboardFragment", "Views initialized successfully");
        android.util.Log.d("DashboardFragment", "My Collection Card found: " + (myCollectionCard != null));
    }

    private void loadDashboardData() {
        userEmailText.setText(userEmail != null ? userEmail : "Guest User");

        // Set total books count to 25 (fixed library collection)
        totalBooksText.setText("25");

        // Get borrowed books count
        int borrowedBooks = db.getBorrowedBooksCount(userEmail);

        // Get overdue books count
        int overdueBooks = db.getOverdueBooksCount(userEmail);

        borrowedBooksText.setText(String.valueOf(borrowedBooks));
        overdueBooksText.setText(String.valueOf(overdueBooks));

        // Show/hide overdue warning
        if (overdueBooks > 0) {
            overdueWarningCard.setVisibility(View.VISIBLE);
        } else {
            overdueWarningCard.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        logoutButton.setOnClickListener(v -> {
            // Show confirmation dialog
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        logout();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        btnBackToHome.setOnClickListener(v -> {
            // Navigate back to BooksFragment (home page)
            if (getActivity() instanceof com.example.library_management.HomeActivity) {
                com.example.library_management.HomeActivity homeActivity = (com.example.library_management.HomeActivity) getActivity();
                homeActivity.getBottomNavigationView().setSelectedItemId(R.id.nav_books);
                Toast.makeText(requireContext(), "Back to Home", Toast.LENGTH_SHORT).show();
            } else {
                android.util.Log.e("DashboardFragment", "Activity is not HomeActivity");
                Toast.makeText(requireContext(), "Navigation error", Toast.LENGTH_SHORT).show();
            }
        });

        statsCard.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Library Statistics", Toast.LENGTH_SHORT).show();
        });

        borrowedBooksCard.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getActivity(), com.example.library_management.BorrowHistoryActivity.class);
            intent.putExtra("email", userEmail);
            startActivity(intent);
        });

        myCollectionCard.setOnClickListener(v -> {
            // Navigate to dedicated Collection page
            android.util.Log.d("DashboardFragment", "My Collection card clicked");
            Toast.makeText(requireContext(), "Opening My Collection", Toast.LENGTH_SHORT).show();

            // Replace current fragment with CollectionFragment
            CollectionFragment collectionFragment = CollectionFragment.newInstance(userEmail);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, collectionFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void logout() {
        android.content.Intent intent = new android.content.Intent(requireContext(), com.example.library_management.MainActivity.class);
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
