import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../services/services';
import { Order } from '../../models/models';

@Component({
  selector: 'app-admin-orders',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="page-header">
        <h1><i class="fas fa-clipboard-list"></i> Manage Orders</h1>
        <p>{{orders.length}} total orders</p>
      </div>

      <div class="filter-bar">
        <select [(ngModel)]="statusFilter" class="filter-select">
          <option value="">All Statuses</option>
          <option *ngFor="let s of statuses">{{s}}</option>
        </select>
        <input type="text" [(ngModel)]="search" placeholder="Search by order ID or book..." class="search-input">
      </div>

      <div class="loading" *ngIf="loading"><div class="spinner"></div></div>

      <div class="card table-card" *ngIf="!loading">
        <table>
          <thead>
            <tr>
              <th>Order ID</th>
              <th>User ID</th>
              <th>Book</th>
              <th>Qty</th>
              <th>Amount</th>
              <th>Payment</th>
              <th>Status</th>
              <th>Date</th>
              <th>Update Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let o of filtered">
              <td><strong>#{{o.orderId}}</strong></td>
              <td>{{o.userId}}</td>
              <td>{{o.bookTitle || 'Book #' + o.bookId}}</td>
              <td>{{o.quantity}}</td>
              <td>₹{{o.amountPaid | number:'1.2-2'}}</td>
              <td>
                <span class="pay-badge" [ngClass]="payClass(o.modeOfPayment)">
                  <i [class]="payIcon(o.modeOfPayment)"></i> {{o.modeOfPayment}}
                </span>
              </td>
              <td><span class="badge" [ngClass]="statusClass(o.orderStatus)">{{o.orderStatus}}</span></td>
              <td>{{o.orderDate | date:'shortDate'}}</td>
              <td>
                <select class="status-select" [(ngModel)]="pendingStatus[o.orderId]">
                  <option *ngFor="let s of statuses" [value]="s">{{s}}</option>
                </select>
              </td>
              <td>
                <button class="btn btn-primary btn-sm save-btn"
                        (click)="saveStatus(o)"
                        [disabled]="savingId === o.orderId || pendingStatus[o.orderId] === o.orderStatus">
                  <i class="fas fa-save"></i>
                  {{savingId === o.orderId ? 'Saving...' : 'Save'}}
                </button>
              </td>
            </tr>
            <tr *ngIf="!filtered.length">
              <td colspan="10" style="text-align:center;color:var(--text-muted);padding:32px">No orders found</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Success toast -->
      <div class="toast toast-success" *ngIf="savedMsg">
        <i class="fas fa-check-circle"></i> {{savedMsg}}
      </div>
    </div>
  `,
  styles: [`
    .filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
    .filter-select, .search-input { padding: 10px 16px; border: 1px solid var(--border); border-radius: 8px; font-size: 14px; &:focus { outline: none; border-color: var(--primary); } }
    .search-input { flex: 1; min-width: 200px; }
    .table-card { padding: 0; overflow: hidden; overflow-x: auto; }
    table { margin: 0; min-width: 1000px; }
    .status-select { padding: 6px 10px; border: 1px solid var(--border); border-radius: 6px; font-size: 13px; cursor: pointer; }
    .save-btn { white-space: nowrap; }
    .pay-badge { display: inline-flex; align-items: center; gap: 5px; font-size: 12px; font-weight: 600; padding: 3px 10px; border-radius: 20px; }
    .pay-cod { background: #c6f6d5; color: #276749; }
    .pay-wallet { background: #bee3f8; color: #2b6cb0; }
    .pay-razorpay { background: #dbeafe; color: #1d4ed8; }
    .pay-default { background: #e2e8f0; color: #4a5568; }
    .toast { position: fixed; bottom: 24px; right: 24px; background: var(--success); color: white; padding: 12px 20px; border-radius: 10px; font-size: 14px; font-weight: 500; display: flex; align-items: center; gap: 8px; z-index: 9999; box-shadow: 0 4px 16px rgba(0,0,0,0.15); animation: slideIn 0.3s ease; }
    @keyframes slideIn { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
  `]
})
export class AdminOrdersComponent implements OnInit {
  orders: Order[] = [];
  loading = true;
  statusFilter = '';
  search = '';
  savingId: number | null = null;
  savedMsg = '';
  pendingStatus: Record<number, string> = {};
  statuses = ['PLACED', 'CONFIRMED', 'DISPATCHED', 'DELIVERED', 'CANCELLED'];

  get filtered(): Order[] {
    return this.orders.filter(o => {
      const matchStatus = !this.statusFilter || o.orderStatus === this.statusFilter;
      const matchSearch = !this.search || String(o.orderId).includes(this.search) || (o.bookTitle || '').toLowerCase().includes(this.search.toLowerCase());
      return matchStatus && matchSearch;
    });
  }

  constructor(private orderSvc: OrderService) {}

  ngOnInit() {
    this.orderSvc.getAll().subscribe({
      next: o => {
        this.orders = [...o].reverse();
        // Initialize pending status selects with current status
        this.orders.forEach(ord => this.pendingStatus[ord.orderId] = ord.orderStatus);
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  saveStatus(order: Order) {
    const newStatus = this.pendingStatus[order.orderId];
    if (!newStatus || newStatus === order.orderStatus) return;
    this.savingId = order.orderId;
    this.orderSvc.changeStatus(order.orderId, newStatus).subscribe({
      next: () => {
        order.orderStatus = newStatus;
        this.savingId = null;
        this.showSaved(`Order #${order.orderId} status updated to ${newStatus}`);
      },
      error: () => {
        this.savingId = null;
        alert('Failed to update status. Please try again.');
      }
    });
  }

  showSaved(msg: string) {
    this.savedMsg = msg;
    setTimeout(() => this.savedMsg = '', 3000);
  }

  payClass(mode: string): string {
    const m = (mode || '').toUpperCase();
    if (m === 'COD') return 'pay-cod';
    if (m === 'WALLET') return 'pay-wallet';
    if (m === 'RAZORPAY') return 'pay-razorpay';
    return 'pay-default';
  }

  payIcon(mode: string): string {
    const m = (mode || '').toUpperCase();
    if (m === 'COD') return 'fas fa-money-bill-wave';
    if (m === 'WALLET') return 'fas fa-wallet';
    if (m === 'RAZORPAY') return 'fas fa-credit-card';
    return 'fas fa-tag';
  }

  statusClass(s: string): string {
    const m: Record<string, string> = { PLACED: 'badge-info', CONFIRMED: 'badge-warning', DISPATCHED: 'badge-warning', DELIVERED: 'badge-success', CANCELLED: 'badge-error' };
    return m[s?.toUpperCase()] || 'badge-secondary';
  }
}
