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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class BorrowHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyStateText;
    private TextView historyCountText;
    private DatabaseHelper db;
    private String userEmail;
    private BorrowHistoryAdapter adapter;
    private List<BorrowHistoryItem> historyItems;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_history);

        // Get user email from intent
        userEmail = getIntent().getStringExtra("email");

        initializeViews();
        setupToolbar();
        setupDatabase();
        setupRecyclerView();
        setupSwipeRefresh();
        loadBorrowHistory();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewHistory);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        emptyStateText = findViewById(R.id.emptyStateText);
        historyCountText = findViewById(R.id.historyCountText);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Borrow History");
        }
    }

    private void setupDatabase() {
        db = new DatabaseHelper(this);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyItems = new ArrayList<>();
        adapter = new BorrowHistoryAdapter(historyItems);
        recyclerView.setAdapter(adapter);

        // Optimize for smooth scrolling with potentially large lists
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadBorrowHistory);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
    }

    private void loadBorrowHistory() {
        swipeRefreshLayout.setRefreshing(true);

        new Thread(() -> {
            Cursor cursor = db.getBorrowHistory(userEmail);
            List<BorrowHistoryItem> items = new ArrayList<>();

            if (cursor.moveToFirst()) {
                do {
                    String bookTitle = cursor.getString(0);
                    String borrowDate = cursor.getString(1);
                    String returnDate = cursor.getString(2);
                    String status = cursor.getString(3);

                    items.add(new BorrowHistoryItem(bookTitle, borrowDate, returnDate, status));
                } while (cursor.moveToNext());
            }
            cursor.close();

            runOnUiThread(() -> {
                historyItems.clear();
                historyItems.addAll(items);
                adapter.notifyDataSetChanged();
                updateUI();
                swipeRefreshLayout.setRefreshing(false);
            });
        }).start();
    }

    private void updateUI() {
        if (historyItems.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            historyCountText.setText("No borrowing history");
        } else {
            emptyStateText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            historyCountText.setText(historyItems.size() + " borrowing record(s)");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private static class BorrowHistoryItem {
        String title;
        String borrowDate;
        String returnDate;
        String status;

        BorrowHistoryItem(String title, String borrowDate, String returnDate, String status) {
            this.title = title;
            this.borrowDate = borrowDate;
            this.returnDate = returnDate != null ? returnDate : "Not returned";
            this.status = status;
        }
    }

    private class BorrowHistoryAdapter extends RecyclerView.Adapter<BorrowHistoryAdapter.ViewHolder> {

        private List<BorrowHistoryItem> historyItems;

        BorrowHistoryAdapter(List<BorrowHistoryItem> historyItems) {
            this.historyItems = historyItems;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_borrow_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BorrowHistoryItem item = historyItems.get(position);
            holder.titleText.setText(item.title);
            holder.borrowDateText.setText("Borrowed: " + item.borrowDate);
            holder.returnDateText.setText("Returned: " + item.returnDate);

            // Set status color
            if ("Returned".equals(item.status)) {
                holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.success_light));
                holder.statusText.setText("RETURNED");
                holder.statusText.setTextColor(getResources().getColor(R.color.success));
            } else if ("Overdue".equals(item.status)) {
                holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.error_light));
                holder.statusText.setText("OVERDUE");
                holder.statusText.setTextColor(getResources().getColor(R.color.error));
            } else {
                holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.surface));
                holder.statusText.setText(item.status.toUpperCase());
                holder.statusText.setTextColor(getResources().getColor(R.color.primary));
            }
        }

        @Override
        public int getItemCount() {
            return historyItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleText;
            TextView borrowDateText;
            TextView returnDateText;
            TextView statusText;
            MaterialCardView cardView;

            ViewHolder(View itemView) {
                super(itemView);
                titleText = itemView.findViewById(R.id.historyBookTitleText);
                borrowDateText = itemView.findViewById(R.id.historyBorrowDateText);
                returnDateText = itemView.findViewById(R.id.historyReturnDateText);
                statusText = itemView.findViewById(R.id.historyStatusText);
                cardView = itemView.findViewById(R.id.historyCardView);
            }
        }
    }
}







