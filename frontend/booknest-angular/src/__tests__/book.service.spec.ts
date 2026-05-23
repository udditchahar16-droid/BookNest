import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BookService } from '../app/services/book.service';

describe('BookService', () => {
  let service: BookService;
  let httpMock: HttpTestingController;

  const mockBook = {
    bookId: 10,
    title: 'Clean Code',
    author: 'Robert C. Martin',
    isbn: '9780132350884',
    genre: 'Technology',
    publisher: 'Prentice Hall',
    price: 599,
    stock: 20,
    rating: 4.8,
    description: 'A handbook of agile software craftsmanship.',
    coverImageUrl: 'https://example.com/clean-code.jpg',
    publishedDate: '2008-08-01',
    featured: true,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BookService],
    });
    service = TestBed.inject(BookService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  // ── getAll ────────────────────────────────────────────────────────────────

  it('should GET /api/books and return list of books', () => {
    service.getAll().subscribe(books => {
      expect(books.length).toBe(1);
      expect(books[0].title).toBe('Clean Code');
    });
    const req = httpMock.expectOne('/api/books');
    expect(req.request.method).toBe('GET');
    req.flush([mockBook]);
  });

  // ── getById ───────────────────────────────────────────────────────────────

  it('should GET /api/books/:id', () => {
    service.getById(10).subscribe(book => expect(book.bookId).toBe(10));
    const req = httpMock.expectOne('/api/books/10');
    expect(req.request.method).toBe('GET');
    req.flush(mockBook);
  });

  it('should return 404 error for non-existent book', () => {
    service.getById(999).subscribe({
      error: err => expect(err.status).toBe(404),
    });
    const req = httpMock.expectOne('/api/books/999');
    req.flush('Not Found', { status: 404, statusText: 'Not Found' });
  });

  // ── getFeatured ───────────────────────────────────────────────────────────

  it('should GET /api/books/featured', () => {
    service.getFeatured().subscribe(books => {
      expect(books.every(b => b.featured)).toBe(true);
    });
    const req = httpMock.expectOne('/api/books/featured');
    expect(req.request.method).toBe('GET');
    req.flush([mockBook]);
  });

  // ── search ────────────────────────────────────────────────────────────────

  it('should GET /api/books/search with keyword param', () => {
    service.search('clean').subscribe(books => expect(books[0].title).toBe('Clean Code'));
    const req = httpMock.expectOne(r => r.url === '/api/books/search' && r.params.get('keyword') === 'clean');
    expect(req.request.method).toBe('GET');
    req.flush([mockBook]);
  });

  it('should return empty array for unmatched keyword', () => {
    service.search('xyzzy').subscribe(books => expect(books.length).toBe(0));
    const req = httpMock.expectOne(r => r.url === '/api/books/search');
    req.flush([]);
  });

  // ── getByGenre ────────────────────────────────────────────────────────────

  it('should GET /api/books/genre/:genre', () => {
    service.getByGenre('Technology').subscribe(books => {
      expect(books[0].genre).toBe('Technology');
    });
    const req = httpMock.expectOne('/api/books/genre/Technology');
    expect(req.request.method).toBe('GET');
    req.flush([mockBook]);
  });

  // ── getByAuthor ───────────────────────────────────────────────────────────

  it('should GET /api/books/author/:author', () => {
    service.getByAuthor('Robert C. Martin').subscribe(books => {
      expect(books[0].author).toBe('Robert C. Martin');
    });
    const req = httpMock.expectOne('/api/books/author/Robert C. Martin');
    expect(req.request.method).toBe('GET');
    req.flush([mockBook]);
  });

  // ── addBook ───────────────────────────────────────────────────────────────

  it('should POST /api/books to add a new book', () => {
    const newBook = { title: 'New Book', author: 'Author', price: 299, stock: 10 };
    service.addBook(newBook).subscribe(book => expect(book.bookId).toBe(10));
    const req = httpMock.expectOne('/api/books');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newBook);
    req.flush(mockBook);
  });

  // ── updateBook ────────────────────────────────────────────────────────────

  it('should PUT /api/books/:id to update a book', () => {
    const updates = { price: 499, stock: 15 };
    service.updateBook(10, updates).subscribe(book => expect(book.price).toBe(499));
    const req = httpMock.expectOne('/api/books/10');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updates);
    req.flush({ ...mockBook, price: 499, stock: 15 });
  });

  // ── deleteBook ────────────────────────────────────────────────────────────

  it('should DELETE /api/books/:id', () => {
    service.deleteBook(10).subscribe(() => expect(true).toBe(true));
    const req = httpMock.expectOne('/api/books/10');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  // ── updateStock ───────────────────────────────────────────────────────────

  it('should PATCH /api/books/:id/stock', () => {
    service.updateStock(10, 50).subscribe(msg => expect(msg).toBe('Stock updated'));
    const req = httpMock.expectOne('/api/books/10/stock');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ stock: 50 });
    req.flush('Stock updated');
  });
});
