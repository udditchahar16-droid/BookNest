import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { WalletService } from '../../services/services';
import { RazorpayService } from '../../services/razorpay.service';
import { AuthService } from '../../services/auth.service';
import { Wallet } from '../../models/models';

type PaymentMethod = 'internal' | 'razorpay';

@Component({
  selector: 'app-wallet',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="container">
      <div class="page-header">
        <h1><i class="fas fa-wallet"></i> My Wallet</h1>
      </div>

      <!-- Balance Card -->
      <div class="wallet-layout">
        <div class="balance-card card">
          <div class="balance-label">Current Balance</div>
          <div class="balance-amount">
            <span *ngIf="wallet">₹{{wallet.currentBalance | number:'1.2-2'}}</span>
            <span *ngIf="!wallet" class="loading-bal">Loading...</span>
          </div>
          <div class="balance-actions">
            <a routerLink="/wallet/statements" class="btn btn-outline btn-sm">
              <i class="fas fa-file-alt"></i> View Statements
            </a>
          </div>
        </div>

        <!-- Add Money Card -->
        <div class="add-money-card card">
          <h3>Add Money to Wallet</h3>

          <!-- Alerts -->
          <div class="alert alert-success" *ngIf="success">
            <i class="fas fa-check-circle"></i> ₹{{lastAdded | number:'1.2-2'}} added to your wallet successfully!
          </div>
          <div class="alert alert-error" *ngIf="error">
            <i class="fas fa-exclamation-circle"></i> {{error}}
          </div>

          <!-- Quick Amount Buttons -->
          <div class="quick-amounts">
            <button *ngFor="let a of quickAmounts"
                    class="amount-btn" [class.selected]="amount === a"
                    (click)="amount = a">₹{{a}}</button>
          </div>

          <!-- Custom Amount -->
          <div class="form-group" style="margin-top:14px">
            <label>Custom Amount (₹)</label>
            <input type="number" [(ngModel)]="amount" min="1" max="100000"
                   placeholder="Enter amount (min ₹1)">
          </div>

          <!-- Payment Method Tabs -->
          <div class="method-tabs">
            <button class="method-tab" [class.active]="payMethod === 'internal'"
                    (click)="payMethod = 'internal'">
              <i class="fas fa-university"></i> Internal Top-Up
            </button>
            <button class="method-tab" [class.active]="payMethod === 'razorpay'"
                    (click)="payMethod = 'razorpay'">
              <img src="https://razorpay.com/favicon.png" width="16" height="16"
                   onerror="this.style.display='none'">
              Pay via Razorpay
            </button>
          </div>

          <!-- Internal top-up info -->
          <div class="method-info" *ngIf="payMethod === 'internal'">
            <i class="fas fa-info-circle"></i>
            Direct wallet credit — use for testing/demo (no real payment).
          </div>

          <!-- Razorpay info -->
          <div class="method-info razorpay-info" *ngIf="payMethod === 'razorpay'">
            <i class="fas fa-lock"></i>
            Secure payment via Razorpay — UPI, Cards, Net Banking, Wallets.
            <br><span class="test-note">Test mode: Use card <strong>4111 1111 1111 1111</strong>, any CVV & future date.</span>
          </div>

          <!-- Add Button -->
          <button class="btn btn-lg add-btn"
                  [class.btn-primary]="payMethod === 'internal'"
                  [class.btn-razorpay]="payMethod === 'razorpay'"
                  (click)="addMoney()"
                  [disabled]="!amount || amount <= 0 || processing">

            <ng-container *ngIf="!processing">
              <ng-container *ngIf="payMethod === 'internal'">
                <i class="fas fa-plus-circle"></i> Add ₹{{amount || 0}} to Wallet
              </ng-container>
              <ng-container *ngIf="payMethod === 'razorpay'">
                <i class="fas fa-credit-card"></i> Pay ₹{{amount || 0}} via Razorpay
              </ng-container>
            </ng-container>

            <ng-container *ngIf="processing">
              <span class="spinner-sm"></span>
              {{payMethod === 'razorpay' ? 'Opening Razorpay...' : 'Processing...'}}
            </ng-container>
          </button>

          <!-- Razorpay badge -->
          <div class="razorpay-badge" *ngIf="payMethod === 'razorpay'">
            <i class="fas fa-shield-alt"></i> Secured by Razorpay &nbsp;|&nbsp;
            <i class="fas fa-lock"></i> 256-bit SSL
          </div>
        </div>
      </div>

      <!-- Recent Transactions -->
      <div class="recent-tx card" *ngIf="wallet?.statements?.length">
        <div class="recent-header">
          <h3>Recent Transactions</h3>
          <a routerLink="/wallet/statements" class="see-all">View All</a>
        </div>
        <div class="tx-list">
          <div class="tx-row" *ngFor="let s of (wallet?.statements || []).slice().reverse().slice(0,5)">
            <div class="tx-icon" [class.deposit]="s.transactionType==='DEPOSIT'"
                                  [class.withdraw]="s.transactionType==='WITHDRAW'">
              <i [class]="s.transactionType === 'DEPOSIT' ? 'fas fa-arrow-down' : 'fas fa-arrow-up'"></i>
            </div>
            <div class="tx-detail">
              <span class="tx-rem">{{s.transactionRemarks || s.transactionType}}</span>
              <span class="tx-date">{{s.dateTime | date:'dd MMM yyyy, h:mm a'}}</span>
            </div>
            <div class="tx-amount" [class.credit]="s.transactionType==='DEPOSIT'"
                                    [class.debit]="s.transactionType==='WITHDRAW'">
              {{s.transactionType === 'DEPOSIT' ? '+' : '-'}}₹{{s.amount | number:'1.2-2'}}
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .wallet-layout { display: grid; grid-template-columns: 300px 1fr; gap: 24px; margin-bottom: 24px; }
    .balance-card { text-align: center; padding: 40px 24px; }
    .balance-label { font-size: 13px; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 12px; }
    .balance-amount { font-size: 2.8rem; font-weight: 700; color: var(--primary); font-family: 'Playfair Display', serif; margin-bottom: 24px; }
    .loading-bal { font-size: 1rem; color: var(--text-muted); }
    .balance-actions { display: flex; flex-direction: column; align-items: center; gap: 10px; }

    .add-money-card h3 { font-family: 'Inter', sans-serif; font-size: 18px; margin-bottom: 20px; }
    .quick-amounts { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; }
    .amount-btn {
      padding: 10px; border: 2px solid var(--border); border-radius: 8px;
      background: white; cursor: pointer; font-size: 14px; font-weight: 600;
      color: var(--primary); transition: all 0.2s;
      &:hover { border-color: var(--primary); background: var(--bg); }
      &.selected { border-color: var(--primary); background: var(--primary); color: white; }
    }

    .method-tabs { display: flex; gap: 0; margin: 16px 0 0; border: 1px solid var(--border); border-radius: 10px; overflow: hidden; }
    .method-tab {
      flex: 1; padding: 12px; border: none; background: white; cursor: pointer;
      font-size: 13px; font-weight: 500; font-family: 'Inter', sans-serif;
      color: var(--text-muted); display: flex; align-items: center; justify-content: center;
      gap: 7px; transition: all 0.2s;
      &:hover { background: var(--bg); }
      &.active { background: var(--primary); color: white; }
      &:first-child { border-right: 1px solid var(--border); }
    }

    .method-info {
      margin: 12px 0; padding: 10px 14px; border-radius: 8px;
      background: #f0f4ff; border: 1px solid #c7d2fe;
      font-size: 13px; color: var(--text-muted); line-height: 1.6;
      i { color: var(--primary); margin-right: 6px; }
    }
    .razorpay-info { background: #fff8f0; border-color: #fed7aa; }
    .test-note { font-size: 12px; color: var(--text-muted); margin-top: 4px; display: block; }

    .add-btn { width: 100%; justify-content: center; margin-top: 4px; font-size: 15px; }
    .btn-razorpay {
      background: #3395ff; color: white; border: none;
      &:hover:not(:disabled) { background: #1a7be8; transform: translateY(-1px); }
      &:disabled { opacity: 0.6; cursor: not-allowed; }
    }

    .razorpay-badge {
      text-align: center; margin-top: 10px; font-size: 12px;
      color: var(--text-muted);
      i { color: var(--success); }
    }

    .spinner-sm { width: 16px; height: 16px; border: 2px solid rgba(255,255,255,0.4); border-top-color: white; border-radius: 50%; animation: spin 0.7s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }

    /* Recent Transactions */
    .recent-tx { padding: 0; overflow: hidden; margin-bottom: 48px; }
    .recent-header { display: flex; align-items: center; justify-content: space-between; padding: 20px 24px; border-bottom: 1px solid var(--border); }
    .recent-header h3 { font-family: 'Inter', sans-serif; font-size: 16px; }
    .see-all { font-size: 13px; color: var(--accent); text-decoration: none; }
    .tx-list { padding: 8px 0; }
    .tx-row { display: flex; align-items: center; gap: 14px; padding: 12px 24px; transition: background 0.15s; &:hover { background: var(--bg); } }
    .tx-icon { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 14px; flex-shrink: 0; }
    .deposit { background: #c6f6d5; color: #276749; }
    .withdraw { background: #fed7d7; color: #742a2a; }
    .tx-detail { flex: 1; }
    .tx-rem { display: block; font-size: 14px; font-weight: 500; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 300px; }
    .tx-date { display: block; font-size: 12px; color: var(--text-muted); margin-top: 2px; }
    .tx-amount { font-size: 15px; font-weight: 700; }
    .credit { color: var(--success); }
    .debit { color: var(--error); }

    @media(max-width:768px){ .wallet-layout { grid-template-columns: 1fr; } .quick-amounts { grid-template-columns: repeat(2, 1fr); } }
  `]
})
export class WalletComponent implements OnInit {
  wallet: Wallet | null = null;
  amount = 0;
  processing = false;
  success = false;
  error = '';
  lastAdded = 0;
  userId!: number;
  userName = '';
  userEmail = '';
  payMethod: PaymentMethod = 'razorpay';
  quickAmounts = [100, 500, 1000, 2000];

  constructor(
    private walletSvc: WalletService,
    private razorpaySvc: RazorpayService,
    private auth: AuthService
  ) {}

  ngOnInit() {
    const user = this.auth.currentUser!;
    this.userId = user.userId;
    this.userName = user.fullName;
    this.userEmail = user.email;
    this.load();
  }

  load() {
    this.walletSvc.getWallet(this.userId).subscribe({
      next: w => this.wallet = w,
      error: () => {}
    });
  }

  addMoney() {
    if (!this.amount || this.amount <= 0) { this.error = 'Please enter a valid amount.'; return; }
    if (this.payMethod === 'razorpay') {
      this.payViaRazorpay();
    } else {
      this.payInternal();
    }
  }

  // ── Internal top-up (direct, no real payment) ─────────────────────────────
  payInternal() {
    this.processing = true; this.success = false; this.error = '';
    this.walletSvc.addMoney(this.userId, this.amount, `Wallet top-up — ₹${this.amount}`).subscribe({
      next: w => {
        this.wallet = w;
        this.lastAdded = this.amount;
        this.amount = 0;
        this.processing = false;
        this.success = true;
        setTimeout(() => this.success = false, 5000);
      },
      error: () => { this.processing = false; this.error = 'Top-up failed. Please try again.'; }
    });
  }

  // ── Razorpay flow ─────────────────────────────────────────────────────────
  async payViaRazorpay() {
    this.processing = true; this.success = false; this.error = '';

    // 1. Load Razorpay checkout.js if not already loaded
    const loaded = await this.razorpaySvc.loadScript();
    if (!loaded) {
      this.error = 'Could not load Razorpay SDK. Check your internet connection.';
      this.processing = false;
      return;
    }

    // 2. Create Razorpay order on backend
    this.razorpaySvc.createOrder(this.amount, `user_${this.userId}_topup`).subscribe({
      next: async (orderResp) => {
        try {
          // 3. Open Razorpay checkout popup — user pays
          const payment = await this.razorpaySvc.openCheckout(
            orderResp, this.userName, this.userEmail
          );

          // 4. Payment success — verify on backend + credit wallet
          this.razorpaySvc.verifyPayment({
            userId: this.userId,
            amount: this.amount,
            razorpayOrderId: payment.razorpay_order_id,
            razorpayPaymentId: payment.razorpay_payment_id,
            razorpaySignature: payment.razorpay_signature,
            remarks: `Razorpay top-up | Payment ID: ${payment.razorpay_payment_id}`
          }).subscribe({
            next: w => {
              this.wallet = w;
              this.lastAdded = this.amount;
              this.amount = 0;
              this.processing = false;
              this.success = true;
              setTimeout(() => this.success = false, 6000);
            },
            error: () => {
              this.processing = false;
              this.error = 'Payment received but wallet credit failed. Contact support.';
            }
          });

        } catch (err: any) {
          // User cancelled or payment failed
          this.processing = false;
          if (err?.message !== 'Payment cancelled by user') {
            this.error = 'Payment failed: ' + (err?.message || 'Unknown error');
          }
        }
      },
      error: () => {
        this.processing = false;
        this.error = 'Could not create payment order. Please try again.';
      }
    });
  }
}
