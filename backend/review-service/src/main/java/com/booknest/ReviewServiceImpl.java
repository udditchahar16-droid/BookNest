package com.booknest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired private ReviewRepository reviewRepository;

    @Override
    public Review addReview(Review review) {
        if (review.getRating() < 1 || review.getRating() > 5)
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        review.setVerified(true);
        review.setReviewDate(LocalDate.now());
        return reviewRepository.save(review);
    }

    @Override public List<Review> getByBook(int bookId)    { return reviewRepository.findByBookId(bookId); }
    @Override public List<Review> getByUser(int userId)    { return reviewRepository.findByUserId(userId); }
    @Override public Review updateReview(int id, Review r) { r.setReviewId(id); return reviewRepository.save(r); }
    @Override @Transactional public void deleteReview(int id) { reviewRepository.deleteByReviewId(id); }
    @Override public double getAvgRating(int bookId)        { Double a = reviewRepository.avgRatingByBookId(bookId); return a != null ? a : 0.0; }
    @Override public List<Review> getAllReviews()           { return reviewRepository.findAll(); }
    @Override public Optional<Review> getReviewById(int id){ return reviewRepository.findById(id); }
}