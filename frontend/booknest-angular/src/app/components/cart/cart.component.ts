import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CartService } from '../../services/services';
import { AuthService } from '../../services/auth.service';
import { Cart } from '../../models/models';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container">
      <div class="page-header">
        <h1><i class="fas fa-shopping-cart"></i> My Cart</h1>
        <p *ngIf="cart">{{cart.items?.length || 0}} items</p>
      </div>

      <div class="loading" *ngIf="loading"><div class="spinner"></div> Loading cart...</div>

      <div class="cart-layout" *ngIf="!loading && cart && cart.items?.length">
        <div class="cart-items">
          <div class="cart-item card" *ngFor="let item of cart.items">
            <div class="item-info">
              <div class="item-thumb"><i class="fas fa-book"></i></div>
              <div class="item-details">
                <h3>{{item.bookTitle}}</h3>
                <p class="item-price">₹{{item.price}} each</p>
              </div>
            </div>
            <div class="item-controls">
              <div class="qty-controls">
                <button class="qty-btn" (click)="updateQty(item.itemId, item.quantity - 1)" [disabled]="item.quantity <= 1">−</button>
                <span class="qty">{{item.quantity}}</span>
                <button class="qty-btn" (click)="updateQty(item.itemId, item.quantity + 1)">+</button>
              </div>
              <span class="subtotal">₹{{item.price * item.quantity | number:'1.2-2'}}</span>
              <button class="btn btn-danger btn-sm" (click)="removeItem(item.itemId)">
                <i class="fas fa-trash"></i>
              </button>
            </div>
          </div>
        </div>

        <div class="cart-summary card">
          <h3>Order Summary</h3>
          <div class="summary-row" *ngFor="let item of cart.items">
            <span>{{item.bookTitle}} × {{item.quantity}}</span>
            <span>₹{{item.price * item.quantity | number:'1.2-2'}}</span>
          </div>
          <div class="summary-divider"></div>
          <div class="summary-total">
            <span>Total</span>
            <span>₹{{cart.totalPrice | number:'1.2-2'}}</span>
          </div>
          <a routerLink="/checkout" class="btn btn-accent btn-lg checkout-btn">
            <i class="fas fa-lock"></i> Proceed to Checkout
          </a>
          <button class="btn btn-outline clear-btn" (click)="clearCart()">Clear Cart</button>
        </div>
      </div>

      <div class="empty-state" *ngIf="!loading && (!cart || !cart.items?.length)">
        <i class="fas fa-shopping-cart"></i>
        <h3>Your cart is empty</h3>
        <p>Discover books and add them to your cart</p>
        <a routerLink="/books" class="btn btn-primary" style="margin-top:16px">Browse Books</a>
      </div>
    </div>
  `,
  styles: [`
    .cart-layout { display: grid; grid-template-columns: 1fr 340px; gap: 24px; padding-bottom: 48px; }
    .cart-items { display: flex; flex-direction: column; gap: 12px; }
    .cart-item { display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
    .item-info { display: flex; align-items: center; gap: 16px; flex: 1; }
    .item-thumb { width: 56px; height: 56px; background: var(--bg); border-radius: 8px; display: flex; align-items: center; justify-content: center; color: var(--primary); font-size: 20px; }
    .item-details h3 { font-size: 15px; font-weight: 600; font-family: 'Inter', sans-serif; }
    .item-price { font-size: 13px; color: var(--text-muted); margin-top: 4px; }
    .item-controls { display: flex; align-items: center; gap: 16px; }
    .qty-controls { display: flex; align-items: center; gap: 8px; }
    .qty-btn { width: 28px; height: 28px; border-radius: 50%; border: 1px solid var(--border); background: white; cursor: pointer; font-size: 16px; display: flex; align-items: center; justify-content: center; transition: background 0.2s; &:hover:not(:disabled) { background: var(--bg); } &:disabled { opacity: 0.4; cursor: not-allowed; } }
    .qty { width: 32px; text-align: center; font-weight: 600; }
    .subtotal { font-size: 15px; font-weight: 700; color: var(--primary); min-width: 80px; text-align: right; }
    .cart-summary h3 { font-family: 'Inter', sans-serif; font-size: 18px; margin-bottom: 16px; }
    .summary-row { display: flex; justify-content: space-between; font-size: 13px; color: var(--text-muted); margin-bottom: 8px; span:first-child { max-width: 180px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; } }
    .summary-divider { height: 1px; background: var(--border); margin: 12px 0; }
    .summary-total { display: flex; justify-content: space-between; font-size: 18px; font-weight: 700; color: var(--primary); margin-bottom: 20px; }
    .checkout-btn { width: 100%; justify-content: center; margin-bottom: 10px; }
    .clear-btn { width: 100%; justify-content: center; }
    @media(max-width:768px){ .cart-layout { grid-template-columns: 1fr; } }
  `]
})
export class CartComponent implements OnInit {
  cart: Cart | null = null;
  loading = true;
  userId!: number;

  constructor(private cartSvc: CartService, private auth: AuthService) {}

  ngOnInit() {
    this.userId = this.auth.currentUser!.userId;
    this.load();
  }

  load() {
    this.cartSvc.getCart(this.userId).subscribe({ next: c => { this.cart = c; this.loading = false; }, error: () => this.loading = false });
  }

  updateQty(itemId: number, qty: number) {
    if (qty < 1) return;
    this.cartSvc.updateItem(this.userId, itemId, qty).subscribe({ next: c => this.cart = c, error: () => {} });
  }

  removeItem(itemId: number) {
    this.cartSvc.removeItem(this.userId, itemId).subscribe({ next: () => this.load(), error: () => {} });
  }

  clearCart() {
    this.cartSvc.clearCart(this.userId).subscribe({ next: () => this.load(), error: () => {} });
  }
}
