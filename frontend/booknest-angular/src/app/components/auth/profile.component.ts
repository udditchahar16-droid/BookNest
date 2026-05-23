import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/models';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="page-header">
        <h1>My Profile</h1>
        <p>Manage your account details</p>
      </div>
      <div class="profile-grid">
        <div class="card profile-card">
          <div class="avatar-big">{{user?.fullName?.[0]}}</div>
          <h3>{{user?.fullName}}</h3>
          <p class="email">{{user?.email}}</p>
          <span class="badge" [class]="user?.role === 'ADMIN' ? 'badge-info' : 'badge-success'">{{user?.role}}</span>
        </div>
        <div class="card edit-card">
          <h3 style="margin-bottom:20px;font-family:'Inter',sans-serif;font-size:18px;">Edit Details</h3>
          <div class="alert alert-success" *ngIf="success">Profile updated successfully!</div>
          <div class="alert alert-error" *ngIf="error">{{error}}</div>
          <form (ngSubmit)="save()">
            <div class="form-group">
              <label>Full Name</label>
              <input type="text" [(ngModel)]="form.fullName" name="fullName">
            </div>
            <div class="form-group">
              <label>Mobile</label>
              <input type="tel" [(ngModel)]="form.mobile" name="mobile">
            </div>
            <div class="form-group">
              <label>Email (read-only)</label>
              <input type="email" [value]="user?.email" disabled>
            </div>
            <button type="submit" class="btn btn-primary" [disabled]="saving">
              {{saving ? 'Saving...' : 'Save Changes'}}
            </button>
          </form>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .profile-grid { display: grid; grid-template-columns: 280px 1fr; gap: 24px; }
    .profile-card { text-align: center; padding: 40px 24px; }
    .avatar-big {
      width: 80px; height: 80px; border-radius: 50%;
      background: var(--primary); color: white;
      font-size: 32px; font-weight: 700;
      display: flex; align-items: center; justify-content: center;
      margin: 0 auto 16px;
    }
    .profile-card h3 { font-family: 'Inter', sans-serif; font-size: 20px; }
    .email { color: var(--text-muted); font-size: 14px; margin: 8px 0 12px; }
    @media(max-width:768px){ .profile-grid { grid-template-columns: 1fr; } }
  `]
})
export class ProfileComponent implements OnInit {
  user: User | null = null;
  form: any = {};
  saving = false; success = false; error = '';

  constructor(private auth: AuthService) {}

  ngOnInit() {
    this.user = this.auth.currentUser;
    if (this.user) this.form = { fullName: this.user.fullName, mobile: this.user.mobile };
  }

  save() {
    if (!this.user) return;
    this.saving = true; this.success = false; this.error = '';
    this.auth.updateUser(this.user.userId, this.form).subscribe({
      next: u => { this.user = u; this.saving = false; this.success = true; },
      error: () => { this.saving = false; this.error = 'Update failed.'; }
    });
  }
}
