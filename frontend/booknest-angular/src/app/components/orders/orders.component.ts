import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { OrderService, NotificationService } from '../../services/services';
import { AuthService } from '../../services/auth.service';
import { Order } from '../../models/models';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container">
      <div class="page-header">
        <h1><i class="fas fa-box"></i> My Orders</h1>
        <p *ngIf="orders.length">{{orders.length}} orders total</p>
      </div>

      <div class="alert alert-success" *ngIf="placed">
        <i class="fas fa-check-circle"></i> Order placed successfully! 🎉
      </div>

      <div class="loading" *ngIf="loading"><div class="spinner"></div></div>

      <div class="orders-list" *ngIf="!loading && orders.length">
        <div class="order-card card" *ngFor="let o of orders">
          <div class="order-header">
            <div class="order-id-group">
              <span class="order-id">#{{o.orderId}}</span>
              <span class="badge" [ngClass]="statusClass(o.orderStatus)">{{o.orderStatus}}</span>
            </div>
            <span class="order-date">{{o.orderDate | date:'mediumDate'}}</span>
          </div>

          <div class="order-body">
            <div class="order-item">
              <div class="item-thumb"><i class="fas fa-book"></i></div>
              <div>
                <h3>{{o.bookTitle || 'Book #' + o.bookId}}</h3>
                <p>Qty: {{o.quantity}} &nbsp;|&nbsp; {{o.modeOfPayment}}</p>
              </div>
            </div>
            <div class="order-amount">₹{{o.amountPaid | number:'1.2-2'}}</div>
          </div>

          <div class="order-footer" *ngIf="o.address">
            <i class="fas fa-map-marker-alt"></i>
            <span>{{o.address.flatNumber}}, {{o.address.city}}, {{o.address.state}} - {{o.address.pincode}}</span>
          </div>

          <!-- Order Actions: No cancel button for users -->
          <div class="order-actions">
            <!-- Download Invoice -->
            <button class="btn btn-outline btn-sm" (click)="downloadInvoice(o)">
              <i class="fas fa-file-pdf"></i> Invoice
            </button>

            <!-- Track order - show status pipeline -->
            <button class="btn btn-outline btn-sm" (click)="toggleTrack(o.orderId)">
              <i class="fas fa-truck"></i> Track
            </button>
          </div>

          <!-- Order Tracking Timeline -->
          <div class="tracking-timeline" *ngIf="trackingOpen === o.orderId">
            <div class="timeline">
              <div class="tl-step" *ngFor="let step of getSteps(o.orderStatus)"
                   [class.done]="step.done" [class.active]="step.active">
                <div class="tl-icon"><i [class]="step.icon"></i></div>
                <div class="tl-label">{{step.label}}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="empty-state" *ngIf="!loading && !orders.length">
        <i class="fas fa-box-open"></i>
        <h3>No orders yet</h3>
        <p>Browse books and place your first order</p>
        <a routerLink="/books" class="btn btn-primary" style="margin-top:16px">Shop Now</a>
      </div>
    </div>
  `,
  styles: [`
    .orders-list { display: flex; flex-direction: column; gap: 16px; padding-bottom: 48px; }
    .order-card { padding: 20px; }
    .order-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
    .order-id-group { display: flex; align-items: center; gap: 10px; }
    .order-id { font-weight: 700; font-size: 16px; }
    .order-date { font-size: 13px; color: var(--text-muted); }
    .order-body { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
    .order-item { display: flex; align-items: center; gap: 14px; flex: 1; }
    .item-thumb { width: 48px; height: 48px; background: var(--bg); border-radius: 8px; display: flex; align-items: center; justify-content: center; color: var(--primary); font-size: 18px; }
    .order-item h3 { font-size: 15px; font-weight: 600; font-family: 'Inter', sans-serif; }
    .order-item p { font-size: 13px; color: var(--text-muted); margin-top: 4px; }
    .order-amount { font-size: 18px; font-weight: 700; color: var(--primary); }
    .order-footer { padding-top: 10px; border-top: 1px solid var(--border); font-size: 13px; color: var(--text-muted); display: flex; align-items: center; gap: 8px; margin-bottom: 12px; i { color: var(--accent); } }
    .order-actions { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 8px; }

    /* Tracking Timeline */
    .tracking-timeline { margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--border); }
    .timeline { display: flex; align-items: flex-start; gap: 0; overflow-x: auto; padding-bottom: 4px; }
    .tl-step {
      display: flex; flex-direction: column; align-items: center; flex: 1;
      min-width: 80px; position: relative;
      &::before {
        content: ''; position: absolute; top: 18px; left: 50%; right: -50%;
        height: 2px; background: var(--border); z-index: 0;
      }
      &:last-child::before { display: none; }
      &.done::before { background: var(--success); }
    }
    .tl-icon {
      width: 36px; height: 36px; border-radius: 50%; border: 2px solid var(--border);
      background: white; display: flex; align-items: center; justify-content: center;
      font-size: 14px; color: var(--text-muted); position: relative; z-index: 1;
    }
    .tl-step.done .tl-icon { border-color: var(--success); color: var(--success); background: #f0fff4; }
    .tl-step.active .tl-icon { border-color: var(--primary); color: var(--primary); background: #ebf4ff; }
    .tl-label { font-size: 11px; text-align: center; margin-top: 6px; color: var(--text-muted); }
    .tl-step.done .tl-label, .tl-step.active .tl-label { color: var(--primary); font-weight: 600; }
  `]
})
export class OrdersComponent implements OnInit {
  orders: Order[] = [];
  loading = true;
  placed = false;
  trackingOpen: number | null = null;

  constructor(
    private orderSvc: OrderService,
    private auth: AuthService,
    private route: ActivatedRoute,
    private notifSvc: NotificationService
  ) {}

  ngOnInit() {
    this.placed = !!this.route.snapshot.queryParams['placed'];
    const uid = this.auth.currentUser!.userId;
    this.orderSvc.getByUser(uid).subscribe({
      next: o => { this.orders = o.reverse(); this.loading = false; },
      error: () => this.loading = false
    });
  }

  downloadInvoice(order: Order) {
    const content = this.buildInvoiceHtml(order);
    const blob = new Blob([content], { type: 'text/html' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `BookNest_Invoice_#${order.orderId}.html`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }

  private buildInvoiceHtml(o: Order): string {
    const user = this.auth.currentUser!;
    const addr = o.address;
    const addrStr = addr
      ? `${addr.flatNumber}, ${addr.city}, ${addr.state} - ${addr.pincode}`
      : 'N/A';
    return `<!DOCTYPE html>
<html><head><meta charset="UTF-8">
<title>Invoice #${o.orderId} — BookNest</title>
<style>
  body { font-family: Arial, sans-serif; max-width: 700px; margin: 40px auto; color: #333; }
  .header { background: #1a237e; color: white; padding: 32px; border-radius: 8px 8px 0 0; display: flex; justify-content: space-between; align-items: center; }
  .header h1 { margin: 0; font-size: 28px; } .header p { margin: 4px 0; opacity: 0.85; }
  .body { border: 1px solid #e0e0e0; border-top: none; border-radius: 0 0 8px 8px; padding: 32px; }
  .info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; margin-bottom: 32px; }
  .info-box h3 { font-size: 13px; text-transform: uppercase; color: #888; margin-bottom: 8px; }
  .info-box p { margin: 2px 0; font-size: 14px; }
  table { width: 100%; border-collapse: collapse; margin-bottom: 24px; }
  th { background: #f5f5f5; padding: 10px 12px; text-align: left; font-size: 13px; }
  td { padding: 12px; border-bottom: 1px solid #eee; font-size: 14px; }
  .total-row td { font-weight: bold; font-size: 16px; border-bottom: none; color: #1a237e; }
  .badge { display: inline-block; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; background: #e3f2fd; color: #1a237e; }
  .footer { text-align: center; margin-top: 32px; font-size: 12px; color: #aaa; }
</style>
</head><body>
<div class="header">
  <div><h1>BookNest</h1><p>Discover. Read. Belong.</p></div>
  <div style="text-align:right"><h2 style="margin:0;font-size:20px">Invoice</h2><p>#${o.orderId}</p></div>
</div>
<div class="body">
  <div class="info-grid">
    <div class="info-box">
      <h3>Billed To</h3>
      <p><strong>${user.fullName}</strong></p>
      <p>${user.email}</p>
      <p>${addrStr}</p>
    </div>
    <div class="info-box">
      <h3>Order Details</h3>
      <p><strong>Order Date:</strong> ${o.orderDate}</p>
      <p><strong>Status:</strong> <span class="badge">${o.orderStatus}</span></p>
      <p><strong>Payment:</strong> ${o.modeOfPayment}</p>
    </div>
  </div>
  <table>
    <thead><tr><th>Book</th><th>Qty</th><th>Unit Price</th><th>Total</th></tr></thead>
    <tbody>
      <tr>
        <td>${o.bookTitle || 'Book #' + o.bookId}</td>
        <td>${o.quantity}</td>
        <td>₹${(o.amountPaid / o.quantity).toFixed(2)}</td>
        <td>₹${o.amountPaid.toFixed(2)}</td>
      </tr>
    </tbody>
    <tfoot>
      <tr class="total-row"><td colspan="3">Total Amount</td><td>₹${o.amountPaid.toFixed(2)}</td></tr>
    </tfoot>
  </table>
  <p style="font-size:13px;color:#888">Payment Method: <strong>${o.modeOfPayment}</strong></p>
  <div class="footer">
    <p>Thank you for shopping at BookNest! 📚</p>
    <p>For support, contact us at support@booknest.com</p>
  </div>
</div>
</body></html>`;
  }

  toggleTrack(orderId: number) {
    this.trackingOpen = this.trackingOpen === orderId ? null : orderId;
  }

  getSteps(status: string): { label: string; icon: string; done: boolean; active: boolean }[] {
    const pipeline = ['PLACED', 'CONFIRMED', 'DISPATCHED', 'DELIVERED'];
    const icons = ['fas fa-check', 'fas fa-thumbs-up', 'fas fa-truck', 'fas fa-home'];
    const labels = ['Placed', 'Confirmed', 'Dispatched', 'Delivered'];
    const currentIdx = pipeline.indexOf(status);
    const isCancelled = status === 'CANCELLED';
    return pipeline.map((s, i) => ({
      label: labels[i],
      icon: icons[i],
      done: !isCancelled && i < currentIdx,
      active: !isCancelled && i === currentIdx
    }));
  }

  statusClass(s: string): string {
    const m: Record<string, string> = {
      PLACED: 'badge-info', CONFIRMED: 'badge-warning', DISPATCHED: 'badge-warning',
      DELIVERED: 'badge-success', CANCELLED: 'badge-error'
    };
    return m[s?.toUpperCase()] || 'badge-secondary';
  }
}
