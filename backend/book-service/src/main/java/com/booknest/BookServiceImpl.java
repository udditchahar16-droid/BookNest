package com.booknest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {
    @Autowired private BookRepository bookRepository;

    @Override public Book addBook(Book book) { return bookRepository.save(book); }
    @Override public List<Book> getAllBooks() { return bookRepository.findAll(); }
    @Override public Optional<Book> getBookById(int id) { return bookRepository.findById(id); }
    @Override public List<Book> searchBooks(String keyword) { return bookRepository.searchByKeyword(keyword); }
    @Override public List<Book> getByGenre(String genre) { return bookRepository.findByGenre(genre); }
    @Override public List<Book> getByAuthor(String author) { return bookRepository.findByAuthor(author); }
    @Override public Book updateBook(Book book) { return bookRepository.save(book); }
    @Override public void deleteBook(int id) { bookRepository.deleteById(id); }
    @Override public void updateStock(int bookId, int newStock) {
        Book b = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found: " + bookId));
        b.setStock(newStock);
        bookRepository.save(b);
    }
    @Override public List<Book> getFeaturedBooks() {
        return bookRepository.findAll().stream().filter(Book::isFeatured).collect(Collectors.toList());
    }
}
