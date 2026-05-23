import { Component, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/services';
import { User } from '../../models/models';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule],
  template: `
    <nav class="navbar">
      <div class="nav-inner">
        <a routerLink="/" class="brand">
          <i class="fas fa-book-open"></i>
          <span>BookNest</span>
        </a>

        <div class="nav-links">
          <a routerLink="/books" routerLinkActive="active">Books</a>
          <a routerLink="/books/featured" routerLinkActive="active">Featured</a>
          <ng-container *ngIf="user">
            <a routerLink="/cart" routerLinkActive="active"><i class="fas fa-shopping-cart"></i> Cart</a>
            <a routerLink="/wishlist" routerLinkActive="active"><i class="fas fa-heart"></i></a>
            <a routerLink="/notifications" routerLinkActive="active" class="notif-link">
              <i class="fas fa-bell"></i>
              <span class="badge-count" *ngIf="unreadCount > 0">{{unreadCount}}</span>
            </a>
          </ng-container>
        </div>

        <div class="nav-actions">
          <ng-container *ngIf="!user">
            <a routerLink="/auth/login" class="btn btn-outline btn-sm">Login</a>
            <a routerLink="/auth/register" class="btn btn-primary btn-sm">Sign Up</a>
          </ng-container>
          <ng-container *ngIf="user">
            <div class="user-menu" (click)="toggleMenu()" (keydown.enter)="toggleMenu()" tabindex="0">
              <div class="avatar">{{user.fullName[0]}}</div>
              <span class="user-name">{{user.fullName.split(' ')[0]}}</span>
              <i class="fas fa-chevron-down"></i>
              <div class="dropdown" *ngIf="menuOpen">
                <a routerLink="/profile" (click)="menuOpen=false"><i class="fas fa-user"></i> Profile</a>
                <a routerLink="/orders" (click)="menuOpen=false"><i class="fas fa-box"></i> My Orders</a>
                <a routerLink="/wallet" (click)="menuOpen=false"><i class="fas fa-wallet"></i> Wallet</a>
                <a routerLink="/wallet/statements" (click)="menuOpen=false"><i class="fas fa-file-alt"></i> Statements</a>
                <ng-container *ngIf="isAdmin">
                  <div class="dropdown-divider"></div>
                  <a routerLink="/admin" (click)="menuOpen=false"><i class="fas fa-shield-alt"></i> Admin Panel</a>
                </ng-container>
                <div class="dropdown-divider"></div>
                <button (click)="logout()"><i class="fas fa-sign-out-alt"></i> Logout</button>
              </div>
            </div>
          </ng-container>
        </div>
      </div>
    </nav>
  `,
  styles: [`
    .navbar {
      background: white;
      border-bottom: 1px solid var(--border);
      height: 64px;
      position: sticky;
      top: 0;
      z-index: 100;
      box-shadow: 0 1px 8px rgba(0,0,0,0.06);
    }
    .nav-inner {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 20px;
      height: 100%;
      display: flex;
      align-items: center;
      gap: 32px;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 10px;
      font-family: 'Playfair Display', serif;
      font-size: 22px;
      font-weight: 700;
      color: var(--primary);
      text-decoration: none;
      flex-shrink: 0;
      i { color: var(--accent); font-size: 20px; }
    }
    .nav-links {
      display: flex;
      align-items: center;
      gap: 4px;
      flex: 1;
      a {
        padding: 6px 12px;
        border-radius: 8px;
        font-size: 14px;
        font-weight: 500;
        color: var(--text-muted);
        transition: all 0.2s;
        text-decoration: none;
        position: relative;
        &:hover, &.active { background: var(--bg); color: var(--primary); }
      }
    }
    .notif-link { position: relative; }
    .badge-count {
      position: absolute;
      top: -4px;
      right: -4px;
      background: var(--accent);
      color: white;
      font-size: 10px;
      width: 18px;
      height: 18px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
    }
    .nav-actions { display: flex; align-items: center; gap: 8px; margin-left: auto; }
    .user-menu {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 6px 12px;
      border-radius: 8px;
      position: relative;
      transition: background 0.2s;
      &:hover { background: var(--bg); }
    }
    .avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: var(--primary);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 13px;
      font-weight: 700;
    }
    .user-name { font-size: 14px; font-weight: 500; color: var(--text); }
    .fa-chevron-down { font-size: 11px; color: var(--text-muted); }
    .dropdown {
      position: absolute;
      top: calc(100% + 8px);
      right: 0;
      background: white;
      border: 1px solid var(--border);
      border-radius: 12px;
      box-shadow: var(--shadow-lg);
      min-width: 200px;
      padding: 8px;
      a, button {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 10px 12px;
        border-radius: 8px;
        font-size: 14px;
        color: var(--text);
        text-decoration: none;
        width: 100%;
        background: none;
        border: none;
        cursor: pointer;
        font-family: 'Inter', sans-serif;
        transition: background 0.2s;
        i { color: var(--text-muted); width: 16px; }
        &:hover { background: var(--bg); }
      }
    }
    .dropdown-divider { height: 1px; background: var(--border); margin: 4px 0; }
  `]
})
export class NavbarComponent implements OnInit {
  user: User | null = null;
  menuOpen = false;
  unreadCount = 0;
  isAdmin = false;

  constructor(private auth: AuthService, private notifSvc: NotificationService) {}

  ngOnInit() {
    this.auth.user$.subscribe(u => {
      this.user = u;
      this.isAdmin = this.auth.isAdmin;
      if (u) this.loadUnread(u.userId);
    });
  }

  loadUnread(userId: number) {
    this.notifSvc.getUnreadCount(userId).subscribe({ next: n => this.unreadCount = n, error: () => {} });
  }

  toggleMenu() { this.menuOpen = !this.menuOpen; }

  logout() {
    this.auth.logout();
    this.menuOpen = false;
    window.location.href = '/';
  }
}
