import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { BookService } from '../../services/book.service';
import { Book } from '../../models/models';

@Component({
  selector: 'app-book-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="container">
      <div class="page-header">
        <h1><i class="fas fa-{{editMode ? 'edit' : 'plus'}}"></i> {{editMode ? 'Edit Book' : 'Add New Book'}}</h1>
        <a routerLink="/admin/books" class="btn btn-outline btn-sm">
          <i class="fas fa-arrow-left"></i> Back
        </a>
      </div>

      <div class="card form-card">
        <div class="alert alert-error" *ngIf="error">{{error}}</div>
        <div class="alert alert-success" *ngIf="uploadSuccess">✅ Image uploaded successfully!</div>

        <form (ngSubmit)="submit()">
          <div class="grid-2">
            <div class="form-group">
              <label>Title *</label>
              <input type="text" [(ngModel)]="book.title" name="title" required placeholder="Book title">
            </div>
            <div class="form-group">
              <label>Author *</label>
              <input type="text" [(ngModel)]="book.author" name="author" required placeholder="Author name">
            </div>
            <div class="form-group">
              <label>ISBN</label>
              <input type="text" [(ngModel)]="book.isbn" name="isbn" placeholder="978-XXXXXXXXXX">
            </div>
            <div class="form-group">
              <label>Genre</label>
              <select [(ngModel)]="book.genre" name="genre">
                <option value="">— Select Genre —</option>
                <option *ngFor="let g of genres">{{g}}</option>
              </select>
            </div>
            <div class="form-group">
              <label>Publisher</label>
              <input type="text" [(ngModel)]="book.publisher" name="publisher">
            </div>
            <div class="form-group">
              <label>Published Date</label>
              <input type="date" [(ngModel)]="book.publishedDate" name="publishedDate">
            </div>
            <div class="form-group">
              <label>Price (₹) *</label>
              <input type="number" [(ngModel)]="book.price" name="price" required min="0" step="0.01">
            </div>
            <div class="form-group">
              <label>Stock *</label>
              <input type="number" [(ngModel)]="book.stock" name="stock" required min="0">
            </div>

            <!-- ── Cover Image — Upload ya URL dono option ── -->
            <div class="form-group" style="grid-column:1/-1">
              <label>Cover Image</label>

              <!-- Image Preview -->
              <div class="img-preview" *ngIf="book.coverImageUrl">
                <img [src]="book.coverImageUrl" alt="Cover preview"
                     onerror="this.src='https://placehold.co/120x160/2c3e6b/white?text=📚'">
                <button type="button" class="remove-img" (click)="book.coverImageUrl = ''">
                  <i class="fas fa-times"></i>
                </button>
              </div>

              <!-- Tab toggle -->
              <div class="img-tabs">
                <button type="button" class="img-tab" [class.active]="imgMode==='upload'" (click)="imgMode='upload'">
                  <i class="fas fa-upload"></i> Upload Image
                </button>
                <button type="button" class="img-tab" [class.active]="imgMode==='url'" (click)="imgMode='url'">
                  <i class="fas fa-link"></i> Paste URL
                </button>
              </div>

              <!-- Upload file -->
              <div *ngIf="imgMode==='upload'" class="upload-zone"
                   (dragover)="$event.preventDefault()" (drop)="onDrop($event)">
                <input type="file" id="imgFile" accept="image/*"
                       (change)="onFileSelect($event)" style="display:none">
                <label for="imgFile" class="upload-label" [class.uploading]="uploading">
                  <ng-container *ngIf="!uploading">
                    <i class="fas fa-cloud-upload-alt"></i>
                    <span>Click to choose image or drag & drop</span>
                    <small>JPG, PNG, WEBP — max 5MB</small>
                  </ng-container>
                  <ng-container *ngIf="uploading">
                    <div class="spinner"></div>
                    <span>Uploading...</span>
                  </ng-container>
                </label>
              </div>

              <!-- URL input -->
              <div *ngIf="imgMode==='url'">
                <input type="url" [(ngModel)]="book.coverImageUrl" name="coverImageUrl"
                       placeholder="https://example.com/book-cover.jpg">
                <small style="color:var(--text-muted);margin-top:4px;display:block">
                  Open Library API se bhi URL use kar sakte hain:
                  https://covers.openlibrary.org/b/isbn/YOUR_ISBN-L.jpg
                </small>
              </div>
            </div>

            <div class="form-group" style="grid-column:1/-1">
              <label>Description</label>
              <textarea [(ngModel)]="book.description" name="description"
                        rows="4" placeholder="Enter book description..."></textarea>
            </div>
            <div class="form-group">
              <label class="checkbox-label">
                <input type="checkbox" [(ngModel)]="book.featured" name="featured">
                Mark as Featured
              </label>
            </div>
          </div>

          <div class="form-actions">
            <a routerLink="/admin/books" class="btn btn-outline">Cancel</a>
            <button type="submit" class="btn btn-accent btn-lg" [disabled]="saving || uploading">
              <i class="fas fa-save"></i>
              {{saving ? 'Saving...' : (editMode ? 'Update Book' : 'Add Book')}}
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .form-card { max-width: 900px; }
    .form-actions { display: flex; gap: 12px; margin-top: 24px; justify-content: flex-end; }
    .checkbox-label { display: flex; align-items: center; gap: 10px; cursor: pointer; font-size: 14px; input { width: auto; } }

    /* Image Preview */
    .img-preview { position: relative; display: inline-block; margin-bottom: 12px;
      img { width: 120px; height: 160px; object-fit: cover; border-radius: 8px; border: 2px solid var(--border); display: block; }
    }
    .remove-img { position: absolute; top: -8px; right: -8px; background: var(--error); color: white;
      border: none; border-radius: 50%; width: 24px; height: 24px; cursor: pointer;
      display: flex; align-items: center; justify-content: center; font-size: 11px; }

    /* Tabs */
    .img-tabs { display: flex; border: 1px solid var(--border); border-radius: 8px; overflow: hidden; margin-bottom: 12px; width: fit-content; }
    .img-tab { padding: 8px 18px; border: none; background: white; cursor: pointer;
      font-size: 13px; color: var(--text-muted); transition: all 0.2s; display: flex; align-items: center; gap: 6px;
      &:first-child { border-right: 1px solid var(--border); }
      &.active { background: var(--primary); color: white; }
    }

    /* Upload zone */
    .upload-zone { border: 2px dashed var(--border); border-radius: 10px; transition: border-color 0.2s;
      &:hover { border-color: var(--primary); } }
    .upload-label { display: flex; flex-direction: column; align-items: center; justify-content: center;
      padding: 32px; cursor: pointer; gap: 8px; text-align: center;
      i { font-size: 32px; color: var(--primary); }
      span { font-size: 14px; font-weight: 500; }
      small { color: var(--text-muted); font-size: 12px; }
      &.uploading { cursor: not-allowed; opacity: 0.7; }
    }
  `]
})
export class BookFormComponent implements OnInit {
  book: Partial<Book> = { price: 0, stock: 0, featured: false };
  editMode = false;
  saving = false;
  error = '';
  bookId!: number;
  imgMode: 'upload' | 'url' = 'upload';
  uploading = false;
  uploadSuccess = false;
  genres = ['Fiction','Non-Fiction','Science','History','Mystery','Fantasy',
            'Romance','Biography','Self-Help','Technology','Children','Poetry'];

  constructor(
    private bookSvc: BookService,
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.params['id'];
    if (id) {
      this.editMode = true;
      this.bookId = +id;
      this.bookSvc.getById(this.bookId).subscribe({
        next: b => {
          this.book = { ...b };
          // Agar URL already set hai toh url mode dikhao
          if (b.coverImageUrl) this.imgMode = 'url';
        },
        error: () => {}
      });
    }
  }

  // File select from input
  onFileSelect(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.uploadFile(input.files[0]);
    }
  }

  // Drag and drop
  onDrop(event: DragEvent) {
    event.preventDefault();
    const file = event.dataTransfer?.files[0];
    if (file && file.type.startsWith('image/')) {
      this.uploadFile(file);
    }
  }

  // Upload to book-service
  uploadFile(file: File) {
    if (file.size > 20 * 1024 * 1024) {
      this.error = 'File too large. Max 5MB allowed.';
      return;
    }
    this.uploading = true;
    this.error = '';

    const formData = new FormData();
    formData.append('file', file);

    this.http.post<{url: string}>('/api/books/upload-image', formData).subscribe({
      next: res => {
        this.book.coverImageUrl = res.url;
        this.uploading = false;
        this.uploadSuccess = true;
        setTimeout(() => this.uploadSuccess = false, 3000);
      },
      error: () => {
        this.uploading = false;
        this.error = 'Image upload failed. Server se connect nahi ho pa raha.';
      }
    });
  }

  submit() {
    if (!this.book.title?.trim() || !this.book.author?.trim()) {
      this.error = 'Title aur Author required hain.';
      return;
    }
    this.saving = true;
    this.error = '';

    const req$ = this.editMode
      ? this.bookSvc.updateBook(this.bookId, this.book)
      : this.bookSvc.addBook(this.book);

    req$.subscribe({
      next: () => { this.saving = false; this.router.navigate(['/admin/books']); },
      error: e => { this.saving = false; this.error = e?.error?.message || 'Book save nahi ho saka.'; }
    });
  }
}
