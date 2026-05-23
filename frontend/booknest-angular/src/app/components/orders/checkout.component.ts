import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CartService, OrderService, WalletService, NotificationService } from '../../services/services';
import { RazorpayService } from '../../services/razorpay.service';
import { AuthService } from '../../services/auth.service';
import { Cart, Wallet, Address } from '../../models/models';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="container">
      <div class="page-header">
        <h1><i class="fas fa-lock"></i> Checkout</h1>
      </div>

      <div class="checkout-layout">
        <!-- Left: Address + Payment -->
        <div class="checkout-main">
          <div class="card">
            <h3 class="section-title">Delivery Address</h3>
            <div class="grid-2">
              <div class="form-group">
                <label>Full Name</label>
                <input type="text" [(ngModel)]="addr.fullName" placeholder="John Doe">
              </div>
              <div class="form-group">
                <label>Mobile Number</label>
                <input type="tel" [(ngModel)]="addr.mobileNumber" placeholder="+91 XXXXX XXXXX">
              </div>
              <div class="form-group" style="grid-column:1/-1">
                <label>Flat / House No., Building, Street</label>
                <input type="text" [(ngModel)]="addr.flatNumber" placeholder="123, Main Street">
              </div>
              <div class="form-group">
                <label>City</label>
                <input type="text" [(ngModel)]="addr.city" placeholder="Mumbai">
              </div>
              <div class="form-group">
                <label>State</label>
                <input type="text" [(ngModel)]="addr.state" placeholder="Maharashtra">
              </div>
              <div class="form-group">
                <label>Pincode</label>
                <input type="text" [(ngModel)]="addr.pincode" placeholder="400001">
              </div>
            </div>

            <h3 class="section-title" style="margin-top:28px">Payment Method</h3>
            <div class="payment-options">

              <!-- COD -->
              <label class="pay-option" [class.selected]="payMode === 'COD'">
                <input type="radio" name="pay" value="COD" [(ngModel)]="payMode">
                <div class="pay-icon cod-icon"><i class="fas fa-money-bill-wave"></i></div>
                <div class="pay-info">
                  <strong>Cash on Delivery</strong>
                  <p>Pay when your order arrives — no online payment needed</p>
                </div>
              </label>

              <!-- Wallet -->
              <label class="pay-option" [class.selected]="payMode === 'WALLET'">
                <input type="radio" name="pay" value="WALLET" [(ngModel)]="payMode">
                <div class="pay-icon wallet-icon"><i class="fas fa-wallet"></i></div>
                <div class="pay-info">
                  <strong>Pay via Wallet</strong>
                  <p>Balance: <span [class]="walletEnough ? 'bal-ok' : 'bal-low'">
                    ₹{{wallet?.currentBalance | number:'1.2-2'}}</span>
                  </p>
                </div>
              </label>

              <!-- Razorpay -->
              <label class="pay-option razorpay-option" [class.selected]="payMode === 'RAZORPAY'">
                <input type="radio" name="pay" value="RAZORPAY" [(ngModel)]="payMode">
                <div class="pay-icon rzp-icon">
                  <i class="fas fa-credit-card"></i>
                </div>
                <div class="pay-info">
                  <strong>Pay via Razorpay</strong>
                  <p>UPI, Credit/Debit Card, Net Banking, Wallets</p>
                </div>
                <span class="rzp-badge">Secure</span>
              </label>
            </div>

            <!-- Razorpay test hint -->
            <div class="rzp-hint" *ngIf="payMode === 'RAZORPAY'">
              <i class="fas fa-info-circle"></i>
              <span>Test mode — use card <strong>4111 1111 1111 1111</strong>, any future date, any CVV.</span>
            </div>

            <!-- Selected Payment Method Indicator -->
            <div class="selected-pay-indicator">
              <span class="selected-pay-label">Selected Payment:</span>
              <button class="btn selected-pay-btn" [ngClass]="payBtnClass()" disabled>
                <i [class]="payBtnIcon()"></i> {{payBtnLabel()}}
              </button>
            </div>

            <!-- Wallet low balance warning -->
            <div class="alert alert-warning" *ngIf="payMode === 'WALLET' && !walletEnough">
              Insufficient wallet balance (₹{{wallet?.currentBalance | number:'1.2-2'}} available).
              <a routerLink="/wallet">Add Money</a> or choose another payment method.
            </div>

            <div class="alert alert-error" *ngIf="error">{{error}}</div>

            <button class="btn btn-lg place-btn"
                    [class.btn-accent]="payMode !== 'RAZORPAY'"
                    [class.btn-rzp]="payMode === 'RAZORPAY'"
                    (click)="placeOrder()"
                    [disabled]="placing || !isFormValid() || (payMode==='WALLET' && !walletEnough)">
              <span *ngIf="!placing">
                <i class="fas fa-check-circle" *ngIf="payMode !== 'RAZORPAY'"></i>
                <i class="fas fa-credit-card" *ngIf="payMode === 'RAZORPAY'"></i>
                {{placeLabel()}}
              </span>
              <span *ngIf="placing" class="btn-loading">
                <span class="spinner-sm"></span> {{placing ? 'Processing...' : ''}}
              </span>
            </button>

            <div class="secure-note" *ngIf="payMode === 'RAZORPAY'">
              <i class="fas fa-shield-alt"></i> Secured by Razorpay &nbsp;|&nbsp;
              <i class="fas fa-lock"></i> 256-bit SSL Encryption
            </div>
          </div>
        </div>

        <!-- Right: Order Summary -->
        <div class="checkout-summary">
          <div class="card">
            <h3 style="font-family:'Inter',sans-serif;margin-bottom:16px">Order Summary</h3>
            <div class="loading" *ngIf="!cart"><div class="spinner"></div></div>
            <ng-container *ngIf="cart">
              <div class="summary-item" *ngFor="let item of cart.items">
                <span>{{item.bookTitle}} × {{item.quantity}}</span>
                <span>₹{{item.price * item.quantity | number:'1.2-2'}}</span>
              </div>
              <div class="summary-divider"></div>
              <div class="summary-total">
                <strong>Total</strong>
                <strong class="total-amt">₹{{cart.totalPrice | number:'1.2-2'}}</strong>
              </div>
            </ng-container>
            <a routerLink="/cart" class="edit-cart"><i class="fas fa-edit"></i> Edit Cart</a>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .checkout-layout { display: grid; grid-template-columns: 1fr 340px; gap: 24px; padding-bottom: 48px; }
    .section-title { font-family: 'Inter', sans-serif; font-size: 16px; font-weight: 600; margin-bottom: 16px; color: var(--text); }
    .payment-options { display: flex; flex-direction: column; gap: 12px; margin-bottom: 16px; }
    .pay-option {
      display: flex; align-items: center; gap: 14px; padding: 16px;
      border: 2px solid var(--border); border-radius: 12px; cursor: pointer; transition: all 0.2s;
      position: relative;
      input { display: none; }
      strong { font-size: 15px; display: block; }
      p { font-size: 13px; color: var(--text-muted); margin-top: 2px; }
    }
    .pay-option.selected { border-color: var(--primary); background: #f0f4ff; }
    .pay-option.razorpay-option.selected { border-color: #3395ff; background: #f0f7ff; }
    .pay-icon { width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 18px; flex-shrink: 0; }
    .cod-icon    { background: #c6f6d5; color: #276749; }
    .wallet-icon { background: #bee3f8; color: #2b6cb0; }
    .rzp-icon    { background: #dbeafe; color: #3395ff; }
    .pay-info { flex: 1; }
    .bal-ok { color: var(--success); font-weight: 600; }
    .bal-low { color: var(--error); font-weight: 600; }
    .rzp-badge { font-size: 11px; background: #3395ff; color: white; padding: 2px 8px; border-radius: 20px; font-weight: 600; flex-shrink: 0; }
    .rzp-hint { display: flex; align-items: flex-start; gap: 8px; padding: 10px 14px; background: #fffbeb; border: 1px solid #fef3c7; border-radius: 8px; font-size: 13px; color: var(--text-muted); margin-bottom: 14px; i { color: #d97706; margin-top: 1px; } }
    .place-btn { width: 100%; justify-content: center; margin-top: 8px; font-size: 15px; }
    .btn-rzp { background: #3395ff; color: white; border: none; &:hover:not(:disabled) { background: #1a7be8; transform: translateY(-1px); } &:disabled { opacity: 0.6; cursor: not-allowed; } }
    .btn-loading { display: flex; align-items: center; gap: 10px; }
    .spinner-sm { width: 16px; height: 16px; border: 2px solid rgba(255,255,255,0.4); border-top-color: white; border-radius: 50%; animation: spin 0.7s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }
    .secure-note { text-align: center; margin-top: 10px; font-size: 12px; color: var(--text-muted); i { color: var(--success); } }
    .selected-pay-indicator { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; padding: 10px 14px; background: var(--bg); border-radius: 10px; border: 1px solid var(--border); }
    .selected-pay-label { font-size: 13px; color: var(--text-muted); font-weight: 500; white-space: nowrap; }
    .selected-pay-btn { padding: 6px 16px; border-radius: 20px; font-size: 13px; font-weight: 600; cursor: default; pointer-events: none; display: inline-flex; align-items: center; gap: 6px; }
    .pay-btn-cod { background: #c6f6d5; color: #276749; border: 1.5px solid #9ae6b4; }
    .pay-btn-wallet { background: #bee3f8; color: #2b6cb0; border: 1.5px solid #90cdf4; }
    .pay-btn-razorpay { background: #dbeafe; color: #1d4ed8; border: 1.5px solid #93c5fd; }
    .summary-item { display: flex; justify-content: space-between; font-size: 13px; color: var(--text-muted); margin-bottom: 8px; span:first-child { max-width: 170px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; } }
    .summary-divider { height: 1px; background: var(--border); margin: 12px 0; }
    .summary-total { display: flex; justify-content: space-between; font-size: 17px; color: var(--primary); margin-bottom: 16px; }
    .total-amt { font-size: 20px; }
    .edit-cart { font-size: 13px; color: var(--accent); display: flex; align-items: center; justify-content: center; gap: 6px; text-decoration: none; }
    @media(max-width:768px){ .checkout-layout { grid-template-columns: 1fr; } }
  `]
})
export class CheckoutComponent implements OnInit {
  cart: Cart | null = null;
  wallet: Wallet | null = null;
  payMode = 'COD';
  placing = false;
  error = '';
  userId!: number;
  userName = '';
  userEmail = '';
  addr: Address = { fullName: '', mobileNumber: '', flatNumber: '', city: '', state: '', pincode: '' };

  get walletEnough(): boolean {
    return !!(this.wallet && this.cart && this.wallet.currentBalance >= this.cart.totalPrice);
  }

  constructor(
    private cartSvc: CartService, private orderSvc: OrderService,
    private walletSvc: WalletService, private notifSvc: NotificationService,
    private razorpaySvc: RazorpayService,
    private auth: AuthService, private router: Router
  ) {}

  ngOnInit() {
    const user = this.auth.currentUser!;
    this.userId = user.userId;
    this.userName = user.fullName;
    this.userEmail = user.email;
    this.cartSvc.getCart(this.userId).subscribe({ next: c => this.cart = c, error: () => {} });
    this.walletSvc.getWallet(this.userId).subscribe({ next: w => this.wallet = w, error: () => {} });
  }

  isFormValid(): boolean {
    return !!(this.addr.fullName && this.addr.mobileNumber && this.addr.flatNumber
           && this.addr.city && this.addr.state && this.addr.pincode);
  }

  payBtnLabel(): string {
    if (this.payMode === 'COD') return 'Cash on Delivery';
    if (this.payMode === 'WALLET') return 'Wallet';
    if (this.payMode === 'RAZORPAY') return 'Razorpay';
    return this.payMode;
  }

  payBtnIcon(): string {
    if (this.payMode === 'COD') return 'fas fa-money-bill-wave';
    if (this.payMode === 'WALLET') return 'fas fa-wallet';
    if (this.payMode === 'RAZORPAY') return 'fas fa-credit-card';
    return 'fas fa-tag';
  }

  payBtnClass(): string {
    if (this.payMode === 'COD') return 'pay-btn-cod';
    if (this.payMode === 'WALLET') return 'pay-btn-wallet';
    if (this.payMode === 'RAZORPAY') return 'pay-btn-razorpay';
    return '';
  }

  placeLabel(): string {
    const amt = this.cart?.totalPrice || 0;
    if (this.payMode === 'RAZORPAY') return `Pay ₹${amt.toFixed(2)} via Razorpay`;
    if (this.payMode === 'WALLET')   return `Pay ₹${amt.toFixed(2)} from Wallet`;
    return 'Place Order (COD)';
  }

  placeOrder() {
    if (!this.cart?.items?.length) { this.error = 'Cart is empty.'; return; }
    this.error = ''; this.placing = true;

    if (this.payMode === 'RAZORPAY') { this.razorpayCheckout(); return; }
    if (this.payMode === 'WALLET')   { this.walletCheckout();   return; }
    this.codCheckout();
  }

  // ── COD ───────────────────────────────────────────────────────────────────
  private codCheckout() {
    const req = this.buildOrderReq();
    this.orderSvc.placeCOD(req).subscribe({
      next: () => this.onSuccess(req.bookTitle),
      error: () => { this.placing = false; this.error = 'Order placement failed.'; }
    });
  }

  // ── Wallet ────────────────────────────────────────────────────────────────
  private walletCheckout() {
    const req = this.buildOrderReq();
    this.walletSvc.payMoney(this.userId, this.cart!.totalPrice,
      `Payment for ${req.bookTitle}`).subscribe({
      next: () => {
        this.orderSvc.placeOnline(req).subscribe({
          next: () => this.onSuccess(req.bookTitle),
          error: () => { this.placing = false; this.error = 'Order failed after wallet deduction. Contact support.'; }
        });
      },
      error: () => { this.placing = false; this.error = 'Wallet payment failed.'; }
    });
  }

  // ── Razorpay ──────────────────────────────────────────────────────────────
  private async razorpayCheckout() {
    const loaded = await this.razorpaySvc.loadScript();
    if (!loaded) { this.placing = false; this.error = 'Could not load Razorpay SDK.'; return; }

    this.razorpaySvc.createOrder(this.cart!.totalPrice,
      `user_${this.userId}_order`).subscribe({
      next: async (orderResp) => {
        try {
          const payment = await this.razorpaySvc.openCheckout(
            orderResp, this.userName, this.userEmail
          );
          // Payment successful — directly place RAZORPAY order (no wallet involved)
          const req = this.buildOrderReq('RAZORPAY');
          this.orderSvc.placeOnline(req).subscribe({
            next: () => this.onSuccess(req.bookTitle),
            error: () => { this.placing = false; this.error = 'Payment done but order failed. Contact support.'; }
          });
        } catch (err: any) {
          this.placing = false;
          if (err?.message !== 'Payment cancelled by user') {
            this.error = 'Payment failed: ' + (err?.message || 'Unknown error');
          }
        }
      },
      error: () => { this.placing = false; this.error = 'Could not create payment order.'; }
    });
  }

  private buildOrderReq(mode?: string) {
    // Use first item as primary book reference; totalPrice covers all items
    const item = this.cart!.items[0];
    const totalQty = this.cart!.items.reduce((sum, i) => sum + i.quantity, 0);
    const bookTitle = this.cart!.items.length === 1
      ? item.bookTitle
      : `${item.bookTitle} + ${this.cart!.items.length - 1} more`;
    return {
      userId: this.userId,
      bookId: item.bookId,
      bookTitle: bookTitle,
      quantity: totalQty,
      amountPaid: this.cart!.totalPrice,
      modeOfPayment: mode || this.payMode,
      address: { ...this.addr, customerId: this.userId }
    };
  }

  onSuccess(bookTitle: string) {
    this.cartSvc.clearCart(this.userId).subscribe({ error: () => {} });
    this.notifSvc.sendNotification({
      userId: this.userId,
      type: 'ORDER_PLACED',
      message: `Your order for "${bookTitle}" has been placed!`
    }).subscribe({ error: () => {} });
    this.placing = false;
    this.router.navigate(['/orders'], { queryParams: { placed: true } });
  }
}
