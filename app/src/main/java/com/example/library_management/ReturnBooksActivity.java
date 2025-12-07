package com.example.library_management;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReturnBooksActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyStateText;
    private TextView bookCountText;
    private DatabaseHelper db;
    private String userEmail;
    private BorrowedBooksAdapter adapter;
    private List<BorrowedBook> borrowedBooks;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_books);

        // Get user email from intent
        userEmail = getIntent().getStringExtra("email");

        initializeViews();
        setupToolbar();
        setupDatabase();
        setupRecyclerView();
        setupSwipeRefresh();
        loadBorrowedBooks();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewBorrowedBooks);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        emptyStateText = findViewById(R.id.emptyStateText);
        bookCountText = findViewById(R.id.bookCountText);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Return Books");
        }
    }

    private void setupDatabase() {
        db = new DatabaseHelper(this);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        borrowedBooks = new ArrayList<>();
        adapter = new BorrowedBooksAdapter(borrowedBooks);
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadBorrowedBooks);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
    }

    private void loadBorrowedBooks() {
        swipeRefreshLayout.setRefreshing(true);

        new Thread(() -> {
            List<String[]> borrowedBooksData = db.getBorrowedBooks(userEmail);
            List<BorrowedBook> books = new ArrayList<>();

            if (borrowedBooksData != null && !borrowedBooksData.isEmpty()) {
                for (String[] bookData : borrowedBooksData) {
                    String bookTitle = bookData[0];
                    String borrowDate = bookData[1];
                    String dueDate = bookData[2];
                    // Assuming borrowId is not directly available from List<String[]> or is not strictly needed for display
                    // If borrowId is crucial, we'd need to modify getBorrowedBooks in DatabaseHelper to return it.
                    // For now, let's pass a placeholder or re-evaluate if it's needed in BorrowedBook constructor.
                    // For simplicity, let's use a dummy ID for now, or remove it from BorrowedBook if not used.
                    // Looking at BorrowedBook constructor, it expects 4 arguments including borrowId. Let's make it 0.
                    books.add(new BorrowedBook(bookTitle, borrowDate, dueDate, 0)); // Dummy borrowId
                }
            }

            runOnUiThread(() -> {
                borrowedBooks.clear();
                borrowedBooks.addAll(books);
                adapter.notifyDataSetChanged();
                updateUI();
                swipeRefreshLayout.setRefreshing(false);
            });
        }).start();
    }

    private void updateUI() {
        if (borrowedBooks.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            bookCountText.setText("No books to return");
        } else {
            emptyStateText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            bookCountText.setText(borrowedBooks.size() + " book(s) to return");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private static class BorrowedBook {
        String title;
        String borrowDate;
        String dueDate;
        int borrowId;

        BorrowedBook(String title, String borrowDate, String dueDate, int borrowId) {
            this.title = title;
            this.borrowDate = borrowDate;
            this.dueDate = dueDate;
            this.borrowId = borrowId;
        }
    }

    private class BorrowedBooksAdapter extends RecyclerView.Adapter<BorrowedBooksAdapter.ViewHolder> {

        private List<BorrowedBook> books;

        BorrowedBooksAdapter(List<BorrowedBook> books) {
            this.books = books;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_borrowed_book, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BorrowedBook book = books.get(position);
            holder.titleText.setText(book.title);
            holder.borrowDateText.setText("Borrowed: " + book.borrowDate);
            holder.dueDateText.setText("Due: " + book.dueDate);

            // Check if overdue
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date dueDate = sdf.parse(book.dueDate);
                Date today = new Date();

                if (today.after(dueDate)) {
                    holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.error_light));
                    holder.statusText.setText("OVERDUE");
                    holder.statusText.setTextColor(getResources().getColor(R.color.error));
                } else {
                    holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.surface));
                    holder.statusText.setText("ACTIVE");
                    holder.statusText.setTextColor(getResources().getColor(R.color.success));
                }
            } catch (ParseException e) {
                holder.statusText.setText("UNKNOWN");
            }

            holder.returnButton.setOnClickListener(v -> returnBook(book));
        }

        @Override
        public int getItemCount() {
            return books.size();
        }

        private void returnBook(BorrowedBook book) {
            // Return the book using the existing method
            String returnDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            if (db.returnBook(userEmail, book.title, returnDate)) {
                Toast.makeText(ReturnBooksActivity.this, "Book returned successfully!", Toast.LENGTH_SHORT).show();
                loadBorrowedBooks(); // Refresh the list
            } else {
                Toast.makeText(ReturnBooksActivity.this, "Failed to return book", Toast.LENGTH_SHORT).show();
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleText;
            TextView borrowDateText;
            TextView dueDateText;
            TextView statusText;
            MaterialButton returnButton;
            MaterialCardView cardView;

            ViewHolder(View itemView) {
                super(itemView);
                titleText = itemView.findViewById(R.id.bookTitleText);
                borrowDateText = itemView.findViewById(R.id.borrowDateText);
                dueDateText = itemView.findViewById(R.id.dueDateText);
                statusText = itemView.findViewById(R.id.statusText);
                returnButton = itemView.findViewById(R.id.returnButton);
                cardView = itemView.findViewById(R.id.cardView);
            }
        }
    }
}
