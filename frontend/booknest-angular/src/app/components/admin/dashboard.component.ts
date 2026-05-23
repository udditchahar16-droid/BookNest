import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { BookService } from '../../services/book.service';
import { OrderService } from '../../services/services';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container">
      <div class="page-header">
        <h1><i class="fas fa-shield-alt"></i> Admin Dashboard</h1>
        <p>Welcome back, {{admin?.fullName}}</p>
      </div>

      <div class="stats-grid">
        <div class="stat-card card">
          <div class="stat-icon books"><i class="fas fa-book"></i></div>
          <div class="stat-info">
            <h3>{{totalBooks}}</h3>
            <p>Total Books</p>
          </div>
        </div>
        <div class="stat-card card">
          <div class="stat-icon orders"><i class="fas fa-box"></i></div>
          <div class="stat-info">
            <h3>{{totalOrders}}</h3>
            <p>Total Orders</p>
          </div>
        </div>
        <div class="stat-card card">
          <div class="stat-icon revenue"><i class="fas fa-rupee-sign"></i></div>
          <div class="stat-info">
            <h3>₹{{totalRevenue | number:'1.0-0'}}</h3>
            <p>Total Revenue</p>
          </div>
        </div>
        <div class="stat-card card">
          <div class="stat-icon pending"><i class="fas fa-clock"></i></div>
          <div class="stat-info">
            <h3>{{pendingOrders}}</h3>
            <p>Pending Orders</p>
          </div>
        </div>
      </div>

      <div class="quick-links">
        <h2>Quick Actions</h2>
        <div class="links-grid">
          <a routerLink="/admin/books/add" class="quick-card card">
            <i class="fas fa-plus-circle"></i>
            <span>Add New Book</span>
          </a>
          <a routerLink="/admin/books" class="quick-card card">
            <i class="fas fa-book"></i>
            <span>Manage Catalog</span>
          </a>
          <a routerLink="/admin/orders" class="quick-card card">
            <i class="fas fa-clipboard-list"></i>
            <span>Manage Orders</span>
          </a>
          <a routerLink="/admin/users" class="quick-card card">
            <i class="fas fa-users"></i>
            <span>Manage Users</span>
          </a>
          <a routerLink="/admin/analytics" class="quick-card card">
            <i class="fas fa-chart-bar"></i>
            <span>Analytics</span>
          </a>
          <a routerLink="/admin/inventory" class="quick-card card">
            <i class="fas fa-warehouse"></i>
            <span>Inventory</span>
          </a>
          <a routerLink="/admin/reviews" class="quick-card card">
            <i class="fas fa-star"></i>
            <span>Moderate Reviews</span>
          </a>
        </div>
      </div>

      <!-- Recent Orders -->
      <div class="recent-section card" *ngIf="recentOrders.length">
        <div class="section-head">
          <h3>Recent Orders</h3>
          <a routerLink="/admin/orders" class="see-all">View All</a>
        </div>
        <table>
          <thead>
            <tr>
              <th>Order ID</th>
              <th>Book</th>
              <th>Amount</th>
              <th>Mode</th>
              <th>Status</th>
              <th>Date</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let o of recentOrders.slice(0,5)">
              <td>#{{o.orderId}}</td>
              <td>{{o.bookTitle || 'Book #' + o.bookId}}</td>
              <td>₹{{o.amountPaid | number:'1.2-2'}}</td>
              <td>{{o.modeOfPayment}}</td>
              <td><span class="badge" [ngClass]="statusClass(o.orderStatus)">{{o.orderStatus}}</span></td>
              <td>{{o.orderDate | date:'shortDate'}}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 32px; }
    .stat-card { display: flex; align-items: center; gap: 16px; padding: 20px; }
    .stat-icon { width: 52px; height: 52px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 22px; }
    .books { background: #e8f0fe; color: var(--primary); }
    .orders { background: #fef3c7; color: #744210; }
    .revenue { background: #c6f6d5; color: #276749; }
    .pending { background: #fed7d7; color: #742a2a; }
    .stat-info h3 { font-size: 1.8rem; font-weight: 700; color: var(--text); font-family: 'Inter', sans-serif; }
    .stat-info p { font-size: 13px; color: var(--text-muted); margin-top: 2px; }
    .quick-links { margin-bottom: 32px; }
    .quick-links h2 { font-size: 1.4rem; color: var(--primary); margin-bottom: 16px; }
    .links-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 16px; }
    .quick-card {
      display: flex; flex-direction: column; align-items: center; justify-content: center;
      gap: 12px; padding: 24px 16px; text-decoration: none; color: var(--text);
      transition: all 0.2s; text-align: center;
      i { font-size: 28px; color: var(--primary); }
      span { font-size: 14px; font-weight: 500; }
      &:hover { transform: translateY(-4px); box-shadow: var(--shadow-lg); border-color: var(--primary); }
    }
    .recent-section { padding: 0; overflow: hidden; }
    .section-head { display: flex; align-items: center; justify-content: space-between; padding: 20px 24px; border-bottom: 1px solid var(--border); }
    .section-head h3 { font-family: 'Inter', sans-serif; font-size: 16px; }
    .see-all { font-size: 13px; color: var(--accent); text-decoration: none; }
    @media(max-width:1024px){ .stats-grid { grid-template-columns: repeat(2, 1fr); } }
    @media(max-width:600px){ .stats-grid { grid-template-columns: 1fr; } }
  `]
})
export class AdminDashboardComponent implements OnInit {
  admin: any;
  totalBooks = 0;
  totalOrders = 0;
  totalRevenue = 0;
  pendingOrders = 0;
  recentOrders: any[] = [];

  constructor(private bookSvc: BookService, private orderSvc: OrderService, private auth: AuthService) {}

  ngOnInit() {
    this.admin = this.auth.currentUser;
    this.bookSvc.getAll().subscribe({ next: b => this.totalBooks = b.length, error: () => {} });
    this.orderSvc.getAll().subscribe({
      next: orders => {
        this.totalOrders = orders.length;
        this.totalRevenue = orders.reduce((s, o) => s + o.amountPaid, 0);
        this.pendingOrders = orders.filter(o => o.orderStatus === 'PLACED').length;
        this.recentOrders = [...orders].reverse();
      },
      error: () => {}
    });
  }

  statusClass(s: string): string {
    const m: Record<string, string> = { PLACED: 'badge-info', CONFIRMED: 'badge-warning', DISPATCHED: 'badge-warning', DELIVERED: 'badge-success', CANCELLED: 'badge-error' };
    return m[s?.toUpperCase()] || 'badge-secondary';
  }
}
