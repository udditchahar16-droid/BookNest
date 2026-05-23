import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BookService } from '../../services/book.service';
import { Book } from '../../models/models';

@Component({
  selector: 'app-admin-books',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="container">
      <div class="page-header" style="display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:12px">
        <div>
          <h1><i class="fas fa-book"></i> Manage Books</h1>
          <p>{{books.length}} books in catalog</p>
        </div>
        <a routerLink="/admin/books/add" class="btn btn-accent">
          <i class="fas fa-plus"></i> Add Book
        </a>
      </div>

      <div class="filter-bar">
        <input type="text" [(ngModel)]="search" placeholder="Search books..." class="search-input">
      </div>

      <div class="loading" *ngIf="loading"><div class="spinner"></div></div>

      <div class="alert alert-success" *ngIf="deleted">Book deleted successfully.</div>

      <div class="card table-card" *ngIf="!loading">
        <table>
          <thead>
            <tr>
              <th>Cover</th>
              <th>Title</th>
              <th>Author</th>
              <th>Genre</th>
              <th>Price</th>
              <th>Stock</th>
              <th>Rating</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let b of filtered">
              <td>
                <img [src]="b.coverImageUrl || 'https://placehold.co/40x56/2c3e6b/white?text=📚'"
                     style="width:40px;height:56px;object-fit:cover;border-radius:4px"
                     onerror="this.src='https://placehold.co/40x56/2c3e6b/white?text=📚'">
              </td>
              <td><strong>{{b.title}}</strong></td>
              <td>{{b.author}}</td>
              <td><span class="badge badge-secondary" *ngIf="b.genre">{{b.genre}}</span></td>
              <td>₹{{b.price}}</td>
              <td>
                <span class="badge" [class]="b.stock > 5 ? 'badge-success' : b.stock > 0 ? 'badge-warning' : 'badge-error'">
                  {{b.stock}}
                </span>
              </td>
              <td>{{b.rating ? (b.rating | number:'1.1-1') : '—'}}</td>
              <td>
                <div style="display:flex;gap:8px">
                  <a [routerLink]="['/admin/books/edit', b.bookId]" class="btn btn-outline btn-sm">
                    <i class="fas fa-edit"></i>
                  </a>
                  <button class="btn btn-danger btn-sm" (click)="delete(b.bookId)">
                    <i class="fas fa-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr *ngIf="!filtered.length">
              <td colspan="8" style="text-align:center;color:var(--text-muted);padding:32px">No books found</td>
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
  `]
})
export class AdminBooksComponent implements OnInit {
  books: Book[] = [];
  search = '';
  loading = true;
  deleted = false;

  get filtered(): Book[] {
    if (!this.search) return this.books;
    const s = this.search.toLowerCase();
    return this.books.filter(b => b.title.toLowerCase().includes(s) || b.author.toLowerCase().includes(s) || (b.genre || '').toLowerCase().includes(s));
  }

  constructor(private bookSvc: BookService) {}

  ngOnInit() {
    this.bookSvc.getAll().subscribe({ next: b => { this.books = b; this.loading = false; }, error: () => this.loading = false });
  }

  delete(id: number) {
    if (!confirm('Delete this book?')) return;
    this.bookSvc.deleteBook(id).subscribe({
      next: () => { this.books = this.books.filter(b => b.bookId !== id); this.deleted = true; setTimeout(() => this.deleted = false, 3000); },
      error: () => {}
    });
  }
}
