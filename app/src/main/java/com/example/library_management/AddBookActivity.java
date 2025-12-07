package com.example.library_management;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

public class AddBookActivity extends AppCompatActivity {

    private MaterialAutoCompleteTextView bookSpinner;
    private TextInputEditText author;
    private AutoCompleteTextView categorySpinner;
    private MaterialButton saveBtn, backBtn;
    private DatabaseHelper db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        initializeViews();
        setupDatabase();
        setupBookSpinner();
        setupCategorySpinner();
        setupClickListeners();
    }

    private void initializeViews() {
        bookSpinner = findViewById(R.id.bookSpinner);
        author = findViewById(R.id.bookAuthor);
        categorySpinner = findViewById(R.id.categorySpinner);
        saveBtn = findViewById(R.id.btnSaveBook);
        backBtn = findViewById(R.id.btnBack);
    }

    private void setupDatabase() {
        db = new DatabaseHelper(this);
    }

    private void setupBookSpinner() {
        // Get the fixed 25 books from BookLibrary (same as home page)
        String[][] fixedBooks = BookLibrary.getFixedBooks();
        java.util.List<String> bookTitles = new java.util.ArrayList<>();

        // Extract just the titles for the dropdown
        for (String[] book : fixedBooks) {
            bookTitles.add(book[0]); // book[0] = title
        }

        // Create adapter for book spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, bookTitles);
        bookSpinner.setAdapter(adapter);

        // Set item selection listener to populate author and category fields
        bookSpinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedBookTitle = (String) parent.getItemAtPosition(position);
            populateFieldsForSelectedBook(selectedBookTitle);
        });

        // Set default selection if books exist
        if (!bookTitles.isEmpty()) {
            bookSpinner.setText(bookTitles.get(0), false);
            populateFieldsForSelectedBook(bookTitles.get(0));
        }
    }

    private void populateFieldsForSelectedBook(String bookTitle) {
        // Get author and category from BookLibrary
        String[] bookInfo = BookLibrary.getBookInfo(bookTitle);
        if (bookInfo != null) {
            String authorName = bookInfo[0];
            String category = bookInfo[1];
            
            // Auto-populate author field
            author.setText(authorName);
            
            // Auto-populate category spinner
            categorySpinner.setText(category, false);
        }
    }

    private void setupCategorySpinner() {
        String[] categories = getResources().getStringArray(R.array.categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        categorySpinner.setAdapter(adapter);

        // Set default selection
        if (categories.length > 0) {
            categorySpinner.setText(categories[0], false);
        }
    }

    private void setupClickListeners() {
        saveBtn.setOnClickListener(v -> {
            animateButtonClick(v);
            saveBook();
        });

        backBtn.setOnClickListener(v -> {
            animateButtonClick(v);
            finish();
        });
    }

    private void animateButtonClick(View view) {
        android.view.animation.Animation animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        animation.setDuration(150);
        view.startAnimation(animation);
    }

    private void saveBook() {
        String selectedBook = bookSpinner.getText().toString().trim();
        String bookAuthor = author.getText().toString().trim();
        String bookCategory = categorySpinner.getText().toString().trim();

        // Validation
        if (selectedBook.isEmpty()) {
            Toast.makeText(this, "Please select a book from the library", Toast.LENGTH_SHORT).show();
            bookSpinner.requestFocus();
            return;
        }

        if (bookAuthor.isEmpty()) {
            Toast.makeText(this, "Author information is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bookCategory.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        // For now, we'll add the selected book as a new entry (could be for multiple copies)
        // In the future, this could be changed to "collect" books differently
        boolean result = db.addBook(selectedBook, bookAuthor, bookCategory);

        if (result) {
            Toast.makeText(this, "Book Collected Successfully!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Failed to collect book. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
