package com.booknest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookResource {
    @Autowired private BookService bookService;

    @GetMapping public ResponseEntity<List<Book>> getAll() { return ResponseEntity.ok(bookService.getAllBooks()); }
    @GetMapping("/{id}") public ResponseEntity<Book> getById(@PathVariable int id) {
        return bookService.getBookById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/featured") public ResponseEntity<List<Book>> getFeatured() { return ResponseEntity.ok(bookService.getFeaturedBooks()); }
    @GetMapping("/search") public ResponseEntity<List<Book>> search(@RequestParam String keyword) { return ResponseEntity.ok(bookService.searchBooks(keyword)); }
    @GetMapping("/genre/{genre}") public ResponseEntity<List<Book>> byGenre(@PathVariable String genre) { return ResponseEntity.ok(bookService.getByGenre(genre)); }
    @GetMapping("/author/{author}") public ResponseEntity<List<Book>> byAuthor(@PathVariable String author) { return ResponseEntity.ok(bookService.getByAuthor(author)); }
    @PostMapping public ResponseEntity<Book> add(@RequestBody Book book) { return ResponseEntity.ok(bookService.addBook(book)); }
    @PutMapping("/{id}") public ResponseEntity<Book> update(@PathVariable int id, @RequestBody Book book) { book.setBookId(id); return ResponseEntity.ok(bookService.updateBook(book)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable int id) { bookService.deleteBook(id); return ResponseEntity.ok().build(); }
    @PatchMapping("/{id}/stock") public ResponseEntity<String> updateStock(@PathVariable int id, @RequestBody Map<String, Integer> body) {
        bookService.updateStock(id, body.get("stock")); return ResponseEntity.ok("Stock updated");
    }

    /**
     * PUT /api/books/{id}/decrease-stock?quantity=2
     * Called by order-service after order placement to reduce stock.
     */
    @PutMapping("/{id}/decrease-stock")
    public ResponseEntity<String> decreaseStock(@PathVariable int id,
                                                @RequestParam(defaultValue = "1") int quantity) {
        try {
            bookService.getBookById(id).ifPresent(book -> {
                int newStock = Math.max(0, book.getStock() - quantity);
                bookService.updateStock(id, newStock);
            });
            return ResponseEntity.ok("Stock decreased");
        } catch (Exception e) {
            return ResponseEntity.ok("Stock update skipped: " + e.getMessage());
        }
    }
}
