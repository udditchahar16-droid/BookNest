import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BookService } from '../../services/book.service';
import { CartService, ReviewService, WishlistService, NotificationService } from '../../services/services';
import { AuthService } from '../../services/auth.service';
import { Book, Review, User } from '../../models/models';

@Component({
  selector: 'app-book-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="container" style="padding-top:32px;padding-bottom:48px">
      <a routerLink="/books" class="back-link"><i class="fas fa-arrow-left"></i> Back to Books</a>

      <div class="loading" *ngIf="loading"><div class="spinner"></div> Loading book...</div>

      <div class="detail-layout" *ngIf="!loading && book">
        <!-- Left: Cover -->
        <div class="cover-col">
          <div class="book-cover-large">
            <img [src]="book.coverImageUrl || 'https://placehold.co/300x420/2c3e6b/white?text=📚'" [alt]="book.title" onerror="this.src='https://placehold.co/300x420/2c3e6b/white?text=📚'">
            <div class="featured-badge" *ngIf="book.featured">⭐ Featured</div>
          </div>
        </div>

        <!-- Right: Info -->
        <div class="info-col">
          <div class="genre-tag" *ngIf="book.genre">{{book.genre}}</div>
          <h1>{{book.title}}</h1>
          <p class="author">by <a [routerLink]="['/books/author', book.author]">{{book.author}}</a></p>

          <div class="rating-row" *ngIf="avgRating">
            <span class="star" *ngFor="let s of getStars(avgRating)">★</span>
            <span class="empty-star" *ngFor="let s of getEmptyStars(avgRating)">☆</span>
            <span class="rating-val">{{avgRating | number:'1.1-1'}}</span>
            <span class="review-count">({{reviews.length}} reviews)</span>
          </div>

          <div class="price-row">
            <span class="price">₹{{book.price}}</span>
            <span class="stock-badge" [class.out]="book.stock === 0">
              {{book.stock > 0 ? book.stock + ' in stock' : 'Out of Stock'}}
            </span>
          </div>

          <p class="description">{{book.description || 'No description available.'}}</p>

          <div class="specs">
            <div class="spec" *ngIf="book.publisher"><span>Publisher</span><strong>{{book.publisher}}</strong></div>
            <div class="spec" *ngIf="book.isbn"><span>ISBN</span><strong>{{book.isbn}}</strong></div>
            <div class="spec" *ngIf="book.publishedDate"><span>Published</span><strong>{{book.publishedDate | date}}</strong></div>
          </div>

          <div class="actions" *ngIf="book.stock > 0">
            <div class="qty-row">
              <label>Qty:</label>
              <select [(ngModel)]="qty">
                <option *ngFor="let n of getQuantityOptions()" [value]="n">{{n}}</option>
              </select>
            </div>
            <button class="btn btn-primary btn-lg" (click)="addToCart()" [disabled]="adding">
              <i class="fas fa-shopping-cart"></i> {{adding ? 'Adding...' : 'Add to Cart'}}
            </button>
            <button class="btn btn-outline" (click)="addToWishlist()">
              <i class="fas fa-heart"></i> Wishlist
            </button>
          </div>

          <div class="alert alert-success" *ngIf="cartMsg" style="margin-top:12px">{{cartMsg}}</div>
          <div class="alert alert-error" *ngIf="cartErr" style="margin-top:12px">{{cartErr}}</div>
          <div class="alert alert-warning" *ngIf="!isLoggedIn && book.stock > 0" style="margin-top:12px">
            <a routerLink="/auth/login">Login</a> to add to cart
          </div>
        </div>
      </div>

      <!-- Reviews -->
      <div class="reviews-section" *ngIf="book">
        <h2>Customer Reviews</h2>

        <!-- Write review -->
        <div class="write-review card" *ngIf="isLoggedIn">
          <h3>Write a Review</h3>
          <div class="star-picker">
            <span *ngFor="let s of [1,2,3,4,5]"
                  class="star-btn" [class.selected]="review.rating >= s"
                  (click)="review.rating = s">★</span>
          </div>
          <div class="form-group" style="margin-top:12px">
            <textarea [(ngModel)]="review.comment" placeholder="Share your thoughts about this book..." rows="3"></textarea>
          </div>
          <button class="btn btn-primary" (click)="submitReview()" [disabled]="!review.rating || !review.comment">
            Submit Review
          </button>
        </div>

        <div class="review-list">
          <div class="review-card" *ngFor="let r of reviews">
            <div class="review-header">
              <div class="reviewer-avatar">{{getReviewerInitials(r.userId)}}</div>
              <div>
                <div class="reviewer-name">{{getReviewerName(r.userId)}}</div>
                <div class="stars">
                  <span class="star" *ngFor="let s of getStars(r.rating)">★</span>
                </div>
                <span class="review-date">{{r.reviewDate | date:'mediumDate'}}</span>
              </div>
            </div>
            <p class="review-text">{{r.comment}}</p>
          </div>
          <div class="empty-state" *ngIf="!reviews.length">
            <i class="fas fa-comment"></i>
            <h3>No reviews yet</h3>
            <p>Be the first to review this book!</p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .back-link { display: inline-flex; align-items: center; gap: 8px; color: var(--text-muted); font-size: 14px; margin-bottom: 24px; text-decoration: none; &:hover { color: var(--primary); } }
    .detail-layout { display: grid; grid-template-columns: 300px 1fr; gap: 40px; margin-bottom: 48px; }
    .book-cover-large { position: relative; border-radius: 12px; overflow: hidden; box-shadow: var(--shadow-lg); img { width: 100%; display: block; } }
    .featured-badge { position: absolute; top: 12px; left: 12px; background: var(--accent); color: white; padding: 4px 12px; border-radius: 20px; font-size: 13px; font-weight: 600; }
    .genre-tag { display: inline-block; background: #e8f0fe; color: var(--primary); padding: 4px 12px; border-radius: 20px; font-size: 13px; font-weight: 500; margin-bottom: 12px; }
    .info-col h1 { font-size: 2rem; color: var(--primary); margin-bottom: 8px; }
    .author { font-size: 16px; color: var(--text-muted); margin-bottom: 16px; a { color: var(--accent); &:hover { text-decoration: underline; } } }
    .rating-row { display: flex; align-items: center; gap: 4px; margin-bottom: 16px; }
    .star { color: #f6ad55; font-size: 18px; }
    .empty-star { color: #e2e8f0; font-size: 18px; }
    .rating-val { font-size: 15px; font-weight: 600; color: var(--text); margin-left: 6px; }
    .review-count { font-size: 14px; color: var(--text-muted); }
    .price-row { display: flex; align-items: center; gap: 16px; margin-bottom: 16px; }
    .price { font-size: 2rem; font-weight: 700; color: var(--primary); }
    .stock-badge { font-size: 13px; font-weight: 600; color: var(--success); &.out { color: var(--error); } }
    .description { color: var(--text-muted); line-height: 1.8; margin-bottom: 20px; }
    .specs { display: flex; flex-direction: column; gap: 8px; padding: 16px; background: var(--bg); border-radius: 8px; margin-bottom: 24px; }
    .spec { display: flex; justify-content: space-between; font-size: 14px; span { color: var(--text-muted); } strong { color: var(--text); } }
    .actions { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
    .qty-row { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 500; select { padding: 8px 12px; border: 1px solid var(--border); border-radius: 8px; font-size: 14px; } }
    .reviews-section { margin-top: 16px; h2 { font-size: 1.6rem; color: var(--primary); margin-bottom: 24px; } }
    .write-review { padding: 24px; margin-bottom: 24px; h3 { font-family: 'Inter', sans-serif; font-size: 16px; margin-bottom: 12px; } }
    .star-picker { display: flex; gap: 8px; }
    .star-btn { font-size: 28px; cursor: pointer; color: #e2e8f0; transition: color 0.1s; &.selected { color: #f6ad55; } &:hover { color: #f6ad55; } }
    .review-list { display: flex; flex-direction: column; gap: 16px; }
    .review-card { background: white; border: 1px solid var(--border); border-radius: 12px; padding: 20px; }
    .review-header { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
    .reviewer-avatar { width: 40px; height: 40px; border-radius: 50%; background: var(--primary); color: white; font-size: 13px; display: flex; align-items: center; justify-content: center; font-weight: 700; flex-shrink: 0; }
    .reviewer-name { font-size: 14px; font-weight: 600; color: var(--text); margin-bottom: 2px; }
    .review-date { font-size: 12px; color: var(--text-muted); }
    .review-text { font-size: 14px; line-height: 1.7; color: var(--text); }
    @media(max-width:768px){ .detail-layout { grid-template-columns: 1fr; } }
  `]
})
export class BookDetailComponent implements OnInit {
  book: Book | null = null;
  reviews: Review[] = [];
  avgRating = 0;
  loading = true;
  qty = 1;
  adding = false;
  cartMsg = ''; cartErr = '';
  isLoggedIn = false;
  review = { rating: 0, comment: '' };
  private userMap = new Map<number, User>();

  constructor(
    private route: ActivatedRoute,
    private bookSvc: BookService,
    private cartSvc: CartService,
    private reviewSvc: ReviewService,
    private wishlistSvc: WishlistService,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.isLoggedIn = this.auth.isLoggedIn;
    const id = Number(this.route.snapshot.params['id']);
    this.bookSvc.getById(id).subscribe({ next: b => { this.book = b; this.loading = false; }, error: () => this.loading = false });
    this.reviewSvc.getByBook(id).subscribe({
      next: r => {
        this.reviews = r;
        // Load usernames for all reviewers
        const userIds = [...new Set(r.map(rv => rv.userId))];
        userIds.forEach(uid => {
          this.auth.getUser(uid).subscribe({
            next: u => this.userMap.set(uid, u),
            error: () => {}
          });
        });
      },
      error: () => {}
    });
    this.reviewSvc.getAvgRating(id).subscribe({ next: r => this.avgRating = r || 0, error: () => {} });
  }

  getReviewerName(userId: number): string {
    const u = this.userMap.get(userId);
    return u ? u.fullName : `User #${userId}`;
  }

  getReviewerInitials(userId: number): string {
    const u = this.userMap.get(userId);
    if (u && u.fullName) {
      return u.fullName.split(' ').map((w: string) => w[0]).join('').toUpperCase().slice(0, 2);
    }
    return 'U';
  }

  addToCart() {
    if (!this.auth.isLoggedIn) { this.router.navigate(['/auth/login']); return; }
    this.adding = true; this.cartMsg = ''; this.cartErr = '';
    const uid = this.auth.currentUser!.userId;
    this.cartSvc.addItem(uid, { bookId: this.book!.bookId, bookTitle: this.book!.title, price: this.book!.price, quantity: this.qty }).subscribe({
      next: () => { this.adding = false; this.cartMsg = 'Added to cart!'; setTimeout(() => this.cartMsg = '', 3000); },
      error: () => { this.adding = false; this.cartErr = 'Could not add to cart.'; }
    });
  }

  addToWishlist() {
    if (!this.auth.isLoggedIn) { this.router.navigate(['/auth/login']); return; }
    const uid = this.auth.currentUser!.userId;
    this.wishlistSvc.addBook(uid, { bookId: this.book!.bookId, bookTitle: this.book!.title, bookPrice: this.book!.price }).subscribe({
      next: () => { this.cartMsg = 'Added to wishlist!'; setTimeout(() => this.cartMsg = '', 3000); },
      error: () => {}
    });
  }

  submitReview() {
    const uid = this.auth.currentUser!.userId;
    this.reviewSvc.addReview({ bookId: this.book!.bookId, userId: uid, rating: this.review.rating, comment: this.review.comment, verified: true }).subscribe({
      next: r => {
        this.reviews.unshift(r);
        this.review = { rating: 0, comment: '' };
        // Add current user to map so name shows immediately
        const currentUser = this.auth.currentUser!;
        this.userMap.set(uid, currentUser);
      },
      error: () => {}
    });
  }

  getStars(r: number): number[] { return Array(Math.round(r)).fill(0); }
  getEmptyStars(r: number): number[] { return Array(5 - Math.round(r)).fill(0); }
  getQuantityOptions(): number[] {
    const max = Math.min(this.book?.stock || 20, 20);
    return Array.from({ length: max }, (_, i) => i + 1);
  }
}
