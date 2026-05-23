import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BookService } from '../../services/book.service';
import { Book } from '../../models/models';

@Component({
  selector: 'app-admin-inventory',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="page-header">
        <h1><i class="fas fa-warehouse"></i> Inventory Management</h1>
        <p>Monitor and update stock levels</p>
      </div>

      <div class="inv-summary">
        <div class="inv-stat card">
          <i class="fas fa-check-circle" style="color:var(--success)"></i>
          <div>
            <strong>{{inStock}}</strong>
            <span>In Stock</span>
          </div>
        </div>
        <div class="inv-stat card">
          <i class="fas fa-exclamation-triangle" style="color:var(--warning)"></i>
          <div>
            <strong>{{lowStock}}</strong>
            <span>Low Stock (&lt;5)</span>
          </div>
        </div>
        <div class="inv-stat card">
          <i class="fas fa-times-circle" style="color:var(--error)"></i>
          <div>
            <strong>{{outOfStock}}</strong>
            <span>Out of Stock</span>
          </div>
        </div>
      </div>

      <div class="loading" *ngIf="loading"><div class="spinner"></div></div>
      <div class="alert alert-success" *ngIf="updated">Stock updated successfully!</div>

      <div class="card table-card" *ngIf="!loading">
        <table>
          <thead>
            <tr>
              <th>Title</th>
              <th>Author</th>
              <th>Genre</th>
              <th>Price</th>
              <th>Current Stock</th>
              <th>Status</th>
              <th>New Stock</th>
              <th>Update</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let b of books" [class.low-stock-row]="b.stock > 0 && b.stock < 5" [class.out-stock-row]="b.stock === 0">
              <td><strong>{{b.title}}</strong></td>
              <td>{{b.author}}</td>
              <td>{{b.genre || '—'}}</td>
              <td>₹{{b.price}}</td>
              <td class="stock-cell">{{b.stock}}</td>
              <td>
                <span class="badge" [class]="b.stock > 5 ? 'badge-success' : b.stock > 0 ? 'badge-warning' : 'badge-error'">
                  {{b.stock > 5 ? 'In Stock' : b.stock > 0 ? 'Low Stock' : 'Out of Stock'}}
                </span>
              </td>
              <td>
                <input type="number" class="stock-input" [(ngModel)]="newStock[b.bookId]" min="0" placeholder="New qty">
              </td>
              <td>
                <button class="btn btn-primary btn-sm" (click)="updateStock(b)" [disabled]="newStock[b.bookId] === undefined || newStock[b.bookId] === null">
                  <i class="fas fa-sync"></i> Update
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .inv-summary { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 24px; }
    .inv-stat { display: flex; align-items: center; gap: 16px; padding: 20px; i { font-size: 24px; } strong { display: block; font-size: 1.6rem; font-weight: 700; color: var(--text); } span { font-size: 13px; color: var(--text-muted); }  }
    .table-card { padding: 0; overflow: hidden; overflow-x: auto; }
    table { margin: 0; }
    .stock-input { width: 80px; padding: 6px 10px; border: 1px solid var(--border); border-radius: 6px; font-size: 13px; }
    .stock-cell { font-weight: 700; font-size: 15px; }
    .low-stock-row { background: #fffbf0 !important; }
    .out-stock-row { background: #fff5f5 !important; }
    @media(max-width:768px){ .inv-summary { grid-template-columns: 1fr; } }
  `]
})
export class AdminInventoryComponent implements OnInit {
  books: Book[] = [];
  loading = true;
  updated = false;
  newStock: Record<number, number> = {};

  get inStock(): number { return this.books.filter(b => b.stock > 5).length; }
  get lowStock(): number { return this.books.filter(b => b.stock > 0 && b.stock <= 5).length; }
  get outOfStock(): number { return this.books.filter(b => b.stock === 0).length; }

  constructor(private bookSvc: BookService) {}

  ngOnInit() {
    this.bookSvc.getAll().subscribe({ next: b => { this.books = b; this.loading = false; }, error: () => this.loading = false });
  }

  updateStock(book: Book) {
    const stock = this.newStock[book.bookId];
    if (stock === undefined || stock === null) return;
    this.bookSvc.updateStock(book.bookId, stock).subscribe({
      next: () => {
        book.stock = stock;
        delete this.newStock[book.bookId];
        this.updated = true;
        setTimeout(() => this.updated = false, 3000);
      },
      error: () => {}
    });
  }
}
