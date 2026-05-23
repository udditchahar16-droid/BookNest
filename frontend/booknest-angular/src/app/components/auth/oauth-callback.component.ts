import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

/**
 * GitHub OAuth Callback Handler
 *
 * Flow:
 * 1. User clicks "Login with GitHub" → redirected to auth-service OAuth2 endpoint
 * 2. GitHub authenticates → auth-service creates/finds user, generates JWT
 * 3. auth-service redirects to http://localhost:4200/auth/oauth-callback?token=...&email=...&userId=...&name=...&role=...
 * 4. THIS component reads URL params, stores token + user in localStorage, redirects to home
 */
@Component({
  selector: 'app-oauth-callback',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div style="min-height:80vh;display:flex;align-items:center;justify-content:center">
      <div class="text-center">
        <div class="spinner-border text-primary mb-3" style="width:3rem;height:3rem"></div>
        <h5 class="text-muted">{{ message }}</h5>
        <p class="text-muted small" *ngIf="errorMsg" class="text-danger">{{ errorMsg }}</p>
      </div>
    </div>
  `
})
export class OAuthCallbackComponent implements OnInit {
  message = 'Completing GitHub login…';
  errorMsg = '';

  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    // Read query params from URL
    const params = new URLSearchParams(window.location.search);
    const token  = params.get('token');
    const email  = params.get('email');
    const name   = params.get('name') || '';
    const userId = Number(params.get('userId'));
    const role   = params.get('role') || 'USER';

    if (!token || !email || !userId) {
      this.message = 'GitHub login failed.';
      this.errorMsg = 'Missing authentication data from GitHub OAuth.';
      setTimeout(() => this.router.navigate(['/auth/login']), 2500);
      return;
    }

    // Store token and user in localStorage (same as normal login)
    localStorage.setItem('booknest_token', token);

    const user = {
      userId, fullName: name, email,
      mobile: '', role, provider: 'github', createdAt: new Date().toISOString()
    };
    localStorage.setItem('booknest_user', JSON.stringify(user));

    // Notify AuthService so navbar updates immediately
    this.auth.setCurrentUser(user as any);

    // Auto-create wallet for new GitHub users (safe — wallet-service ignores if already exists)
    this.auth.createWallet(userId);

    this.message = '✅ GitHub login successful! Redirecting…';
    setTimeout(() => this.router.navigate(['/']), 800);
  }
}
