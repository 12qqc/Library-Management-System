package com.example.library_management.ui;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.library_management.DatabaseHelper;
import com.example.library_management.R;
import com.google.android.material.button.MaterialButton;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyBooksFragment extends Fragment {

    private LinearLayout booksContainer;
    private TextView titleText;
    private TextView emptyStateText;
    private MaterialButton btnBackToHome;

    private DatabaseHelper db;
    private String userEmail;


    public MyBooksFragment() {
        // Required empty public constructor
    }

    public static MyBooksFragment newInstance(String email) {
        MyBooksFragment fragment = new MyBooksFragment();
        Bundle args = new Bundle();
        args.putString("email", email);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userEmail = getArguments().getString("email");
        }
        db = new DatabaseHelper(getContext());
    }

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        android.util.Log.d("MyBooksFragment", "onAttach called - Activity: " + (getActivity() != null ? getActivity().getClass().getSimpleName() : "NULL"));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_my_books, container, false);
            initializeViews(view);
        setupListeners();
        loadBorrowedBook();
            return view;
    }

    private void initializeViews(View view) {
        booksContainer = view.findViewById(R.id.booksContainer);
        titleText = view.findViewById(R.id.titleText);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        btnBackToHome = view.findViewById(R.id.btnBackToHome);
    }

    private void setupListeners() {
        if (btnBackToHome != null) {
            btnBackToHome.setOnClickListener(v -> {
                android.util.Log.d("MyBooksFragment", "Back button clicked - navigating to home");
                android.widget.Toast.makeText(getActivity(), "Going back to Home...", android.widget.Toast.LENGTH_SHORT).show();

                if (getActivity() != null && getActivity() instanceof com.example.library_management.HomeActivity) {
                    com.example.library_management.HomeActivity homeActivity = (com.example.library_management.HomeActivity) getActivity();
                    homeActivity.getBottomNavigationView().setSelectedItemId(R.id.nav_books);
                    android.util.Log.d("MyBooksFragment", "Navigation to books page successful");
                } else {
                    android.util.Log.e("MyBooksFragment", "Cannot navigate - activity not HomeActivity");
                    android.widget.Toast.makeText(getActivity(), "Navigation error", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }
        // Listeners will be set up for each individual book card
    }


    public void loadBorrowedBook() {
        List<String[]> borrowedBooksData = db.getBorrowedBooks(userEmail);

        // Clear existing book cards
            booksContainer.removeAllViews();

        if (borrowedBooksData != null && !borrowedBooksData.isEmpty()) {
            emptyStateText.setVisibility(View.GONE);

            // Create a card for each borrowed book
            for (String[] bookData : borrowedBooksData) {
                String title = bookData[0];
                String borrowDate = bookData[1];
                String dueDate = bookData[2];

                createBookCard(title, borrowDate, dueDate);
            }

            android.util.Log.d("MyBooksFragment", "Loaded " + borrowedBooksData.size() + " borrowed books");
            } else {
            emptyStateText.setVisibility(View.VISIBLE);
            android.util.Log.d("MyBooksFragment", "No borrowed books found for user: " + userEmail);
        }
    }

    private void createBookCard(String title, String borrowDate, String dueDate) {
        // Inflate the book card layout
        View bookCardView = LayoutInflater.from(getContext()).inflate(R.layout.item_borrowed_book, booksContainer, false);

        // Find views in the card
        TextView bookTitleText = bookCardView.findViewById(R.id.bookTitleText);
        TextView borrowDateText = bookCardView.findViewById(R.id.borrowDateText);
        TextView dueDateText = bookCardView.findViewById(R.id.dueDateText);
        TextView statusText = bookCardView.findViewById(R.id.statusText);
        MaterialButton returnButton = bookCardView.findViewById(R.id.returnButton);

        // Set book data
        bookTitleText.setText(title);
        borrowDateText.setText("Borrowed: " + borrowDate);
        dueDateText.setText("Due: " + dueDate);

        // Check if overdue
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date dueDateObj = sdf.parse(dueDate);
            Date today = new Date();

            if (today.after(dueDateObj)) {
                statusText.setText("OVERDUE");
                statusText.setTextColor(getResources().getColor(R.color.error));
            } else {
                statusText.setText("ACTIVE");
                statusText.setTextColor(getResources().getColor(R.color.success));
            }
        } catch (ParseException e) {
            statusText.setText("UNKNOWN");
        }

        // Set up return button click listener
        returnButton.setOnClickListener(v -> returnBook(title));

        // Add the card to the container
        booksContainer.addView(bookCardView);
    }

    private void returnBook(String bookTitle) {
        // Get current date for returnDate
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String returnDate = sdf.format(new Date());

        if (db.returnBook(userEmail, bookTitle, returnDate)) {
            Toast.makeText(getActivity(), bookTitle + " returned successfully!", Toast.LENGTH_SHORT).show();
            loadBorrowedBook(); // Refresh the display
        } else {
            Toast.makeText(getActivity(), "Failed to return " + bookTitle + ".", Toast.LENGTH_SHORT).show();
    }
    }

}
