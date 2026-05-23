import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Book } from '../models/models';

@Injectable({ providedIn: 'root' })
export class BookService {
  private readonly BASE = '/api/books';
  constructor(private http: HttpClient) {}

  getAll(): Observable<Book[]> { return this.http.get<Book[]>(this.BASE); }
  getById(id: number): Observable<Book> { return this.http.get<Book>(`${this.BASE}/${id}`); }
  getFeatured(): Observable<Book[]> { return this.http.get<Book[]>(`${this.BASE}/featured`); }
  search(keyword: string): Observable<Book[]> {
    return this.http.get<Book[]>(`${this.BASE}/search`, { params: new HttpParams().set('keyword', keyword) });
  }
  getByGenre(genre: string): Observable<Book[]> { return this.http.get<Book[]>(`${this.BASE}/genre/${genre}`); }
  getByAuthor(author: string): Observable<Book[]> { return this.http.get<Book[]>(`${this.BASE}/author/${author}`); }

  addBook(book: Partial<Book>): Observable<Book> { return this.http.post<Book>(this.BASE, book); }
  updateBook(id: number, book: Partial<Book>): Observable<Book> { return this.http.put<Book>(`${this.BASE}/${id}`, book); }
  deleteBook(id: number): Observable<void> { return this.http.delete<void>(`${this.BASE}/${id}`); }
  updateStock(id: number, stock: number): Observable<string> {
    return this.http.patch(`${this.BASE}/${id}/stock`, { stock }, { responseType: 'text' });
  }
}
