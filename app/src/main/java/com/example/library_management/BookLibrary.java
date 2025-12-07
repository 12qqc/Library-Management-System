package com.example.library_management;

/**
 * Shared book library data containing the fixed collection of 25 books
 * used across the application (home page display and book selection dropdown)
 */
public class BookLibrary {
    
    /**
     * Returns the fixed collection of 25 books
     * Format: {title, author, category}
     */
    public static String[][] getFixedBooks() {
        return new String[][]{
            {"The Great Gatsby", "F. Scott Fitzgerald", "Fiction"},
            {"To Kill a Mockingbird", "Harper Lee", "Fiction"},
            {"1984", "George Orwell", "Science Fiction"},
            {"Pride and Prejudice", "Jane Austen", "Romance"},
            {"The Catcher in the Rye", "J.D. Salinger", "Fiction"},
            {"Harry Potter and the Sorcerer's Stone", "J.K. Rowling", "Fantasy"},
            {"The Lord of the Rings", "J.R.R. Tolkien", "Fantasy"},
            {"The Alchemist", "Paulo Coelho", "Philosophy"},
            {"Sapiens: A Brief History of Humankind", "Yuval Noah Harari", "History"},
            {"Thinking, Fast and Slow", "Daniel Kahneman", "Psychology"},
            {"The Hitchhiker's Guide to the Galaxy", "Douglas Adams", "Science Fiction"},
            {"Dune", "Frank Herbert", "Science Fiction"},
            {"The Name of the Wind", "Patrick Rothfuss", "Fantasy"},
            {"Educated", "Tara Westover", "Memoir"},
            {"Becoming", "Michelle Obama", "Biography"},
            {"Atomic Habits", "James Clear", "Self-Help"},
            {"The Subtle Art of Not Giving a F*ck", "Mark Manson", "Self-Help"},
            {"Clean Code", "Robert C. Martin", "Technology"},
            {"Design Patterns", "Gang of Four", "Technology"},
            {"The Pragmatic Programmer", "Andrew Hunt", "Technology"},
            {"JavaScript: The Good Parts", "Douglas Crockford", "Technology"},
            {"You Don't Know JS", "Kyle Simpson", "Technology"},
            {"Cracking the Coding Interview", "Gayle Laakmann McDowell", "Technology"},
            {"The Phoenix Project", "Gene Kim", "Business"},
            {"The Lean Startup", "Eric Ries", "Business"}
        };
    }
    
    /**
     * Formats a book entry for display in the list
     * Format: "📖 {title} - {author} ({category})"
     */
    public static String formatBookForDisplay(String title, String author, String category) {
        return "📖 " + title + " - " + author + " (" + category + ")";
    }
    
    /**
     * Gets book information by title
     * Returns: {author, category} or null if not found
     */
    public static String[] getBookInfo(String title) {
        String[][] books = getFixedBooks();
        for (String[] book : books) {
            if (book[0].equals(title)) {
                return new String[]{book[1], book[2]}; // {author, category}
            }
        }
        return null;
    }
}







