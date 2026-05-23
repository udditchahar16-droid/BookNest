import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { WishlistService, CartService } from '../../services/services';
import { AuthService } from '../../services/auth.service';
import { Wishlist } from '../../models/models';
import { BookService } from '../../services/book.service';

@Component({
  selector: 'app-wishlist',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container">
      <div class="page-header">
        <h1><i class="fas fa-heart"></i> My Wishlist</h1>
        <p *ngIf="wishlist">{{wishlist.books?.length || 0}} items saved</p>
      </div>

      <div class="loading" *ngIf="loading"><div class="spinner"></div></div>

      <div class="wishlist-grid" *ngIf="!loading && wishlist?.books?.length">
        <div class="wish-card card" *ngFor="let item of wishlist!.books">
          <div class="wish-info">
            <div class="wish-thumb"><i class="fas fa-book"></i></div>
            <div>
              <h3>{{item.bookTitle}}</h3>
              <p class="wish-price">₹{{item.bookPrice}}</p>
            </div>
          </div>
          <div class="wish-actions">
            <button class="btn btn-primary btn-sm" (click)="moveToCart(item.bookId, item.bookTitle, item.bookPrice)">
              <i class="fas fa-cart-plus"></i> Move to Cart
            </button>
            <button class="btn btn-danger btn-sm" (click)="remove(item.bookId)">
              <i class="fas fa-trash"></i>
            </button>
          </div>
        </div>
      </div>

      <div class="empty-state" *ngIf="!loading && (!wishlist || !wishlist.books?.length)">
        <i class="fas fa-heart"></i>
        <h3>Your wishlist is empty</h3>
        <p>Save books you'd like to buy later</p>
        <a routerLink="/books" class="btn btn-primary" style="margin-top:16px">Browse Books</a>
      </div>
    </div>
  `,
  styles: [`
    .wishlist-grid { display: flex; flex-direction: column; gap: 12px; padding-bottom: 48px; }
    .wish-card { display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
    .wish-info { display: flex; align-items: center; gap: 16px; flex: 1; }
    .wish-thumb { width: 48px; height: 48px; background: var(--bg); border-radius: 8px; display: flex; align-items: center; justify-content: center; color: var(--primary); font-size: 18px; }
    .wish-info h3 { font-size: 15px; font-weight: 600; font-family: 'Inter', sans-serif; }
    .wish-price { font-size: 14px; color: var(--accent); font-weight: 600; margin-top: 4px; }
    .wish-actions { display: flex; gap: 8px; }
  `]
})
export class WishlistComponent implements OnInit {
  wishlist: Wishlist | null = null;
  loading = true;
  userId!: number;

  constructor(private wishSvc: WishlistService, private cartSvc: CartService, private auth: AuthService, private bookSvc: BookService) {}

  ngOnInit() {
    this.userId = this.auth.currentUser!.userId;
    this.load();
  }

  load() {
    this.wishSvc.getWishlist(this.userId).subscribe({ next: w => { this.wishlist = w; this.loading = false; }, error: () => this.loading = false });
  }

  remove(bookId: number) {
    this.wishSvc.removeBook(this.userId, bookId).subscribe({ next: () => this.load(), error: () => {} });
  }

  moveToCart(bookId: number, bookTitle: string, bookPrice: number) {
    this.cartSvc.addItem(this.userId, { bookId, bookTitle, price: bookPrice, quantity: 1 }).subscribe({
      next: () => this.remove(bookId),
      error: () => {}
    });
  }
}
