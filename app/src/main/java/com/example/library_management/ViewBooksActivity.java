package com.example.library_management;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ViewBooksActivity extends AppCompatActivity {

    private ListView listView;
    private TextInputEditText searchEditText;
    private Toolbar toolbar;
    private DatabaseHelper db;

    private ArrayAdapter<String> adapter;
    private List<String> allBooks;
    private List<String> filteredBooks;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_books);

        initializeViews();
        setupToolbar();
        setupDatabase();
        loadBooksData();
        setupSearch();
    }

    private void initializeViews() {
        listView = findViewById(R.id.listBooks);
        searchEditText = findViewById(R.id.searchEditText);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupDatabase() {
        db = new DatabaseHelper(this);
    }

    private void loadBooksData() {
        Cursor cursor = db.getBooks();
        allBooks = new ArrayList<>();
        filteredBooks = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                String title = cursor.getString(1);
                String author = cursor.getString(2);
                String category = cursor.getString(3);

                // Create a beautiful formatted string for each book
                String bookEntry = "[BOOK] " + title + "\n[Author] " + author + "\n[Category] " + category;
                allBooks.add(bookEntry);
            } while (cursor.moveToNext());
        }
        cursor.close();

        filteredBooks.addAll(allBooks);

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, filteredBooks) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setSingleLine(false);
                textView.setMaxLines(3);
                textView.setMinLines(3);
                textView.setLineSpacing(4, 1.0f);
                textView.setPadding(16, 16, 16, 16);
                return textView;
            }
        };
        listView.setAdapter(adapter);
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
            for (String book : allBooks) {
                if (book.toLowerCase().contains(lowerQuery)) {
                    filteredBooks.add(book);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
