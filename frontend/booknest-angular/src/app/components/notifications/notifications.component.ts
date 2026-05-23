import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../services/services';
import { AuthService } from '../../services/auth.service';
import { Notification } from '../../models/models';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container">
      <div class="page-header" style="display:flex;align-items:center;justify-content:space-between">
        <div>
          <h1><i class="fas fa-bell"></i> Notifications</h1>
          <p>{{unread}} unread</p>
        </div>
        <button class="btn btn-outline btn-sm" (click)="markAll()" *ngIf="unread">Mark all read</button>
      </div>

      <div class="loading" *ngIf="loading"><div class="spinner"></div></div>

      <div class="notif-list" *ngIf="!loading && notifications.length">
        <div class="notif-item" *ngFor="let n of notifications" [class.unread]="!n.isRead">
          <div class="notif-icon" [ngClass]="iconClass(n.type)">
            <i [class]="typeIcon(n.type)"></i>
          </div>
          <div class="notif-content">
            <p>{{n.message}}</p>
            <span class="notif-time">{{n.createdAt | date:'medium'}}</span>
          </div>
          <button class="delete-btn" (click)="delete(n.notificationId)"><i class="fas fa-times"></i></button>
        </div>
      </div>

      <div class="empty-state" *ngIf="!loading && !notifications.length">
        <i class="fas fa-bell-slash"></i>
        <h3>No notifications</h3>
        <p>You're all caught up!</p>
      </div>
    </div>
  `,
  styles: [`
    .notif-list { display: flex; flex-direction: column; gap: 8px; padding-bottom: 48px; }
    .notif-item {
      display: flex; align-items: center; gap: 16px;
      background: white; border: 1px solid var(--border); border-radius: 12px; padding: 16px;
      transition: background 0.2s;
      &.unread { background: #f0f4ff; border-color: var(--primary-light); }
    }
    .notif-icon { width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 16px; flex-shrink: 0; }
    .icon-order { background: #bee3f8; color: #2b6cb0; }
    .icon-payment { background: #c6f6d5; color: #276749; }
    .icon-alert { background: #fef3c7; color: #744210; }
    .icon-default { background: var(--bg); color: var(--text-muted); }
    .notif-content { flex: 1; p { font-size: 14px; line-height: 1.5; } }
    .notif-time { font-size: 12px; color: var(--text-muted); margin-top: 4px; display: block; }
    .delete-btn { background: none; border: none; color: var(--text-muted); cursor: pointer; padding: 4px; &:hover { color: var(--error); } }
  `]
})
export class NotificationsComponent implements OnInit {
  notifications: Notification[] = [];
  loading = true;
  unread = 0;

  constructor(private notifSvc: NotificationService, private auth: AuthService) {}

  ngOnInit() {
    const uid = this.auth.currentUser!.userId;
    this.notifSvc.getByUser(uid).subscribe({
      next: n => {
        this.notifications = n.reverse();
        this.unread = n.filter(x => !x.isRead).length;
        this.loading = false;
        // auto mark all read
        if (this.unread) this.notifSvc.markAllRead(uid).subscribe({ error: () => {} });
      },
      error: () => this.loading = false
    });
  }

  markAll() {
    const uid = this.auth.currentUser!.userId;
    this.notifSvc.markAllRead(uid).subscribe({ next: () => { this.notifications.forEach(n => n.isRead = true); this.unread = 0; }, error: () => {} });
  }

  delete(id: number) {
    this.notifSvc.deleteNotification(id).subscribe({ next: () => { this.notifications = this.notifications.filter(n => n.notificationId !== id); }, error: () => {} });
  }

  typeIcon(type: string): string {
    if (type?.includes('ORDER')) return 'fas fa-box';
    if (type?.includes('PAYMENT')) return 'fas fa-check-circle';
    return 'fas fa-bell';
  }

  iconClass(type: string): string {
    if (type?.includes('ORDER')) return 'icon-order';
    if (type?.includes('PAYMENT')) return 'icon-payment';
    return 'icon-default';
  }
}
