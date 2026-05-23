package com.booknest;
import java.util.List;
import java.util.Optional;
public interface ReviewService {
    Review addReview(Review review);
    List<Review> getByBook(int bookId);
    List<Review> getByUser(int userId);
    Review updateReview(int id, Review review);
    void deleteReview(int id);
    double getAvgRating(int bookId);
    List<Review> getAllReviews();
    Optional<Review> getReviewById(int id);
}
