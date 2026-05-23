import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="auth-page">
      <div class="auth-card card">
        <div class="auth-header">
          <i class="fas fa-book-open brand-icon"></i>
          <h2>Welcome Back</h2>
          <p>Sign in to your BookNest account</p>
        </div>

        <div class="alert alert-error" *ngIf="error">{{error}}</div>
        <div class="alert alert-success" *ngIf="success">{{success}}</div>

        <form (ngSubmit)="submit()" #f="ngForm">
          <div class="form-group">
            <label>Email</label>
            <input type="email" [(ngModel)]="email" name="email" required placeholder="you@example.com">
          </div>
          <div class="form-group">
            <label>Password</label>
            <div class="pass-wrap">
              <input [type]="showPass ? 'text' : 'password'" [(ngModel)]="password" name="password" required placeholder="••••••••">
              <button type="button" class="show-pass" (click)="showPass=!showPass">
                <i [class]="showPass ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
              </button>
            </div>
          </div>
          <button type="submit" class="btn btn-primary btn-lg w-full" [disabled]="loading">
            <span *ngIf="loading" class="spinner-sm"></span>
            {{loading ? 'Signing in...' : 'Sign In'}}
          </button>
        </form>

        <!-- Divider -->
        <div class="divider">
          <span>or continue with</span>
        </div>

        <!-- GitHub OAuth Button -->
        <a href="http://localhost:8081/oauth2/authorization/github" class="btn btn-github w-full">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor" style="margin-right:8px">
            <path d="M12 0C5.374 0 0 5.373 0 12c0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23A11.509 11.509 0 0112 5.803c1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576C20.566 21.797 24 17.3 24 12c0-6.627-5.373-12-12-12z"/>
          </svg>
          Login with GitHub
        </a>

        <div class="auth-footer">
          <p><a routerLink="/auth/forgot-password" class="forgot-link">Forgot Password?</a></p>
          <p>Don't have an account? <a routerLink="/auth/register">Sign up</a></p>
          <p>Admin? <a routerLink="/auth/register-admin">Register as Admin</a></p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .auth-page { min-height: 80vh; display: flex; align-items: center; justify-content: center; padding: 40px 20px; }
    .auth-card { max-width: 420px; width: 100%; }
    .auth-header { text-align: center; margin-bottom: 28px; }
    .brand-icon { font-size: 36px; color: var(--accent); margin-bottom: 12px; display: block; }
    .auth-header h2 { font-size: 1.8rem; color: var(--primary); }
    .auth-header p { color: var(--text-muted); margin-top: 6px; font-size: 14px; }
    .pass-wrap { position: relative; }
    .pass-wrap input { padding-right: 40px; }
    .show-pass { position: absolute; right: 12px; top: 50%; transform: translateY(-50%); background: none; border: none; cursor: pointer; color: var(--text-muted); }
    .w-full { width: 100%; justify-content: center; margin-top: 8px; }
    .auth-footer { text-align: center; margin-top: 20px; font-size: 14px; color: var(--text-muted); }
    .auth-footer p { margin-top: 6px; }
    .auth-footer a { color: var(--primary); font-weight: 500; }
    .forgot-link { color: var(--accent) !important; font-size: 13px; }
    .spinner-sm { width: 16px; height: 16px; border: 2px solid rgba(255,255,255,0.4); border-top-color: white; border-radius: 50%; animation: spin 0.7s linear infinite; display: inline-block; }
    @keyframes spin { to { transform: rotate(360deg); } }

    /* Divider */
    .divider { display: flex; align-items: center; gap: 12px; margin: 20px 0; }
    .divider::before, .divider::after { content: ''; flex: 1; height: 1px; background: var(--border-color, #dee2e6); }
    .divider span { font-size: 13px; color: var(--text-muted); white-space: nowrap; }

    /* GitHub Button */
    .btn-github {
      display: flex; align-items: center; justify-content: center;
      background: #24292e; color: #fff; border: none; border-radius: 8px;
      padding: 10px 20px; font-size: 15px; font-weight: 600; cursor: pointer;
      text-decoration: none; transition: background 0.2s;
    }
    .btn-github:hover { background: #1a1f24; color: #fff; }
  `]
})
export class LoginComponent {
  email = ''; password = ''; error = ''; success = ''; loading = false; showPass = false;

  constructor(private auth: AuthService, private router: Router) {
    const nav = this.router.getCurrentNavigation();
    if (nav?.extras?.queryParams?.['registered'])   this.success = 'Account created! Please sign in.';
    if (nav?.extras?.queryParams?.['adminCreated']) this.success = 'Admin account created! Please sign in.';
    if (nav?.extras?.queryParams?.['logout'])       this.success = 'Logged out successfully.';
    if (nav?.extras?.queryParams?.['forbidden'])    this.error   = 'Access denied. Admin login required.';
  }

  submit() {
    this.error = ''; this.loading = true;
    this.auth.login({ email: this.email, password: this.password }).subscribe({
      next: () => { this.loading = false; this.router.navigate(['/']); },
      error: e => {
        this.loading = false;
        let err = e?.error;
        if (typeof err === 'string') { try { err = JSON.parse(err); } catch (_) {} }
        this.error = (typeof err === 'object' ? err?.message || err?.error : err) || 'Invalid email or password.';
      }
    });
  }
}
