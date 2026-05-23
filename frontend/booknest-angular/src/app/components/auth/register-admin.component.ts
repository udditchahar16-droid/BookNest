import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="auth-page">
      <div class="auth-card card">
        <div class="auth-header">
          <i class="fas fa-shield-alt brand-icon"></i>
          <h2>Admin Registration</h2>
          <p>Create an administrator account</p>
        </div>
        <div class="alert alert-error" *ngIf="error">{{error}}</div>
        <form (ngSubmit)="submit()">
          <div class="form-group">
            <label>Full Name</label>
            <input type="text" [(ngModel)]="form.fullName" name="fullName" required>
          </div>
          <div class="form-group">
            <label>Email</label>
            <input type="email" [(ngModel)]="form.email" name="email" required>
          </div>
          <div class="form-group">
            <label>Mobile</label>
            <input type="tel" [(ngModel)]="form.mobile" name="mobile" required>
          </div>
          <div class="form-group">
            <label>Password</label>
            <input type="password" [(ngModel)]="form.password" name="password" required>
          </div>
          <div class="form-group">
            <label>Admin Secret Key</label>
            <input type="password" [(ngModel)]="form.adminSecret" name="adminSecret" required placeholder="Enter admin secret">
          </div>
          <button type="submit" class="btn btn-primary btn-lg w-full" [disabled]="loading">
            {{loading ? 'Creating...' : 'Register Admin'}}
          </button>
        </form>
        <div class="auth-footer">
          <p><a routerLink="/auth/login">Back to Login</a></p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .auth-page { min-height: 80vh; display: flex; align-items: center; justify-content: center; padding: 40px 20px; }
    .auth-card { max-width: 420px; width: 100%; }
    .auth-header { text-align: center; margin-bottom: 28px; }
    .brand-icon { font-size: 36px; color: var(--primary); margin-bottom: 12px; display: block; }
    .auth-header h2 { font-size: 1.8rem; color: var(--primary); }
    .auth-header p { color: var(--text-muted); margin-top: 6px; font-size: 14px; }
    .w-full { width: 100%; justify-content: center; margin-top: 8px; }
    .auth-footer { text-align: center; margin-top: 20px; font-size: 14px; a { color: var(--primary); font-weight: 500; } }
  `]
})
export class RegisterAdminComponent {
  form = { fullName: '', email: '', mobile: '', password: '', adminSecret: '' };
  error = ''; loading = false;

  constructor(private auth: AuthService, private router: Router) {}

  submit() {
    this.error = ''; this.loading = true;
    try {
      this.auth.registerAdmin(this.form).subscribe({
        next: () => { this.loading = false; this.router.navigate(['/auth/login'], { queryParams: { registered: true } }); },
        error: e => { this.loading = false; this.error = e?.error?.message || 'Registration failed.'; }
      });
    } catch (e: any) {
      this.loading = false; this.error = e.message;
    }
  }
}
