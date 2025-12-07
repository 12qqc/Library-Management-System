package com.example.library_management;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "library.db";

    // TABLE NAMES
    public static final String TABLE_USERS = "users";
    public static final String TABLE_BOOKS = "books";
    public static final String TABLE_BORROW = "borrow";

    // BOOK COLUMNS
    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_AUTHOR = "author";
    public static final String COL_CATEGORY = "category";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // USERS TABLE
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email TEXT, " +
                "password TEXT, " +
                "username TEXT)");

        // BOOKS TABLE
        db.execSQL("CREATE TABLE " + TABLE_BOOKS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "author TEXT, " +
                "category TEXT)");

        // BORROW TABLE
        db.execSQL("CREATE TABLE " + TABLE_BORROW + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "userEmail TEXT, " +
                "bookTitle TEXT, " +
                "borrowDate TEXT, " +
                "dueDate TEXT, " +
                "returnDate TEXT, " +
                "status TEXT DEFAULT 'borrowed')");

        // SAMPLE BOOKS
        ContentValues values = new ContentValues();

        values.put(COL_TITLE, "Harry Potter");
        values.put(COL_AUTHOR, "J.K. Rowling");
        values.put(COL_CATEGORY, "Fantasy");
        db.insert(TABLE_BOOKS, null, values);

        values.put(COL_TITLE, "The Hobbit");
        values.put(COL_AUTHOR, "J.R.R. Tolkien");
        values.put(COL_CATEGORY, "Adventure");
        db.insert(TABLE_BOOKS, null, values);

        values.put(COL_TITLE, "Atomic Habits");
        values.put(COL_AUTHOR, "James Clear");
        values.put(COL_CATEGORY, "Self Help");
        db.insert(TABLE_BOOKS, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add username column to users table
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN username TEXT");
            } catch (Exception e) {
                // Column might already exist, ignore
            }
        }

        // For future versions, we can add more migration logic here
        // For now, we'll keep the existing behavior for other upgrades
        if (oldVersion < newVersion) {
            // Future migration logic can be added here
        }
    }

    // =====================================
    // USER FUNCTIONS
    // =====================================

    public boolean registerUser(String email, String password) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();

            cv.put("email", email);
            cv.put("password", password);

            long result = db.insert(TABLE_USERS, null, cv);
            db.close(); // Always close the database
            return result != -1;
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error registering user", e);
            return false;
        }
    }

    public boolean loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE email=? AND password=?",
                new String[]{email, password}
        );

        return cursor.getCount() > 0;
    }

    // =====================================
    // BOOK FUNCTIONS
    // =====================================

    public boolean addBook(String title, String author, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COL_TITLE, title);
        cv.put(COL_AUTHOR, author);
        cv.put(COL_CATEGORY, category);

        long result = db.insert(TABLE_BOOKS, null, cv);
        return result != -1;
    }

    public Cursor getBooks() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_BOOKS, null);
    }

    // =====================================
    // BORROW FUNCTIONS
    // =====================================

    public boolean borrowBook(String userEmail, String bookTitle, String borrowDate, String dueDate) {
        // First check if the book exists
        if (!bookExists(bookTitle)) {
            android.util.Log.e("DatabaseHelper", "Book does not exist: " + bookTitle);
            return false;
        }

        // Check if user already has this book borrowed
        if (hasBorrowedBook(userEmail, bookTitle)) {
            android.util.Log.e("DatabaseHelper", "User already has this book borrowed: " + userEmail + " - " + bookTitle);
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("userEmail", userEmail);
        cv.put("bookTitle", bookTitle);
        cv.put("borrowDate", borrowDate);
        cv.put("dueDate", dueDate);
        cv.put("status", "borrowed");

        try {
        long result = db.insert(TABLE_BORROW, null, cv);
            android.util.Log.d("DatabaseHelper", "Borrow book result: " + result + " for user: " + userEmail + ", book: " + bookTitle);
        return result != -1;
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error borrowing book", e);
            return false;
        }
    }

    public boolean bookExists(String bookTitle) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BOOKS + " WHERE title=?", new String[]{bookTitle});
        boolean exists = cursor.getCount() > 0;
        android.util.Log.d("DatabaseHelper", "bookExists check: '" + bookTitle + "' -> " + exists + " (count: " + cursor.getCount() + ")");
        cursor.close();
        return exists;
    }

    public boolean hasBorrowedBook(String userEmail, String bookTitle) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BORROW +
                " WHERE userEmail=? AND bookTitle=? AND status='borrowed'",
                new String[]{userEmail, bookTitle});
        boolean hasBorrowed = cursor.getCount() > 0;
        android.util.Log.d("DatabaseHelper", "hasBorrowedBook check: '" + userEmail + "' - '" + bookTitle + "' -> " + hasBorrowed + " (count: " + cursor.getCount() + ")");
        cursor.close();
        return hasBorrowed;
    }

    // Legacy method for backward compatibility
    public boolean borrowBook(String userEmail, String bookTitle, String date) {
        // Default 14 days borrowing period
        String dueDate = calculateDueDate(date, 14);
        return borrowBook(userEmail, bookTitle, date, dueDate);
    }

    public List<String[]> getBorrowedBooks(String userEmail) {
        List<String[]> borrowedBooks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT bookTitle, borrowDate, dueDate FROM " + TABLE_BORROW +
                " WHERE userEmail=? AND status='borrowed' ORDER BY dueDate ASC",
                new String[]{userEmail});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String bookTitle = cursor.getString(cursor.getColumnIndexOrThrow("bookTitle"));
                    String borrowDate = cursor.getString(cursor.getColumnIndexOrThrow("borrowDate"));
                    String dueDate = cursor.getString(cursor.getColumnIndexOrThrow("dueDate"));
                    borrowedBooks.add(new String[]{bookTitle, borrowDate, dueDate});
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error getting borrowed books", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return borrowedBooks;
    }

    public Cursor getBorrowHistory(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_BORROW +
                " WHERE userEmail=? ORDER BY borrowDate DESC",
                new String[]{userEmail});
    }

    public boolean userExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS +
                " WHERE email=?",
                new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Add sample borrowed books for testing the My Books page
    public void addSampleBorrowedBooks(String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if sample books already exist for this user
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BORROW +
                " WHERE userEmail=? AND status='borrowed'",
                new String[]{userEmail});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        if (count == 0) {
            // Add sample borrowed books
            ContentValues values1 = new ContentValues();
            values1.put("userEmail", userEmail);
            values1.put("bookTitle", "The Great Gatsby");
            values1.put("borrowDate", "2024-12-01");
            values1.put("dueDate", "2024-12-15");
            values1.put("status", "borrowed");
            db.insert(TABLE_BORROW, null, values1);

            ContentValues values2 = new ContentValues();
            values2.put("userEmail", userEmail);
            values2.put("bookTitle", "To Kill a Mockingbird");
            values2.put("borrowDate", "2024-12-02");
            values2.put("dueDate", "2024-12-16");
            values2.put("status", "borrowed");
            db.insert(TABLE_BORROW, null, values2);

            ContentValues values3 = new ContentValues();
            values3.put("userEmail", userEmail);
            values3.put("bookTitle", "1984");
            values3.put("borrowDate", "2024-12-03");
            values3.put("dueDate", "2024-12-17");
            values3.put("status", "borrowed");
            db.insert(TABLE_BORROW, null, values3);
        }
    }

    public boolean validateLogin(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS +
                " WHERE email=? AND password=?",
                new String[]{email, password});
        boolean valid = cursor.getCount() > 0;
        cursor.close();
        return valid;
    }

    public boolean returnBook(String userEmail, String bookTitle, String returnDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("returnDate", returnDate);
        cv.put("status", "returned");

        int result = db.update(TABLE_BORROW, cv,
                "userEmail=? AND bookTitle=? AND status='borrowed'",
                new String[]{userEmail, bookTitle});
        return result > 0;
    }

    public int getTotalBooksCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BOOKS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getBorrowedBooksCount(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BORROW +
                " WHERE userEmail=? AND status='borrowed'", new String[]{userEmail});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getOverdueBooksCount(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BORROW +
                " WHERE userEmail=? AND status='borrowed' AND dueDate < date('now')",
                new String[]{userEmail});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    private String calculateDueDate(String startDate, int days) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date date = sdf.parse(startDate);
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(java.util.Calendar.DAY_OF_YEAR, days);
            return sdf.format(calendar.getTime());
        } catch (Exception e) {
            // Fallback: return a date 14 days from today
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.add(java.util.Calendar.DAY_OF_YEAR, days);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            return sdf.format(calendar.getTime());
        }
    }

    // -------------------------
    //   USER PROFILE MANAGEMENT
    // -------------------------

    public String getUsername(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT username FROM " + TABLE_USERS +
                " WHERE email=?", new String[]{email});

        String username = null;
        if (cursor.moveToFirst()) {
            username = cursor.getString(0);
        }
        cursor.close();
        return username;
    }

    public boolean updateUsername(String email, String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", username);

        int result = db.update(TABLE_USERS, cv, "email=?", new String[]{email});
        return result > 0;
    }
}
