package com.example.library_management.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.library_management.DatabaseHelper;
import com.example.library_management.R;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollectionFragment extends Fragment {

    private static final String ARG_USER_EMAIL = "userEmail";

    private String userEmail;
    private DatabaseHelper db;

    private MaterialButton btnBackToProfile;
    private TextView totalCollectedText;
    private TextView categoriesCountText;
    private RecyclerView collectionRecyclerView;
    private View emptyStateLayout;

    private CollectionBooksAdapter adapter;
    private List<BookItem> collectedBooks;

    public CollectionFragment() {}

    public static CollectionFragment newInstance(String userEmail) {
        CollectionFragment fragment = new CollectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_EMAIL, userEmail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userEmail = getArguments().getString(ARG_USER_EMAIL);
        }
        db = new DatabaseHelper(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collection, container, false);

        initializeViews(view);
        setupRecyclerView();
        loadCollectionData();
        setupClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        btnBackToProfile = view.findViewById(R.id.btnBackToProfile);
        totalCollectedText = view.findViewById(R.id.totalCollectedText);
        categoriesCountText = view.findViewById(R.id.categoriesCountText);
        collectionRecyclerView = view.findViewById(R.id.collectionRecyclerView);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
    }

    private void setupRecyclerView() {
        collectedBooks = new ArrayList<>();
        adapter = new CollectionBooksAdapter(collectedBooks);

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        collectionRecyclerView.setLayoutManager(layoutManager);
        collectionRecyclerView.setAdapter(adapter);

        // Optimize for smooth scrolling with potentially large lists
        collectionRecyclerView.setHasFixedSize(true);
        collectionRecyclerView.setItemViewCacheSize(20);
        collectionRecyclerView.setDrawingCacheEnabled(true);
        collectionRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
    }

    private void loadCollectionData() {
        try {
            // For now, show all books in the library as the user's collection
            // In a real app, you'd track which books each user has collected
            Cursor cursor = db.getBooks();

            collectedBooks.clear();
            Set<String> categories = new HashSet<>();

            if (cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(1);
                    String author = cursor.getString(2);
                    String category = cursor.getString(3);

                    collectedBooks.add(new BookItem(title, author, category));
                    categories.add(category);
                } while (cursor.moveToNext());
            }
            cursor.close();

            // Update statistics
            totalCollectedText.setText(String.valueOf(collectedBooks.size()));
            categoriesCountText.setText(String.valueOf(categories.size()));

            // Show/hide empty state
            if (collectedBooks.isEmpty()) {
                collectionRecyclerView.setVisibility(View.GONE);
                emptyStateLayout.setVisibility(View.VISIBLE);
            } else {
                collectionRecyclerView.setVisibility(View.VISIBLE);
                emptyStateLayout.setVisibility(View.GONE);
            }

            adapter.notifyDataSetChanged();

            android.util.Log.d("CollectionFragment", "Loaded " + collectedBooks.size() + " books in collection");

        } catch (Exception e) {
            android.util.Log.e("CollectionFragment", "Error loading collection data", e);
        }
    }

    private void setupClickListeners() {
        btnBackToProfile.setOnClickListener(v -> {
            // Navigate back to profile
            if (getActivity() instanceof com.example.library_management.HomeActivity) {
                com.example.library_management.HomeActivity homeActivity = (com.example.library_management.HomeActivity) getActivity();
                homeActivity.getBottomNavigationView().setSelectedItemId(R.id.nav_profile);
            }
        });
    }

    // Simple data class for book items
    public static class BookItem {
        private String title;
        private String author;
        private String category;

        public BookItem(String title, String author, String category) {
            this.title = title;
            this.author = author;
            this.category = category;
        }

        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public String getCategory() { return category; }
    }

    // Simple adapter for the RecyclerView
    public static class CollectionBooksAdapter extends RecyclerView.Adapter<CollectionBooksAdapter.ViewHolder> {

        private List<BookItem> books;

        public CollectionBooksAdapter(List<BookItem> books) {
            this.books = books;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_book, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BookItem book = books.get(position);
            holder.titleText.setText(book.getTitle());
            holder.authorText.setText(book.getAuthor());
            holder.categoryText.setText(book.getCategory());
        }

        @Override
        public int getItemCount() {
            return books.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleText;
            TextView authorText;
            TextView categoryText;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                titleText = itemView.findViewById(R.id.bookTitle);
                authorText = itemView.findViewById(R.id.bookAuthor);
                categoryText = itemView.findViewById(R.id.bookCategory);
            }
        }
    }
}


