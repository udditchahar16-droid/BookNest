package com.booknest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByBookId(int bookId);
    List<Review> findByUserId(int userId);
    Optional<Review> findByBookIdAndUserId(int bookId, int userId);
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.bookId = :id") Double avgRatingByBookId(@Param("id") int id);
    int countByBookId(int bookId);
    void deleteByReviewId(int reviewId);
}
