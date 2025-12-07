package com.example.library_management;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.Toast;
import android.database.Cursor;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BorrowBookActivity extends AppCompatActivity {

    private AutoCompleteTextView bookSpinner;
    private TextInputEditText dateInput;
    private MaterialButton borrowBtn, backBtn;

    private DatabaseHelper db;
    private String userEmail;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_book);

        userEmail = getIntent().getStringExtra("email");
        db = new DatabaseHelper(this);

        initializeViews();
        loadBooks();
        setupDatePicker();
        setupClickListeners();
        setDefaultDates();
    }

    private void initializeViews() {
        bookSpinner = findViewById(R.id.bookSpinner);
        dateInput = findViewById(R.id.dateInput);
        borrowBtn = findViewById(R.id.borrowButton2);
        backBtn = findViewById(R.id.btnBack);
    }

    private void loadBooks() {
        Cursor cursor = db.getBooks();
        String[] books = new String[cursor.getCount()];

        if (cursor.moveToFirst()) {
            int i = 0;
            do {
                String title = cursor.getString(1);
                String author = cursor.getString(2);
                books[i++] = title + " - " + author;
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (books.length > 0) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, books);
            bookSpinner.setAdapter(adapter);
            bookSpinner.setText(books[0], false);
        } else {
            Toast.makeText(this, "No books available to borrow", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupClickListeners() {
        borrowBtn.setOnClickListener(v -> {
            animateButtonClick(v);
            borrowBook();
        });

        backBtn.setOnClickListener(v -> {
            animateButtonClick(v);
            finish();
        });

        dateInput.setOnClickListener(v -> showDatePicker());
    }

    private void setupDatePicker() {
        dateInput.setFocusable(false);
        dateInput.setClickable(true);
    }

    private void setDefaultDates() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());

        // Calculate due date (14 days from today)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 14);
        String dueDate = sdf.format(calendar.getTime());

        dateInput.setText(today);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        String selectedDate = selectedYear + "-" +
                                String.format("%02d", (selectedMonth + 1)) + "-" +
                                String.format("%02d", selectedDay);
                        dateInput.setText(selectedDate);
                    }
                },
                year, month, day);

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void animateButtonClick(View view) {
        android.view.animation.Animation animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        animation.setDuration(150);
        view.startAnimation(animation);
    }

    private void borrowBook() {
        String selectedBook = bookSpinner.getText().toString().trim();
        String borrowDate = dateInput.getText().toString().trim();

        // Validation
        if (selectedBook.isEmpty()) {
            Toast.makeText(this, "Please select a book", Toast.LENGTH_SHORT).show();
            return;
        }

        if (borrowDate.isEmpty()) {
            dateInput.setError("Borrow date is required");
            dateInput.requestFocus();
            return;
        }

        // Extract book title from selection (remove author part)
        String bookTitle = selectedBook;
        if (selectedBook.contains(" - ")) {
            bookTitle = selectedBook.substring(0, selectedBook.indexOf(" - "));
        }

        // Calculate due date (14 days from borrow date)
        String dueDate = calculateDueDate(borrowDate, 14);

        // Debug logging
        android.util.Log.d("BorrowBook", "Attempting to borrow book:");
        android.util.Log.d("BorrowBook", "Selected book string: " + selectedBook);
        android.util.Log.d("BorrowBook", "Extracted book title: " + bookTitle);
        android.util.Log.d("BorrowBook", "User: " + userEmail);
        android.util.Log.d("BorrowBook", "Borrow Date: " + borrowDate);
        android.util.Log.d("BorrowBook", "Due Date: " + dueDate);

        // Check if book exists first
        boolean bookExists = db.bookExists(bookTitle);
        android.util.Log.d("BorrowBook", "Book exists check: " + bookTitle + " -> " + bookExists);
        if (!bookExists) {
            Toast.makeText(this, "Selected book is not available in the library.", Toast.LENGTH_LONG).show();
            return;
        }

        // Check if user already has this book
        boolean alreadyBorrowed = db.hasBorrowedBook(userEmail, bookTitle);
        android.util.Log.d("BorrowBook", "Already borrowed check: " + userEmail + " - " + bookTitle + " -> " + alreadyBorrowed);
        if (alreadyBorrowed) {
            Toast.makeText(this, "You have already borrowed this book.", Toast.LENGTH_LONG).show();
            return;
        }

        // Borrow book with due date
        boolean result = db.borrowBook(userEmail, bookTitle, borrowDate, dueDate);

        android.util.Log.d("BorrowBook", "Borrow result: " + result);

        if (result) {
            Toast.makeText(this, "Book borrowed successfully!\nDue date: " + dueDate, Toast.LENGTH_LONG).show();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("action", "borrow");
            setResult(RESULT_OK, resultIntent);

            // Reset the form instead of closing the activity
            resetForm();
        } else {
            Toast.makeText(this, "Failed to borrow book. Please try again.", Toast.LENGTH_SHORT).show();
            // Even if borrowing fails, return to home page after a short delay
            new android.os.Handler().postDelayed(() -> {
                finish();
            }, 2000); // 2 second delay to show the error message
        }
    }

    private void resetForm() {
        // Clear the book selection
        bookSpinner.setText("", false);

        // Reset dates to default
        setDefaultDates();

        // Reload available books (in case some became unavailable)
        loadBooks();

        // Show first book as selected again
        Cursor cursor = db.getBooks();
        if (cursor.moveToFirst()) {
            String title = cursor.getString(1);
            String author = cursor.getString(2);
            bookSpinner.setText(title + " - " + author, false);
        }
        cursor.close();
    }

    private String calculateDueDate(String startDate, int daysToAdd) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(startDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_YEAR, daysToAdd);
            return sdf.format(calendar.getTime());
        } catch (Exception e) {
            // Fallback: return a date 14 days from today
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 14);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.format(calendar.getTime());
        }
    }
}
