import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService } from '../../services/services';
import { BookService } from '../../services/book.service';
import { Order, Book } from '../../models/models';

@Component({
  selector: 'app-admin-analytics',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container">
      <div class="page-header">
        <h1><i class="fas fa-chart-bar"></i> Platform Analytics</h1>
      </div>

      <div class="loading" *ngIf="loading"><div class="spinner"></div></div>

      <div *ngIf="!loading">
        <!-- KPI Cards -->
        <div class="kpi-grid">
          <div class="kpi-card card">
            <div class="kpi-label">Total Revenue</div>
            <div class="kpi-value">₹{{totalRevenue | number:'1.2-2'}}</div>
            <div class="kpi-sub">from {{totalOrders}} orders</div>
          </div>
          <div class="kpi-card card">
            <div class="kpi-label">COD Revenue</div>
            <div class="kpi-value">₹{{codRevenue | number:'1.2-2'}}</div>
            <div class="kpi-sub">{{codOrders}} COD orders</div>
          </div>
          <div class="kpi-card card">
            <div class="kpi-label">Wallet Revenue</div>
            <div class="kpi-value">₹{{walletRevenue | number:'1.2-2'}}</div>
            <div class="kpi-sub">{{walletOrders}} wallet orders</div>
          </div>
          <div class="kpi-card card">
            <div class="kpi-label">Razorpay Revenue</div>
            <div class="kpi-value">₹{{razorpayRevenue | number:'1.2-2'}}</div>
            <div class="kpi-sub">{{razorpayOrders}} online orders</div>
          </div>
          <div class="kpi-card card">
            <div class="kpi-label">Avg Order Value</div>
            <div class="kpi-value">₹{{avgOrder | number:'1.2-2'}}</div>
            <div class="kpi-sub">per order</div>
          </div>
        </div>

        <div class="analytics-grid">
          <!-- Order Status Breakdown -->
          <div class="card">
            <h3>Orders by Status</h3>
            <div class="status-breakdown">
              <div class="status-row" *ngFor="let s of statusBreakdown">
                <div class="status-name">
                  <span class="badge" [ngClass]="s.cls">{{s.status}}</span>
                </div>
                <div class="status-bar-wrap">
                  <div class="status-bar" [style.width.%]="(s.count / totalOrders) * 100" [ngClass]="s.barCls"></div>
                </div>
                <div class="status-count">{{s.count}}</div>
              </div>
            </div>
          </div>

          <!-- Top Books -->
          <div class="card">
            <h3>Top Selling Books</h3>
            <div class="top-books">
              <div class="top-book-row" *ngFor="let b of topBooks; let i = index">
                <div class="rank">#{{i + 1}}</div>
                <div class="book-details">
                  <strong>{{b.title}}</strong>
                  <span>{{b.author}}</span>
                </div>
                <div class="book-orders">{{b.orderCount}} orders</div>
              </div>
              <div class="empty-state" *ngIf="!topBooks.length" style="padding:24px">
                <p style="color:var(--text-muted);font-size:14px">No orders yet</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Revenue by Payment Mode -->
        <div class="card revenue-breakdown">
          <h3>Payment Mode Distribution</h3>
          <div class="mode-grid">
            <div class="mode-card">
              <div class="mode-icon cod"><i class="fas fa-money-bill-wave"></i></div>
              <div class="mode-info">
                <div class="mode-value">{{codOrders}}</div>
                <div class="mode-label">Cash on Delivery</div>
                <div class="mode-pct">{{totalOrders ? ((codOrders / totalOrders) * 100 | number:'1.0-0') : 0}}% of orders</div>
              </div>
            </div>
            <div class="mode-card">
              <div class="mode-icon wallet"><i class="fas fa-wallet"></i></div>
              <div class="mode-info">
                <div class="mode-value">{{walletOrders}}</div>
                <div class="mode-label">E-Wallet</div>
                <div class="mode-pct">{{totalOrders ? ((walletOrders / totalOrders) * 100 | number:'1.0-0') : 0}}% of orders</div>
              </div>
            </div>
            <div class="mode-card">
              <div class="mode-icon razorpay"><i class="fas fa-credit-card"></i></div>
              <div class="mode-info">
                <div class="mode-value">{{razorpayOrders}}</div>
                <div class="mode-label">Razorpay</div>
                <div class="mode-pct">{{totalOrders ? ((razorpayOrders / totalOrders) * 100 | number:'1.0-0') : 0}}% of orders</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .kpi-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 20px; margin-bottom: 24px; }
    .kpi-card { padding: 24px; }
    .kpi-label { font-size: 13px; color: var(--text-muted); font-weight: 500; margin-bottom: 8px; }
    .kpi-value { font-size: 1.8rem; font-weight: 700; color: var(--primary); font-family: 'Playfair Display', serif; }
    .kpi-sub { font-size: 12px; color: var(--text-muted); margin-top: 4px; }
    .analytics-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; margin-bottom: 24px; }
    .analytics-grid h3, .revenue-breakdown h3 { font-family: 'Inter', sans-serif; font-size: 16px; margin-bottom: 20px; }
    .status-breakdown { display: flex; flex-direction: column; gap: 12px; }
    .status-row { display: grid; grid-template-columns: 120px 1fr 40px; align-items: center; gap: 12px; }
    .status-bar-wrap { background: var(--bg); border-radius: 4px; height: 8px; overflow: hidden; }
    .status-bar { height: 100%; border-radius: 4px; transition: width 0.5s; }
    .bar-placed { background: #63b3ed; }
    .bar-confirmed { background: #f6ad55; }
    .bar-dispatched { background: #fc8181; }
    .bar-delivered { background: #68d391; }
    .bar-cancelled { background: #fc8181; }
    .status-count { font-weight: 600; font-size: 14px; text-align: right; }
    .top-books { display: flex; flex-direction: column; gap: 12px; }
    .top-book-row { display: flex; align-items: center; gap: 12px; padding: 10px; border-radius: 8px; background: var(--bg); }
    .rank { width: 28px; height: 28px; border-radius: 50%; background: var(--primary); color: white; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; flex-shrink: 0; }
    .book-details { flex: 1; strong { display: block; font-size: 14px; font-family: 'Inter', sans-serif; } span { font-size: 12px; color: var(--text-muted); } }
    .book-orders { font-size: 13px; font-weight: 600; color: var(--primary); }
    .revenue-breakdown { margin-bottom: 24px; }
    .mode-grid { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 20px; }
    .mode-card { display: flex; align-items: center; gap: 16px; padding: 20px; background: var(--bg); border-radius: 10px; }
    .mode-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 20px; }
    .cod { background: #c6f6d5; color: #276749; }
    .wallet { background: #bee3f8; color: #2b6cb0; }
    .razorpay { background: #dbeafe; color: #3395ff; }
    .mode-value { font-size: 1.4rem; font-weight: 700; color: var(--primary); }
    .mode-label { font-size: 14px; font-weight: 500; }
    .mode-pct { font-size: 12px; color: var(--text-muted); margin-top: 2px; }
    @media(max-width:1024px){ .kpi-grid { grid-template-columns: repeat(2, 1fr); } }
    @media(max-width:768px){ .analytics-grid, .mode-grid { grid-template-columns: 1fr; } .kpi-grid { grid-template-columns: 1fr; } }
  `]
})
export class AdminAnalyticsComponent implements OnInit {
  loading = true;
  totalRevenue = 0; totalOrders = 0; codRevenue = 0; codOrders = 0;
  walletRevenue = 0; walletOrders = 0; razorpayRevenue = 0; razorpayOrders = 0; avgOrder = 0;
  statusBreakdown: any[] = [];
  topBooks: any[] = [];

  constructor(private orderSvc: OrderService, private bookSvc: BookService) {}

  ngOnInit() {
    this.orderSvc.getAll().subscribe({
      next: orders => {
        this.totalOrders = orders.length;
        this.totalRevenue = orders.reduce((s, o) => s + o.amountPaid, 0);
        this.avgOrder = this.totalOrders ? this.totalRevenue / this.totalOrders : 0;
        const cod = orders.filter(o => o.modeOfPayment === 'COD');
        const wallet = orders.filter(o => o.modeOfPayment === 'WALLET');
        const razorpay = orders.filter(o => o.modeOfPayment === 'RAZORPAY');
        this.codOrders = cod.length; this.codRevenue = cod.reduce((s, o) => s + o.amountPaid, 0);
        this.walletOrders = wallet.length; this.walletRevenue = wallet.reduce((s, o) => s + o.amountPaid, 0);
        this.razorpayOrders = razorpay.length; this.razorpayRevenue = razorpay.reduce((s, o) => s + o.amountPaid, 0);

        const statusMap: Record<string, number> = {};
        orders.forEach(o => { statusMap[o.orderStatus] = (statusMap[o.orderStatus] || 0) + 1; });
        const clsMap: Record<string, string> = { PLACED: 'badge-info', CONFIRMED: 'badge-warning', DISPATCHED: 'badge-warning', DELIVERED: 'badge-success', CANCELLED: 'badge-error' };
        const barMap: Record<string, string> = { PLACED: 'bar-placed', CONFIRMED: 'bar-confirmed', DISPATCHED: 'bar-dispatched', DELIVERED: 'bar-delivered', CANCELLED: 'bar-cancelled' };
        this.statusBreakdown = Object.entries(statusMap).map(([status, count]) => ({ status, count, cls: clsMap[status] || 'badge-secondary', barCls: barMap[status] || 'bar-placed' }));

        const bookMap: Record<string, any> = {};
        orders.forEach(o => {
          const key = o.bookId;
          if (!bookMap[key]) bookMap[key] = { bookId: key, title: o.bookTitle || `Book #${key}`, author: '', orderCount: 0 };
          bookMap[key].orderCount++;
        });
        this.topBooks = Object.values(bookMap).sort((a, b) => b.orderCount - a.orderCount).slice(0, 5);
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }
}
