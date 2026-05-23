package com.booknest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewResource {

    @Autowired private ReviewService reviewService;

    @GetMapping
    public List<Review> getAllReviews() { return reviewService.getAllReviews(); }

    @GetMapping("/book/{bookId}")
    public List<Review> getByBook(@PathVariable int bookId) {
        return reviewService.getByBook(bookId);
    }

    @GetMapping("/user/{userId}")
    public List<Review> getByUser(@PathVariable int userId) {
        return reviewService.getByUser(userId);
    }

    @GetMapping("/book/{bookId}/rating")
    public double getAvgRating(@PathVariable int bookId) {
        return reviewService.getAvgRating(bookId);
    }

    // Alias: Angular frontend calls /avg-rating, backend exposes both
    @GetMapping("/book/{bookId}/avg-rating")
    public double getAvgRatingAlias(@PathVariable int bookId) {
        return reviewService.getAvgRating(bookId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getById(@PathVariable int id) {
        return reviewService.getReviewById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Review> addReview(@RequestBody Review review) {
        return ResponseEntity.ok(reviewService.addReview(review));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable int id,
                                               @RequestBody Review review) {
        return ResponseEntity.ok(reviewService.updateReview(id, review));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable int id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
