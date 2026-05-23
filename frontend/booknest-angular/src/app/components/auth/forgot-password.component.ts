import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

type Step = 'email' | 'otp' | 'reset' | 'done';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="auth-page">
      <div class="auth-card card">
        <div class="auth-header">
          <i class="fas fa-key brand-icon"></i>
          <h2>Forgot Password</h2>
          <p>
            <span *ngIf="step === 'email'">Enter your registered email to receive an OTP.</span>
            <span *ngIf="step === 'otp'">Enter the 6-digit OTP sent to <strong>{{email}}</strong></span>
            <span *ngIf="step === 'reset'">Set your new password.</span>
            <span *ngIf="step === 'done'">Your password has been reset!</span>
          </p>
        </div>

        <div class="alert alert-error"   *ngIf="error">{{error}}</div>
        <div class="alert alert-success" *ngIf="success">{{success}}</div>

        <!-- Step 1: Email -->
        <div *ngIf="step === 'email'">
          <div class="form-group">
            <label>Email Address</label>
            <input type="email" [(ngModel)]="email" placeholder="you@example.com" (keyup.enter)="sendOtp()">
          </div>
          <button class="btn btn-primary btn-lg w-full" [disabled]="loading" (click)="sendOtp()">
            <span *ngIf="loading" class="spinner-sm"></span>
            {{loading ? 'Sending OTP...' : 'Send OTP'}}
          </button>
        </div>

        <!-- Step 2: OTP -->
        <div *ngIf="step === 'otp'">
          <div class="form-group">
            <label>OTP Code</label>
            <input type="text" [(ngModel)]="otp" placeholder="Enter 6-digit OTP" maxlength="6"
                   class="otp-input" (keyup.enter)="verifyOtp()">
          </div>
          <button class="btn btn-primary btn-lg w-full" [disabled]="loading" (click)="verifyOtp()">
            <span *ngIf="loading" class="spinner-sm"></span>
            {{loading ? 'Verifying...' : 'Verify OTP'}}
          </button>
          <button class="btn btn-outline btn-sm resend-btn" (click)="step='email'; error=''; success=''">
            ← Change Email
          </button>
        </div>

        <!-- Step 3: New Password -->
        <div *ngIf="step === 'reset'">
          <div class="form-group">
            <label>New Password</label>
            <div class="pass-wrap">
              <input [type]="showPass ? 'text' : 'password'" [(ngModel)]="newPassword"
                     placeholder="Minimum 6 characters" (keyup.enter)="resetPassword()">
              <button type="button" class="show-pass" (click)="showPass=!showPass">
                <i [class]="showPass ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
              </button>
            </div>
          </div>
          <div class="form-group">
            <label>Confirm New Password</label>
            <div class="pass-wrap">
              <input [type]="showPass2 ? 'text' : 'password'" [(ngModel)]="confirmPassword"
                     placeholder="Re-enter new password" (keyup.enter)="resetPassword()">
              <button type="button" class="show-pass" (click)="showPass2=!showPass2">
                <i [class]="showPass2 ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
              </button>
            </div>
          </div>
          <button class="btn btn-primary btn-lg w-full" [disabled]="loading" (click)="resetPassword()">
            <span *ngIf="loading" class="spinner-sm"></span>
            {{loading ? 'Resetting...' : 'Reset Password'}}
          </button>
        </div>

        <!-- Step 4: Done -->
        <div *ngIf="step === 'done'" class="done-block">
          <i class="fas fa-check-circle done-icon"></i>
          <p>Password reset successfully!</p>
          <a routerLink="/auth/login" class="btn btn-primary btn-lg w-full" style="margin-top:16px">
            Go to Login
          </a>
        </div>

        <!-- Back to login -->
        <div class="auth-footer" *ngIf="step !== 'done'">
          <p><a routerLink="/auth/login">← Back to Login</a></p>
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
    .w-full { width: 100%; justify-content: center; margin-top: 8px; }
    .auth-footer { text-align: center; margin-top: 20px; font-size: 14px; }
    .auth-footer a { color: var(--primary); font-weight: 500; }
    .otp-input { text-align: center; font-size: 24px; font-weight: 700; letter-spacing: 10px; }
    .resend-btn { display: block; margin-top: 12px; width: 100%; }
    .pass-wrap { position: relative; }
    .pass-wrap input { padding-right: 40px; }
    .show-pass { position: absolute; right: 12px; top: 50%; transform: translateY(-50%); background: none; border: none; cursor: pointer; color: var(--text-muted); }
    .spinner-sm { width: 16px; height: 16px; border: 2px solid rgba(255,255,255,0.4); border-top-color: white; border-radius: 50%; animation: spin 0.7s linear infinite; display: inline-block; }
    @keyframes spin { to { transform: rotate(360deg); } }
    .done-block { text-align: center; padding: 20px 0; }
    .done-icon { font-size: 56px; color: var(--success); margin-bottom: 16px; display: block; }
    .done-block p { font-size: 16px; color: var(--text-muted); }
  `]
})
export class ForgotPasswordComponent {
  step: Step = 'email';
  email = '';
  otp = '';
  newPassword = '';
  confirmPassword = '';
  loading = false;
  error = '';
  success = '';
  showPass = false;
  showPass2 = false;

  constructor(private http: HttpClient, private router: Router) {}

  sendOtp() {
    if (!this.email?.trim()) { this.error = 'Please enter your email.'; return; }
    this.loading = true; this.error = ''; this.success = '';
    this.http.post<any>('/api/auth/forgot-password', { email: this.email.trim() }).subscribe({
      next: res => {
        this.loading = false;
        this.success = 'OTP sent successfully! Please check your email (including spam folder).';
        this.step = 'otp';
      },
      error: e => {
        this.loading = false;
        this.error = e?.error?.error || 'Could not send OTP. Please try again.';
      }
    });
  }

  verifyOtp() {
    if (!this.otp?.trim()) { this.error = 'Please enter the OTP.'; return; }
    this.loading = true; this.error = ''; this.success = '';
    this.http.post<any>('/api/auth/verify-otp', { email: this.email.trim(), otp: this.otp.trim() }).subscribe({
      next: () => {
        this.loading = false;
        this.step = 'reset';
        this.success = '';
      },
      error: e => {
        this.loading = false;
        this.error = e?.error?.error || 'Invalid or expired OTP. Please try again.';
      }
    });
  }

  resetPassword() {
    if (!this.newPassword || this.newPassword.length < 6) {
      this.error = 'Password must be at least 6 characters.'; return;
    }
    if (this.newPassword !== this.confirmPassword) {
      this.error = 'Passwords do not match.'; return;
    }
    this.loading = true; this.error = '';
    this.http.post<any>('/api/auth/reset-password', {
      email: this.email.trim(),
      otp: this.otp.trim(),
      newPassword: this.newPassword
    }).subscribe({
      next: () => {
        this.loading = false;
        this.step = 'done';
      },
      error: e => {
        this.loading = false;
        this.error = e?.error?.error || 'Password reset failed. Please start over.';
      }
    });
  }
}
