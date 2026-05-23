package com.booknest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {
    List<Book> findByTitle(String title);
    List<Book> findByAuthor(String author);
    List<Book> findByGenre(String genre);
    Optional<Book> findByIsbn(String isbn);
    List<Book> findByPriceBetween(double min, double max);
    List<Book> findByStockGreaterThan(int stock);
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%',:kw,'%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%',:kw,'%')) OR LOWER(b.genre) LIKE LOWER(CONCAT('%',:kw,'%')) OR LOWER(b.isbn) LIKE LOWER(CONCAT('%',:kw,'%')) OR LOWER(b.publisher) LIKE LOWER(CONCAT('%',:kw,'%'))")
    List<Book> searchByKeyword(@Param("kw") String keyword);
}
