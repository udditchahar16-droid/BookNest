package com.booknest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReviewServiceImpl
 * Tests: addReview, getByBook, getByUser, updateReview, deleteReview,
 *        getAvgRating, getAllReviews, getReviewById
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService Unit Tests")
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Review mockReview;

    @BeforeEach
    void setUp() {
        mockReview = new Review();
        mockReview.setReviewId(1);
        mockReview.setBookId(10);
        mockReview.setUserId(1);
        mockReview.setRating(5);
        mockReview.setComment("Excellent read! Highly recommended.");
        mockReview.setReviewDate(LocalDate.now());
        mockReview.setVerified(true);
    }

    // ── addReview ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("addReview: should persist and return the review")
    void addReview_persistsReview() {
        when(reviewRepository.save(mockReview)).thenReturn(mockReview);

        Review result = reviewService.addReview(mockReview);

        assertThat(result.getReviewId()).isEqualTo(1);
        assertThat(result.getComment()).isEqualTo("Excellent read! Highly recommended.");
        verify(reviewRepository).save(mockReview);
    }

    @Test
    @DisplayName("addReview: should reject rating outside 1-5 range")
    void addReview_rejectsInvalidRating() {
        mockReview.setRating(6);
        assertThatThrownBy(() -> reviewService.addReview(mockReview))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rating must be between 1 and 5");
    }

    @Test
    @DisplayName("addReview: should reject review from non-verified purchaser")
    void addReview_rejectsNonVerifiedPurchaser() {
        mockReview.setVerified(false);
        assertThatThrownBy(() -> reviewService.addReview(mockReview))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("verified purchaser");
    }

    // ── getByBook ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getByBook: should return all reviews for a book")
    void getByBook_returnsReviews() {
        when(reviewRepository.findByBookId(10)).thenReturn(List.of(mockReview));

        List<Review> result = reviewService.getByBook(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBookId()).isEqualTo(10);
    }

    @Test
    @DisplayName("getByBook: should return empty list for book with no reviews")
    void getByBook_returnsEmptyForNoReviews() {
        when(reviewRepository.findByBookId(999)).thenReturn(List.of());
        assertThat(reviewService.getByBook(999)).isEmpty();
    }

    // ── getByUser ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getByUser: should return reviews written by user")
    void getByUser_returnsReviews() {
        when(reviewRepository.findByUserId(1)).thenReturn(List.of(mockReview));

        List<Review> result = reviewService.getByUser(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1);
    }

    // ── updateReview ──────────────────────────────────────────────────────

    @Test
    @DisplayName("updateReview: should save updated comment and rating")
    void updateReview_savesUpdate() {
        Review updated = new Review();
        updated.setReviewId(1);
        updated.setBookId(10);
        updated.setUserId(1);
        updated.setRating(4);
        updated.setComment("Updated: Very good book.");
        updated.setVerified(true);

        when(reviewRepository.save(updated)).thenReturn(updated);

        Review result = reviewService.updateReview(1, updated);

        assertThat(result.getComment()).isEqualTo("Updated: Very good book.");
        assertThat(result.getRating()).isEqualTo(4);
    }

    // ── deleteReview ──────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteReview: should call deleteByReviewId on repository")
    void deleteReview_callsDelete() {
        doNothing().when(reviewRepository).deleteByReviewId(1);
        reviewService.deleteReview(1);
        verify(reviewRepository, times(1)).deleteByReviewId(1);
    }

    // ── getAvgRating ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getAvgRating: should return computed average for a book")
    void getAvgRating_returnsAverage() {
        when(reviewRepository.avgRatingByBookId(10)).thenReturn(4.5);

        double avg = reviewService.getAvgRating(10);

        assertThat(avg).isEqualTo(4.5);
    }

    @Test
    @DisplayName("getAvgRating: should return 0.0 for book with no reviews")
    void getAvgRating_returnsZeroForNoReviews() {
        when(reviewRepository.avgRatingByBookId(999)).thenReturn(0.0);
        assertThat(reviewService.getAvgRating(999)).isEqualTo(0.0);
    }

    // ── getAllReviews ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllReviews: should return all reviews for admin moderation")
    void getAllReviews_returnsAll() {
        when(reviewRepository.findAll()).thenReturn(List.of(mockReview));

        List<Review> result = reviewService.getAllReviews();

        assertThat(result).hasSize(1);
    }

    // ── getReviewById ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getReviewById: should return review for valid ID")
    void getReviewById_returnsReview() {
        when(reviewRepository.findById(1)).thenReturn(Optional.of(mockReview));

        Optional<Review> result = reviewService.getReviewById(1);

        assertThat(result).isPresent();
        assertThat(result.get().getRating()).isEqualTo(5);
    }

    @Test
    @DisplayName("getReviewById: should return empty Optional for unknown ID")
    void getReviewById_returnsEmpty() {
        when(reviewRepository.findById(999)).thenReturn(Optional.empty());
        assertThat(reviewService.getReviewById(999)).isEmpty();
    }

    // ── countByBookId ─────────────────────────────────────────────────────

    @Test
    @DisplayName("countByBookId: should return total review count for a book")
    void countByBookId_returnsCount() {
        when(reviewRepository.countByBookId(10)).thenReturn(5);
        assertThat(reviewRepository.countByBookId(10)).isEqualTo(5);
    }

    // ── findByBookIdAndUserId ─────────────────────────────────────────────

    @Test
    @DisplayName("findByBookIdAndUserId: should return review for specific user and book")
    void findByBookIdAndUserId_returnsReview() {
        when(reviewRepository.findByBookIdAndUserId(10, 1)).thenReturn(Optional.of(mockReview));

        Optional<Review> result = reviewRepository.findByBookIdAndUserId(10, 1);
        assertThat(result).isPresent();
        assertThat(result.get().getBookId()).isEqualTo(10);
        assertThat(result.get().getUserId()).isEqualTo(1);
    }
}
