package com.example.library_management.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.example.library_management.DatabaseHelper;
import com.example.library_management.MainActivity;
import com.example.library_management.R;
import com.google.android.material.button.MaterialButton;

public class ProfileFragment extends Fragment {

    private TextView userEmailText, totalBooksText, borrowedBooksText, overdueBooksText;
    private MaterialButton logoutButton;
    private CardView statsCard, borrowedBooksCard, overdueWarningCard, myCollectionCard;
    private DatabaseHelper db;
    private String userEmail;

    public ProfileFragment() {}

    public static ProfileFragment newInstance(String userEmail) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("userEmail", userEmail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userEmail = getArguments().getString("userEmail");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initializeViews(view);
        setupDatabase();
        loadUserStats();
        setupClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        userEmailText = view.findViewById(R.id.userEmailText);
        totalBooksText = view.findViewById(R.id.totalBooksText);
        borrowedBooksText = view.findViewById(R.id.borrowedBooksText);
        overdueBooksText = view.findViewById(R.id.overdueBooksText);
        logoutButton = view.findViewById(R.id.logoutButton);
        statsCard = view.findViewById(R.id.statsCard);
        borrowedBooksCard = view.findViewById(R.id.borrowedBooksCard);
        overdueWarningCard = view.findViewById(R.id.overdueWarningCard);
        myCollectionCard = view.findViewById(R.id.myCollectionCard);

        // Debug logging
        android.util.Log.d("ProfileFragment", "My Collection Card found: " + (myCollectionCard != null));
        if (myCollectionCard != null) {
            android.util.Log.d("ProfileFragment", "My Collection Card visibility: " + myCollectionCard.getVisibility());
        }
    }

    private void setupDatabase() {
        db = new DatabaseHelper(requireContext());
    }

    private void loadUserStats() {
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

        statsCard.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Library Statistics", Toast.LENGTH_SHORT).show();
        });

        borrowedBooksCard.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Borrowed Books History", Toast.LENGTH_SHORT).show();
        });

        myCollectionCard.setOnClickListener(v -> {
            // Navigate to dedicated Collection page
            android.util.Log.d("ProfileFragment", "My Collection card clicked");
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
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
