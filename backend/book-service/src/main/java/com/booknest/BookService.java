package com.booknest;

import java.util.List;
import java.util.Optional;

public interface BookService {
    Book addBook(Book book);
    List<Book> getAllBooks();
    Optional<Book> getBookById(int id);
    List<Book> searchBooks(String keyword);
    List<Book> getByGenre(String genre);
    List<Book> getByAuthor(String author);
    Book updateBook(Book book);
    void deleteBook(int id);
    void updateStock(int bookId, int newStock);
    List<Book> getFeaturedBooks();
}
