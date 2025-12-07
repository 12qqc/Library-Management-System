package com.example.library_management.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.library_management.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class BorrowedBooksAdapter extends RecyclerView.Adapter<BorrowedBooksAdapter.BorrowedBookViewHolder> {

    private List<BorrowedBook> borrowedBooks;
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onReturnClick(int position);
    }

    public BorrowedBooksAdapter(List<BorrowedBook> borrowedBooks, OnItemActionListener listener) {
        this.borrowedBooks = borrowedBooks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BorrowedBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_borrowed_books, parent, false);
        return new BorrowedBookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BorrowedBookViewHolder holder, int position) {
        BorrowedBook book = borrowedBooks.get(position);
        holder.bookTitleText.setText(book.getTitle());
        holder.borrowDateText.setText("Borrowed: " + book.getBorrowDate());
        holder.dueDateText.setText("Due: " + book.getDueDate());
        holder.statusText.setText(book.getStatus());

        holder.returnButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReturnClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return borrowedBooks.size();
    }

    public static class BorrowedBookViewHolder extends RecyclerView.ViewHolder {
        TextView bookTitleText, borrowDateText, dueDateText, statusText;
        MaterialButton returnButton;

        public BorrowedBookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookTitleText = itemView.findViewById(R.id.bookTitleText);
            borrowDateText = itemView.findViewById(R.id.borrowDateText);
            dueDateText = itemView.findViewById(R.id.dueDateText);
            statusText = itemView.findViewById(R.id.statusText);
            returnButton = itemView.findViewById(R.id.returnButton);
        }
    }

    // Helper class for data
    public static class BorrowedBook {
        private String title;
        private String borrowDate;
        private String dueDate;
        private String status;

        public BorrowedBook(String title, String borrowDate, String dueDate, String status) {
            this.title = title;
            this.borrowDate = borrowDate;
            this.dueDate = dueDate;
            this.status = status;
        }

        public String getTitle() {
            return title;
        }

        public String getBorrowDate() {
            return borrowDate;
        }

        public String getDueDate() {
            return dueDate;
        }

        public String getStatus() {
            return status;
        }
    }
}












