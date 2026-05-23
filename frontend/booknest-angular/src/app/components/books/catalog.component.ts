import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BookService } from '../../services/book.service';
import { Book } from '../../models/models';

@Component({
  selector: 'app-catalog',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="container">
      <div class="catalog-top">
        <div class="page-header">
          <h1>{{title}}</h1>
          <p *ngIf="books.length">{{books.length}} books found</p>
        </div>
        <div class="search-controls">
          <div class="search-box">
            <i class="fas fa-search"></i>
            <input type="text" [(ngModel)]="keyword" placeholder="Search books..." (keyup.enter)="doSearch()">
            <button class="btn btn-accent btn-sm" (click)="doSearch()">Search</button>
          </div>
          <div class="genre-filter">
            <span class="filter-label">Genre:</span>
            <span *ngFor="let g of genres" class="genre-pill" [class.active]="activeGenre === g" (click)="filterGenre(g)">{{g}}</span>
            <span class="genre-pill" [class.active]="!activeGenre" (click)="clearFilter()">All</span>
          </div>
        </div>
      </div>

      <div class="loading" *ngIf="loading"><div class="spinner"></div> Loading...</div>

      <div class="books-grid" *ngIf="!loading && books.length">
        <div class="book-card" *ngFor="let b of books">
          <a [routerLink]="['/books', b.bookId]" class="book-link">
            <div class="book-cover">
              <img [src]="b.coverImageUrl || 'https://placehold.co/200x280/2c3e6b/white?text=📚'" [alt]="b.title" onerror="this.src='https://placehold.co/200x280/2c3e6b/white?text=📚'">
              <div class="out-of-stock" *ngIf="b.stock === 0">Out of Stock</div>
            </div>
            <div class="book-info">
              <h3 class="book-title">{{b.title}}</h3>
              <p class="author">by {{b.author}}</p>
              <p class="genre" *ngIf="b.genre">{{b.genre}}</p>
              <div class="book-footer">
                <span class="price">₹{{b.price}}</span>
                <span class="rating" *ngIf="b.rating">
                  <i class="fas fa-star"></i> {{b.rating | number:'1.1-1'}}
                </span>
              </div>
            </div>
          </a>
        </div>
      </div>

      <div class="empty-state" *ngIf="!loading && !books.length">
        <i class="fas fa-search"></i>
        <h3>No books found</h3>
        <p>Try a different search or browse all books</p>
        <a routerLink="/books" class="btn btn-primary" style="margin-top:16px">Browse All</a>
      </div>
    </div>
  `,
  styles: [`
    .catalog-top { padding: 32px 0 20px; }
    .search-controls { display: flex; flex-direction: column; gap: 12px; margin-bottom: 24px; }
    .search-box {
      display: flex; align-items: center; gap: 10px;
      background: white; border: 1px solid var(--border); border-radius: 8px; padding: 8px 12px;
      max-width: 500px;
      i { color: var(--text-muted); }
      input { flex: 1; border: none; outline: none; font-size: 14px; }
    }
    .genre-filter { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
    .filter-label { font-size: 13px; font-weight: 600; color: var(--text-muted); }
    .genre-pill {
      padding: 4px 12px; border-radius: 20px; font-size: 13px; cursor: pointer;
      background: var(--border); color: var(--text-muted); transition: all 0.2s;
      &:hover, &.active { background: var(--primary); color: white; }
    }
    .books-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 20px; padding-bottom: 48px; }
    .book-card {
      background: white; border-radius: 12px; overflow: hidden;
      border: 1px solid var(--border); transition: all 0.2s;
      &:hover { box-shadow: var(--shadow-lg); transform: translateY(-4px); }
    }
    .book-link { text-decoration: none; color: inherit; }
    .book-cover {
      position: relative; aspect-ratio: 3/4; overflow: hidden; background: var(--bg);
      img { width: 100%; height: 100%; object-fit: cover; }
    }
    .out-of-stock {
      position: absolute; inset: 0; background: rgba(0,0,0,0.5);
      color: white; display: flex; align-items: center; justify-content: center;
      font-size: 13px; font-weight: 600;
    }
    .book-info { padding: 14px; }
    .book-title { font-size: 14px; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-family: 'Inter', sans-serif; margin-bottom: 4px; }
    .author { font-size: 12px; color: var(--text-muted); margin-bottom: 2px; }
    .genre { font-size: 11px; color: var(--accent); margin-bottom: 8px; }
    .book-footer { display: flex; align-items: center; justify-content: space-between; }
    .price { font-size: 16px; font-weight: 700; color: var(--primary); }
    .rating { font-size: 12px; color: var(--text-muted); i { color: #f6ad55; } }
  `]
})
export class CatalogComponent implements OnInit {
  books: Book[] = [];
  loading = true;
  title = 'All Books';
  keyword = '';
  activeGenre = '';
  genres = ['Fiction', 'Non-Fiction', 'Science', 'History', 'Mystery', 'Fantasy', 'Romance', 'Biography', 'Self-Help', 'Technology'];

  constructor(private bookSvc: BookService, private route: ActivatedRoute, private router: Router) {}

  ngOnInit() {
    this.route.url.subscribe(segments => {
      this.route.queryParams.subscribe(qp => {
        const path = this.router.url;
        if (path.includes('/featured')) {
          this.title = 'Featured Books';
          this.bookSvc.getFeatured().subscribe({ next: b => { this.books = b; this.loading = false; }, error: () => this.loading = false });
        } else if (path.includes('/search')) {
          this.keyword = qp['keyword'] || '';
          this.title = `Search: "${this.keyword}"`;
          this.bookSvc.search(this.keyword).subscribe({ next: b => { this.books = b; this.loading = false; }, error: () => this.loading = false });
        } else {
          this.route.params.subscribe(p => {
            if (p['genre']) {
              this.activeGenre = p['genre'];
              this.title = `Genre: ${p['genre']}`;
              this.bookSvc.getByGenre(p['genre']).subscribe({ next: b => { this.books = b; this.loading = false; }, error: () => this.loading = false });
            } else if (p['author']) {
              this.title = `Author: ${p['author']}`;
              this.bookSvc.getByAuthor(p['author']).subscribe({ next: b => { this.books = b; this.loading = false; }, error: () => this.loading = false });
            } else {
              this.title = 'All Books';
              this.bookSvc.getAll().subscribe({ next: b => { this.books = b; this.loading = false; }, error: () => this.loading = false });
            }
          });
        }
      });
    });
  }

  doSearch() { if (this.keyword.trim()) this.router.navigate(['/books/search'], { queryParams: { keyword: this.keyword } }); }
  filterGenre(g: string) { this.activeGenre = g; this.router.navigate(['/books/genre', g]); }
  clearFilter() { this.activeGenre = ''; this.router.navigate(['/books']); }
}
