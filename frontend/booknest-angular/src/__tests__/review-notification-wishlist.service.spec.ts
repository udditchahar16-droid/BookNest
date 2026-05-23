import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ReviewService, NotificationService, WishlistService } from '../app/services/services';

// ═══════════════════════════════════════════════════════════════════
// ReviewService Tests
// ═══════════════════════════════════════════════════════════════════

describe('ReviewService', () => {
  let service: ReviewService;
  let httpMock: HttpTestingController;

  const mockReview = {
    reviewId: 1,
    bookId: 10,
    userId: 1,
    rating: 5,
    comment: 'Excellent read!',
    reviewDate: '2026-05-01',
    verified: true,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule], providers: [ReviewService] });
    service = TestBed.inject(ReviewService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should GET /api/reviews/book/:bookId', () => {
    service.getByBook(10).subscribe(reviews => {
      expect(reviews.length).toBe(1);
      expect(reviews[0].rating).toBe(5);
    });
    const req = httpMock.expectOne('/api/reviews/book/10');
    expect(req.request.method).toBe('GET');
    req.flush([mockReview]);
  });

  it('should GET /api/reviews (getAll)', () => {
    // getAll() hits /api/reviews (not /api/reviews/all)
    service.getAll().subscribe(reviews => expect(reviews.length).toBeGreaterThan(0));
    const req = httpMock.expectOne('/api/reviews');
    req.flush([mockReview]);
  });

  it('should GET /api/reviews/book/:bookId/avg-rating', () => {
    service.getAvgRating(10).subscribe(avg => expect(avg).toBe(4.5));
    const req = httpMock.expectOne('/api/reviews/book/10/avg-rating');
    expect(req.request.method).toBe('GET');
    req.flush(4.5);
  });

  it('should POST /api/reviews to add a review', () => {
    const newReview = { bookId: 10, userId: 1, rating: 4, comment: 'Good book', verified: true };
    service.addReview(newReview).subscribe(r => {
      expect(r.reviewId).toBe(1);
      expect(r.comment).toBe('Excellent read!');
    });
    const req = httpMock.expectOne('/api/reviews');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newReview);
    req.flush(mockReview);
  });

  it('should DELETE /api/reviews/:id', () => {
    service.deleteReview(1).subscribe(() => expect(true).toBe(true));
    const req = httpMock.expectOne('/api/reviews/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should return 403 if non-verified buyer tries to review', () => {
    service.addReview({ bookId: 10, userId: 99, rating: 1, comment: 'Bad', verified: false })
      .subscribe({ error: e => expect(e.status).toBe(403) });
    const req = httpMock.expectOne('/api/reviews');
    req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });
  });

  it('should return empty reviews for book with no reviews', () => {
    service.getByBook(999).subscribe(reviews => expect(reviews).toEqual([]));
    const req = httpMock.expectOne('/api/reviews/book/999');
    req.flush([]);
  });
});

// ═══════════════════════════════════════════════════════════════════
// NotificationService Tests
// ═══════════════════════════════════════════════════════════════════

describe('NotificationService', () => {
  let service: NotificationService;
  let httpMock: HttpTestingController;

  const mockNotification = {
    notificationId: 1,
    userId: 1,
    type: 'ORDER_PLACED',
    message: 'Your order #100 has been placed.',
    isRead: false,
    createdAt: '2026-05-01T12:00:00',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule], providers: [NotificationService] });
    service = TestBed.inject(NotificationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should GET /api/notifications/user/:userId', () => {
    service.getByUser(1).subscribe(notifs => {
      expect(notifs.length).toBe(1);
      expect(notifs[0].type).toBe('ORDER_PLACED');
    });
    const req = httpMock.expectOne('/api/notifications/user/1');
    expect(req.request.method).toBe('GET');
    req.flush([mockNotification]);
  });

  it('should GET /api/notifications/user/:userId/unread-count', () => {
    service.getUnreadCount(1).subscribe(count => expect(count).toBe(3));
    const req = httpMock.expectOne('/api/notifications/user/1/unread-count');
    expect(req.request.method).toBe('GET');
    req.flush(3);
  });

  it('should PUT /api/notifications/user/:userId/read-all', () => {
    service.markAllRead(1).subscribe(() => expect(true).toBe(true));
    const req = httpMock.expectOne('/api/notifications/user/1/read-all');
    expect(req.request.method).toBe('PUT');
    req.flush(null);
  });

  it('should POST /api/notifications/send', () => {
    const payload = { userId: 1, type: 'PAYMENT_SUCCESS', message: 'Payment confirmed.' };
    service.sendNotification(payload).subscribe(n => expect(n.notificationId).toBe(1));
    const req = httpMock.expectOne('/api/notifications/send');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(mockNotification);
  });

  it('should DELETE /api/notifications/:id', () => {
    service.deleteNotification(1).subscribe(() => expect(true).toBe(true));
    const req = httpMock.expectOne('/api/notifications/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should return 0 unread count when all notifications read', () => {
    service.getUnreadCount(1).subscribe(count => expect(count).toBe(0));
    const req = httpMock.expectOne('/api/notifications/user/1/unread-count');
    req.flush(0);
  });
});

// ═══════════════════════════════════════════════════════════════════
// WishlistService Tests
// ═══════════════════════════════════════════════════════════════════

describe('WishlistService', () => {
  let service: WishlistService;
  let httpMock: HttpTestingController;

  const mockWishlist = {
    wishlistId: 1,
    userId: 1,
    createdAt: '2026-01-01',
    books: [{ itemId: 1, bookId: 10, bookTitle: 'Clean Code', bookPrice: 599 }],
  };

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule], providers: [WishlistService] });
    service = TestBed.inject(WishlistService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should GET /api/wishlist/:userId', () => {
    service.getWishlist(1).subscribe(w => {
      expect(w.books.length).toBe(1);
      expect(w.books[0].bookTitle).toBe('Clean Code');
    });
    const req = httpMock.expectOne('/api/wishlist/1');
    expect(req.request.method).toBe('GET');
    req.flush(mockWishlist);
  });

  it('should POST /api/wishlist/:userId/add/:bookId to add a book', () => {
    // addBook hits /api/wishlist/:userId/add/:bookId with empty body
    const item = { bookId: 20, bookTitle: 'Design Patterns', bookPrice: 799 };
    service.addBook(1, item).subscribe(w => expect(w.books.length).toBeGreaterThanOrEqual(1));
    const req = httpMock.expectOne('/api/wishlist/1/add/20');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    req.flush({ ...mockWishlist, books: [...mockWishlist.books, { itemId: 2, ...item }] });
  });

  it('should DELETE /api/wishlist/:userId/remove/:bookId', () => {
    service.removeBook(1, 10).subscribe(() => expect(true).toBe(true));
    const req = httpMock.expectOne('/api/wishlist/1/remove/10');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should DELETE /api/wishlist/:userId/clear', () => {
    service.clearWishlist(1).subscribe(() => expect(true).toBe(true));
    const req = httpMock.expectOne('/api/wishlist/1/clear');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should return empty wishlist for new user', () => {
    service.getWishlist(99).subscribe(w => expect(w.books.length).toBe(0));
    const req = httpMock.expectOne('/api/wishlist/99');
    req.flush({ ...mockWishlist, books: [] });
  });

  it('should POST /api/wishlist/:userId/move-to-cart/:bookId', () => {
    service.moveToCart(1, 10).subscribe(res => expect(res).toBeTruthy());
    const req = httpMock.expectOne('/api/wishlist/1/move-to-cart/10');
    expect(req.request.method).toBe('POST');
    req.flush('Moved to cart');
  });
});
