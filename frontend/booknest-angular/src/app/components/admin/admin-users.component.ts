import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/models';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="page-header">
        <h1><i class="fas fa-users"></i> Manage Users</h1>
        <p>{{users.length}} customers registered</p>
      </div>

      <div class="filter-bar">
        <input type="text" [(ngModel)]="search" placeholder="Search by name or email..." class="search-input">
      </div>

      <div class="loading" *ngIf="loading"><div class="spinner"></div></div>

      <div class="card table-card" *ngIf="!loading">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Email</th>
              <th>Mobile</th>
              <th>Role</th>
              <th>Joined</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let u of filtered">
              <td>{{u.userId}}</td>
              <td>
                <div style="display:flex;align-items:center;gap:10px">
                  <div class="mini-avatar">{{u.fullName[0]}}</div>
                  {{u.fullName}}
                </div>
              </td>
              <td>{{u.email}}</td>
              <td>{{u.mobile || '—'}}</td>
              <td><span class="badge" [class]="u.role === 'ADMIN' ? 'badge-info' : 'badge-success'">{{u.role}}</span></td>
              <td>{{u.createdAt | date:'shortDate'}}</td>
              <td>
                <button class="btn btn-danger btn-sm" (click)="deleteUser(u.userId)" *ngIf="u.role !== 'ADMIN'">
                  <i class="fas fa-trash"></i> Delete
                </button>
              </td>
            </tr>
            <tr *ngIf="!filtered.length">
              <td colspan="7" style="text-align:center;color:var(--text-muted);padding:32px">No users found</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .filter-bar { margin-bottom: 16px; }
    .search-input { padding: 10px 16px; border: 1px solid var(--border); border-radius: 8px; font-size: 14px; width: 320px; &:focus { outline: none; border-color: var(--primary); } }
    .table-card { padding: 0; overflow: hidden; }
    table { margin: 0; }
    .mini-avatar { width: 28px; height: 28px; border-radius: 50%; background: var(--primary); color: white; font-size: 11px; font-weight: 700; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
  `]
})
export class AdminUsersComponent implements OnInit {
  users: User[] = [];
  loading = true;
  search = '';

  get filtered(): User[] {
    if (!this.search) return this.users;
    const s = this.search.toLowerCase();
    return this.users.filter(u => u.fullName.toLowerCase().includes(s) || u.email.toLowerCase().includes(s));
  }

  constructor(private auth: AuthService) {}

  ngOnInit() {
    this.auth.getUsersByRole('CUSTOMER').subscribe({ next: u => { this.users = u; this.loading = false; }, error: () => this.loading = false });
  }

  deleteUser(id: number) {
    if (!confirm('Delete this user account?')) return;
    this.auth.deleteUser(id).subscribe({ next: () => { this.users = this.users.filter(u => u.userId !== id); }, error: () => {} });
  }
}
