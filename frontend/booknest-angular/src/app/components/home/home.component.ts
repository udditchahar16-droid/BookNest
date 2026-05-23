import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { BookService } from '../../services/book.service';
import { Book } from '../../models/models';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <!-- Hero -->
    <section class="hero">
      <div class="hero-content">
        <h1>Discover Your Next<br><span>Great Read</span></h1>
        <p>Browse thousands of books across every genre. Shop, review, and discover.</p>
        <div class="search-bar">
          <i class="fas fa-search"></i>
          <input type="text" [(ngModel)]="keyword" placeholder="Search by title, author, genre..." (keyup.enter)="search()">
          <button class="btn btn-accent" (click)="search()">Search</button>
        </div>
        <div class="quick-genres">
          <span *ngFor="let g of genres" (click)="goGenre(g)" class="genre-tag">{{g}}</span>
        </div>
      </div>
    </section>

    <div class="container">
      <!-- Featured -->
      <section class="section" *ngIf="featured.length">
        <div class="section-header">
          <h2>Featured Books</h2>
          <a routerLink="/books/featured" class="see-all">See all <i class="fas fa-arrow-right"></i></a>
        </div>
        <div class="books-grid">
          <div class="book-card" *ngFor="let b of featured.slice(0,4)">
            <a [routerLink]="['/books', b.bookId]">
              <div class="book-cover">
                <img [src]="b.coverImageUrl || 'https://placehold.co/180x260/2c3e6b/white?text=📚'" [alt]="b.title" onerror="this.src='https://placehold.co/180x260/2c3e6b/white?text=📚'">
                <div class="featured-badge" *ngIf="b.featured">Featured</div>
              </div>
              <div class="book-info">
                <h3>{{b.title}}</h3>
                <p class="author">{{b.author}}</p>
                <div class="meta">
                  <span class="price">₹{{b.price}}</span>
                  <span class="stock" [class.low]="b.stock < 5">{{b.stock > 0 ? 'In Stock' : 'Out of Stock'}}</span>
                </div>
                <div class="rating" *ngIf="b.rating">
                  <span class="star" *ngFor="let s of getStars(b.rating)">★</span>
                  <span class="rating-val">{{b.rating | number:'1.1-1'}}</span>
                </div>
              </div>
            </a>
          </div>
        </div>
      </section>

      <!-- All Books -->
      <section class="section">
        <div class="section-header">
          <h2>All Books</h2>
          <a routerLink="/books" class="see-all">Browse all <i class="fas fa-arrow-right"></i></a>
        </div>
        <div class="loading" *ngIf="loading"><div class="spinner"></div> Loading books...</div>
        <div class="books-grid" *ngIf="!loading">
          <div class="book-card" *ngFor="let b of allBooks.slice(0,8)">
            <a [routerLink]="['/books', b.bookId]">
              <div class="book-cover">
                <img [src]="b.coverImageUrl || 'https://placehold.co/180x260/2c3e6b/white?text=📚'" [alt]="b.title" onerror="this.src='https://placehold.co/180x260/2c3e6b/white?text=📚'">
              </div>
              <div class="book-info">
                <h3>{{b.title}}</h3>
                <p class="author">{{b.author}}</p>
                <div class="meta">
                  <span class="price">₹{{b.price}}</span>
                  <span class="genre-chip" *ngIf="b.genre">{{b.genre}}</span>
                </div>
              </div>
            </a>
          </div>
        </div>
        <div class="empty-state" *ngIf="!loading && !allBooks.length">
          <i class="fas fa-books"></i>
          <h3>No books yet</h3>
          <p>Check back soon!</p>
        </div>
      </section>
    </div>
  `,
  styles: [`
    .hero {
      background: linear-gradient(135deg, var(--primary) 0%, #1a2a5e 100%);
      color: white;
      padding: 80px 20px 60px;
      text-align: center;
    }
    .hero-content { max-width: 700px; margin: 0 auto; }
    .hero h1 { font-size: 3rem; line-height: 1.2; margin-bottom: 16px; span { color: #f6c87a; } }
    .hero p { font-size: 18px; opacity: 0.85; margin-bottom: 32px; }
    .search-bar {
      display: flex;
      align-items: center;
      background: white;
      border-radius: 50px;
      padding: 6px 8px 6px 20px;
      gap: 12px;
      max-width: 560px;
      margin: 0 auto 20px;
      i { color: var(--text-muted); }
      input { flex: 1; border: none; outline: none; font-size: 15px; background: none; color: var(--text); }
      .btn { border-radius: 40px; }
    }
    .quick-genres { display: flex; gap: 8px; flex-wrap: wrap; justify-content: center; }
    .genre-tag {
      background: rgba(255,255,255,0.15);
      color: white;
      padding: 5px 14px;
      border-radius: 20px;
      font-size: 13px;
      cursor: pointer;
      transition: background 0.2s;
      &:hover { background: rgba(255,255,255,0.25); }
    }
    .section { padding: 48px 0; }
    .section-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 24px;
      h2 { font-size: 1.6rem; color: var(--primary); }
    }
    .see-all { font-size: 14px; color: var(--accent); font-weight: 500; text-decoration: none; display: flex; align-items: center; gap: 6px; &:hover { text-decoration: underline; } }
    .books-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 20px; }
    .book-card {
      background: white;
      border-radius: 12px;
      overflow: hidden;
      border: 1px solid var(--border);
      transition: box-shadow 0.2s, transform 0.2s;
      &:hover { box-shadow: var(--shadow-lg); transform: translateY(-4px); }
      a { text-decoration: none; color: inherit; }
    }
    .book-cover {
      position: relative;
      aspect-ratio: 3/4;
      overflow: hidden;
      background: var(--bg);
      img { width: 100%; height: 100%; object-fit: cover; }
    }
    .featured-badge {
      position: absolute;
      top: 8px;
      left: 8px;
      background: var(--accent);
      color: white;
      font-size: 11px;
      font-weight: 600;
      padding: 3px 8px;
      border-radius: 20px;
    }
    .book-info { padding: 12px; }
    .book-info h3 { font-size: 14px; font-weight: 600; margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-family: 'Inter', sans-serif; }
    .author { font-size: 12px; color: var(--text-muted); margin-bottom: 8px; }
    .meta { display: flex; align-items: center; justify-content: space-between; }
    .price { font-size: 15px; font-weight: 700; color: var(--primary); }
    .stock { font-size: 11px; color: var(--success); &.low { color: var(--error); } }
    .genre-chip { font-size: 11px; background: var(--bg); color: var(--text-muted); padding: 2px 8px; border-radius: 20px; }
    .rating { display: flex; align-items: center; gap: 2px; margin-top: 6px; }
    .rating-val { font-size: 12px; color: var(--text-muted); margin-left: 4px; }
  `]
})
export class HomeComponent implements OnInit {
  featured: Book[] = [];
  allBooks: Book[] = [];
  loading = true;
  keyword = '';
  genres = ['Fiction', 'Non-Fiction', 'Science', 'History', 'Mystery', 'Fantasy', 'Romance', 'Biography'];

  constructor(private bookSvc: BookService, private router: Router) {}

  ngOnInit() {
    this.bookSvc.getFeatured().subscribe({ next: b => this.featured = b, error: () => {} });
    this.bookSvc.getAll().subscribe({ next: b => { this.allBooks = b; this.loading = false; }, error: () => this.loading = false });
  }

  search() { if (this.keyword.trim()) this.router.navigate(['/books/search'], { queryParams: { keyword: this.keyword } }); }
  goGenre(g: string) { this.router.navigate(['/books/genre', g]); }
  getStars(r: number): number[] { return Array(Math.round(r)).fill(0); }
}
