package com.example.library_management.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.library_management.BookLibrary;
import com.example.library_management.DatabaseHelper;
import com.example.library_management.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class BooksFragment extends Fragment {

    // UI Components
    private ListView booksListView;
    private EditText searchEditText;
    private FloatingActionButton floatingActionButton;
    private MaterialButton btnBorrowBook;
    private MaterialButton btnAddBook;
    private MaterialButton btnMyBooks;
    private MaterialButton btnProfile;
    private DatabaseHelper db;
    private ArrayAdapter<String> adapter;
    private List<String> allBooks;
    private List<String> filteredBooks;
    private String userEmail;

    public BooksFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        android.util.Log.d("BooksFragment", "onCreateView called");
        View view = inflater.inflate(R.layout.fragment_books, container, false);

        // Get user email from parent activity
        try {
            if (getActivity() instanceof com.example.library_management.HomeActivity) {
                com.example.library_management.HomeActivity homeActivity = (com.example.library_management.HomeActivity) getActivity();
                userEmail = homeActivity.getUserEmail();
                android.util.Log.d("BooksFragment", "Got userEmail: " + userEmail);
            } else {
                android.util.Log.w("BooksFragment", "Activity is not HomeActivity: " + getActivity());
            }
        } catch (Exception e) {
            android.util.Log.e("BooksFragment", "Error getting user email", e);
        }

        initializeViews(view);
        setupDatabase();
        loadBooksData();
        setupSearch();
        setupFab();

        // Start entrance animations
        startEntranceAnimations(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh books data when fragment becomes visible
        // This ensures newly collected books appear in the list
        loadBooksData();
    }

    private void initializeViews(View view) {
        booksListView = view.findViewById(R.id.booksListView);
        searchEditText = view.findViewById(R.id.searchEditText);
        floatingActionButton = view.findViewById(R.id.fabAddBook);
        btnBorrowBook = view.findViewById(R.id.btnBorrowBook);
        btnAddBook = view.findViewById(R.id.btnAddBook);
        btnMyBooks = view.findViewById(R.id.btnMyBooks);
        btnProfile = view.findViewById(R.id.btnBackBooks);

        // Configure ListView for optimal scrolling with many books
        booksListView.setDividerHeight(1);
        booksListView.setScrollingCacheEnabled(true);
        booksListView.setSmoothScrollbarEnabled(true);
        booksListView.setCacheColorHint(android.graphics.Color.TRANSPARENT);
        booksListView.setFastScrollEnabled(true);

        // Additional optimizations for smooth scrolling
        booksListView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        booksListView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);

        // Enable hardware acceleration for smoother scrolling
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            booksListView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    private void setupDatabase() {
        db = new DatabaseHelper(requireContext());
    }

    private void loadBooksData() {
        try {
            android.util.Log.d("BooksFragment", "Loading fixed book collection data");

            // Load fixed collection of 25 books (not from database)
            loadFixedBookCollection();

        filteredBooks.addAll(allBooks);

        adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, filteredBooks);
        booksListView.setAdapter(adapter);

            // Ensure smooth scrolling for many books
            booksListView.setSmoothScrollbarEnabled(true);
            booksListView.setFastScrollEnabled(true);

            // Optimize adapter performance
            adapter.setNotifyOnChange(false); // We'll manually notify when needed

            // Update UI to show book count for better user experience
            String titleText = "📚 Your Collected Books (" + filteredBooks.size() + ")";
            android.widget.TextView titleView = requireView().findViewById(R.id.titleText);
            if (titleView != null) {
                titleView.setText(titleText);
            }

            android.util.Log.d("BooksFragment", "Books list optimized for scrolling with " + filteredBooks.size() + " books");

            android.util.Log.d("BooksFragment", "Fixed book collection loaded successfully: " + allBooks.size() + " books");
        } catch (Exception e) {
            android.util.Log.e("BooksFragment", "Error loading fixed book collection", e);
            android.widget.Toast.makeText(requireContext(), "Error loading book collection: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFixedBookCollection() {
        allBooks = new ArrayList<>();
        filteredBooks = new ArrayList<>();

        // Get the fixed collection of 25 books from BookLibrary (shared with AddBookActivity)
        String[][] fixedBooks = BookLibrary.getFixedBooks();

        // Format and add all books to the list for display
        for (String[] book : fixedBooks) {
            String title = book[0];
            String author = book[1];
            String category = book[2];
            // Format the book entry for display
            String formattedBook = BookLibrary.formatBookForDisplay(title, author, category);
            allBooks.add(formattedBook);
        }
    }

    private void addSampleBooksIfEmpty() {
        try {
            Cursor cursor = db.getBooks();
            int bookCount = cursor.getCount();
            cursor.close();

            if (bookCount == 0) {
                android.util.Log.d("BooksFragment", "Database is empty, adding sample books for collection functionality");
                addSampleBooks();
                android.widget.Toast.makeText(requireContext(), "Library database initialized with sample books!", android.widget.Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            android.util.Log.e("BooksFragment", "Error checking database", e);
        }
    }

    private void addSampleBooks() {
        // Use the shared BookLibrary data (same as home page and AddBookActivity)
        String[][] sampleBooks = BookLibrary.getFixedBooks();

        for (String[] book : sampleBooks) {
            db.addBook(book[0], book[1], book[2]); // {title, author, category}
        }
        android.util.Log.d("BooksFragment", "Added " + sampleBooks.length + " sample books from BookLibrary");
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBooks(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterBooks(String query) {
        filteredBooks.clear();

        if (query.isEmpty()) {
            filteredBooks.addAll(allBooks);
        } else {
            String lowerQuery = query.toLowerCase();
            // Optimize search for better performance with many books
            for (String book : allBooks) {
                if (book.toLowerCase().contains(lowerQuery)) {
                    filteredBooks.add(book);
                }
            }
        }

        // Efficiently update the adapter
        adapter.clear();
        adapter.addAll(filteredBooks);
        adapter.notifyDataSetChanged();

        // Smooth scroll to top after filtering
        if (booksListView != null) {
            booksListView.post(() -> booksListView.setSelection(0));
        }
    }

    private void startEntranceAnimations(View view) {
        Handler handler = new Handler();

        // Animate search bar
        searchEditText.setAlpha(0f);
        searchEditText.setTranslationY(-20f);
        searchEditText.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(200)
                .start();

        // Animate quick action buttons with staggered entrance
        if (btnBorrowBook != null) {
            btnBorrowBook.setAlpha(0f);
            btnBorrowBook.setTranslationY(30f);
            handler.postDelayed(() -> {
                btnBorrowBook.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .start();
            }, 300);
        }

        if (btnAddBook != null) {
            btnAddBook.setAlpha(0f);
            btnAddBook.setTranslationY(30f);
            handler.postDelayed(() -> {
                btnAddBook.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .start();
            }, 400);
        }

        if (btnMyBooks != null) {
            btnMyBooks.setAlpha(0f);
            btnMyBooks.setTranslationY(30f);
            handler.postDelayed(() -> {
                btnMyBooks.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .start();
            }, 500);
        }

        // Animate FAB with bounce effect
        floatingActionButton.setAlpha(0f);
        floatingActionButton.setScaleX(0.3f);
        floatingActionButton.setScaleY(0.3f);

        handler.postDelayed(() -> {
            Animation bounceAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce);
            floatingActionButton.startAnimation(bounceAnimation);
            floatingActionButton.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(600)
                    .start();
        }, 600);

        // Animate ListView with fade in
        if (booksListView != null) {
            booksListView.setAlpha(0f);
            handler.postDelayed(() -> {
                booksListView.animate()
                    .alpha(1f)
                    .setDuration(600)
                    .start();
            }, 700);
        }
    }

    private void setupFab() {
        // FAB click listener
        floatingActionButton.setOnClickListener(v -> {
            // Animate FAB click
            animateFabClick();

            // Handle action with delay for better UX
            new Handler().postDelayed(() -> {
                if (getActivity() instanceof BookActionListener) {
                    ((BookActionListener) getActivity()).onAddBookClicked();
                }
            }, 200);
        });

        // Add Book button click listener
        btnAddBook.setOnClickListener(v -> {
            if (getActivity() instanceof BookActionListener) {
                ((BookActionListener) getActivity()).onAddBookClicked();
            }
        });

        // Borrow Book button click listener
        btnBorrowBook.setOnClickListener(v -> {
            if (getActivity() instanceof BookActionListener) {
                ((BookActionListener) getActivity()).onBorrowBookClicked();
            }
        });

        // My Books button click listener
        btnMyBooks.setOnClickListener(v -> {
            if (getActivity() instanceof BookActionListener) {
                ((BookActionListener) getActivity()).onMyBooksClicked();
            }
        });

        // Profile button click listener - directly go to dashboard
        btnProfile.setOnClickListener(v -> {
            android.widget.Toast.makeText(requireContext(), "Going to Dashboard...", android.widget.Toast.LENGTH_SHORT).show();
            if (getActivity() instanceof BookActionListener) {
                ((BookActionListener) getActivity()).onBackToDashboardClicked();
    }
        });
    }


    private void animateFabClick() {
        ScaleAnimation scaleDown = new ScaleAnimation(
                1.0f, 0.8f, 1.0f, 0.8f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleDown.setDuration(100);

        ScaleAnimation scaleUp = new ScaleAnimation(
                0.8f, 1.1f, 0.8f, 1.1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleUp.setDuration(200);
        scaleUp.setStartOffset(100);

        ScaleAnimation scaleNormal = new ScaleAnimation(
                1.1f, 1.0f, 1.1f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleNormal.setDuration(150);
        scaleNormal.setStartOffset(300);

        android.view.animation.AnimationSet animationSet = new android.view.animation.AnimationSet(true);
        animationSet.addAnimation(scaleDown);
        animationSet.addAnimation(scaleUp);
        animationSet.addAnimation(scaleNormal);

        floatingActionButton.startAnimation(animationSet);
    }

    public void refreshBooks() {
        loadBooksData();
    }

    public interface BookActionListener {
        void onAddBookClicked();
        void onBorrowBookClicked();
        void onMyBooksClicked();
        void onBackToDashboardClicked();
    }
}
