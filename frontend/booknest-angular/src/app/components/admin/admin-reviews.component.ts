import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReviewService } from '../../services/services';
import { AuthService } from '../../services/auth.service';
import { Review, User } from '../../models/models';

@Component({
  selector: 'app-admin-reviews',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container">
      <div class="page-header">
        <h1><i class="fas fa-star"></i> Moderate Reviews</h1>
        <p>{{reviews.length}} total reviews</p>
      </div>

      <div class="loading" *ngIf="loading"><div class="spinner"></div></div>

      <div class="card table-card" *ngIf="!loading">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Book ID</th>
              <th>User</th>
              <th>Rating</th>
              <th>Comment</th>
              <th>Verified</th>
              <th>Date</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let r of reviews">
              <td>{{r.reviewId}}</td>
              <td>{{r.bookId}}</td>
              <td>
                <div class="user-cell">
                  <div class="user-avatar">{{getInitials(r.userId)}}</div>
                  <span>{{getUserName(r.userId)}}</span>
                </div>
              </td>
              <td>
                <span class="stars">
                  <span *ngFor="let s of getStars(r.rating)">★</span>
                  <span class="empty" *ngFor="let s of getEmptyStars(r.rating)">★</span>
                </span>
                {{r.rating}}/5
              </td>
              <td class="comment-cell">{{r.comment}}</td>
              <td><span class="badge" [class]="r.verified ? 'badge-success' : 'badge-secondary'">{{r.verified ? 'Verified' : 'Unverified'}}</span></td>
              <td>{{r.reviewDate | date:'shortDate'}}</td>
              <td>
                <button class="btn btn-danger btn-sm" (click)="delete(r.reviewId)" [disabled]="deletingId === r.reviewId">
                  <i class="fas fa-trash"></i> {{deletingId === r.reviewId ? 'Removing...' : 'Remove'}}
                </button>
              </td>
            </tr>
            <tr *ngIf="!reviews.length">
              <td colspan="8" style="text-align:center;color:var(--text-muted);padding:32px">No reviews found</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .table-card { padding: 0; overflow: hidden; overflow-x: auto; }
    table { margin: 0; min-width: 800px; }
    .stars { color: #f6ad55; .empty { color: #e2e8f0; } }
    .comment-cell { max-width: 250px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .user-cell { display: flex; align-items: center; gap: 8px; }
    .user-avatar { width: 30px; height: 30px; border-radius: 50%; background: var(--primary); color: white; font-size: 11px; display: flex; align-items: center; justify-content: center; font-weight: 700; flex-shrink: 0; }
  `]
})
export class AdminReviewsComponent implements OnInit {
  reviews: Review[] = [];
  loading = true;
  deletingId: number | null = null;
  private userMap = new Map<number, User>();

  constructor(private reviewSvc: ReviewService, private auth: AuthService) {}

  ngOnInit() {
    this.reviewSvc.getAll().subscribe({
      next: r => {
        this.reviews = r;
        this.loading = false;
        // Load user details for all unique userIds
        const userIds = [...new Set(r.map(rv => rv.userId))];
        userIds.forEach(uid => {
          this.auth.getUser(uid).subscribe({
            next: u => this.userMap.set(uid, u),
            error: () => {}
          });
        });
      },
      error: () => this.loading = false
    });
  }

  getUserName(userId: number): string {
    const u = this.userMap.get(userId);
    return u ? u.fullName : `User #${userId}`;
  }

  getInitials(userId: number): string {
    const u = this.userMap.get(userId);
    if (u && u.fullName) {
      return u.fullName.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);
    }
    return 'U';
  }

  delete(id: number) {
    if (!confirm('Remove this review?')) return;
    this.deletingId = id;
    this.reviewSvc.deleteReview(id).subscribe({
      next: () => {
        this.reviews = this.reviews.filter(r => r.reviewId !== id);
        this.deletingId = null;
      },
      error: () => {
        alert('Failed to remove review. Please try again.');
        this.deletingId = null;
      }
    });
  }

  getStars(r: number): number[] { return Array(r).fill(0); }
  getEmptyStars(r: number): number[] { return Array(5 - r).fill(0); }
}
