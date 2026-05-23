import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cart, Order, PlaceOrderRequest, Address, Wallet, Statement, Review, Notification, Wishlist } from '../models/models';

// ── Cart Service ── port 8083 ─────────────────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class CartService {
  private BASE = '/api/cart';
  constructor(private http: HttpClient) {}

  getCart(userId: number): Observable<Cart> {
    return this.http.get<Cart>(`${this.BASE}/${userId}`);
  }
  addItem(userId: number, item: { bookId: number; bookTitle: string; price: number; quantity: number }): Observable<Cart> {
    return this.http.post<Cart>(`${this.BASE}/${userId}/add`, { bookId: item.bookId, quantity: item.quantity });
  }
  updateItem(userId: number, itemId: number, quantity: number): Observable<Cart> {
    return this.http.put<Cart>(`${this.BASE}/${userId}/update/${itemId}`, { quantity });
  }
  removeItem(userId: number, itemId: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE}/${userId}/remove/${itemId}`);
  }
  clearCart(userId: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE}/${userId}/clear`);
  }
}

// ── Order Service ── port 8084 ────────────────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class OrderService {
  private BASE = '/api/orders';
  constructor(private http: HttpClient) {}

  getByUser(userId: number): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.BASE}/user/${userId}`);
  }
  getAll(): Observable<Order[]> {
    return this.http.get<Order[]>(this.BASE);
  }
  placeCOD(req: PlaceOrderRequest): Observable<any> {
    return this.http.post(`${this.BASE}/place`, req, { responseType: 'text' });
  }
  placeOnline(req: PlaceOrderRequest): Observable<any> {
    return this.http.post(`${this.BASE}/online-payment`, req, { responseType: 'text' });
  }
  changeStatus(orderId: number, status: string): Observable<any> {
    return this.http.put(`${this.BASE}/${orderId}/status?status=${status}`, {}, { responseType: 'text' });
  }
  cancelOrder(orderId: number): Observable<any> {
    return this.http.put(`${this.BASE}/${orderId}/status?status=CANCELLED`, {}, { responseType: 'text' });
  }
  deleteOrder(orderId: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE}/${orderId}`);
  }
  getSavedAddresses(userId: number): Observable<Address[]> {
    return this.http.get<Address[]>(`${this.BASE}/address/${userId}`);
  }
  saveAddress(address: Address): Observable<any> {
    return this.http.post(`${this.BASE}/address`, address, { responseType: 'text' });
  }
}

// ── Wallet Service ── port 8085 ───────────────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class WalletService {
  private BASE = '/api/wallet';
  constructor(private http: HttpClient) {}

  getWallet(userId: number): Observable<Wallet> {
    return this.http.get<Wallet>(`${this.BASE}/${userId}`);
  }
  createWallet(userId: number): Observable<Wallet> {
    return this.http.post<Wallet>(this.BASE, { walletId: userId, currentBalance: 0 });
  }
  // Returns Wallet object (updated balance) after adding money
  addMoney(userId: number, amount: number, remarks: string): Observable<Wallet> {
    return this.http.post<Wallet>(`${this.BASE}/${userId}/add`, { amount, remarks });
  }
  // Returns Wallet object after deduction
  payMoney(userId: number, amount: number, remarks: string, orderId: number = 0): Observable<Wallet> {
    return this.http.post<Wallet>(`${this.BASE}/${userId}/deduct`, { amount, remarks, orderId });
  }
  getStatements(userId: number): Observable<Statement[]> {
    return this.http.get<Statement[]>(`${this.BASE}/${userId}/statements`);
  }
}

// ── Review Service ── port 8086 ───────────────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class ReviewService {
  private BASE = '/api/reviews';
  constructor(private http: HttpClient) {}

  getByBook(bookId: number): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.BASE}/book/${bookId}`);
  }
  getAll(): Observable<Review[]> {
    return this.http.get<Review[]>(this.BASE);
  }
  getAvgRating(bookId: number): Observable<number> {
    return this.http.get<number>(`${this.BASE}/book/${bookId}/avg-rating`);
  }
  addReview(review: Partial<Review>): Observable<Review> {
    return this.http.post<Review>(this.BASE, review);
  }
  deleteReview(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE}/${id}`);
  }
}

// ── Notification Service ── port 8087 ─────────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private BASE = '/api/notifications';
  constructor(private http: HttpClient) {}

  getByUser(userId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.BASE}/user/${userId}`);
  }
  getUnreadCount(userId: number): Observable<number> {
    return this.http.get<number>(`${this.BASE}/user/${userId}/unread-count`);
  }
  markAllRead(userId: number): Observable<void> {
    return this.http.put<void>(`${this.BASE}/user/${userId}/read-all`, {});
  }
  markAsRead(id: number): Observable<void> {
    return this.http.put<void>(`${this.BASE}/${id}/read`, {});
  }
  sendNotification(n: { userId: number; type: string; message: string }): Observable<Notification> {
    return this.http.post<Notification>(`${this.BASE}/send`, n);
  }
  deleteNotification(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE}/${id}`);
  }
}

// ── Wishlist Service ── port 8088 ─────────────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class WishlistService {
  private BASE = '/api/wishlist';
  constructor(private http: HttpClient) {}

  getWishlist(userId: number): Observable<Wishlist> {
    return this.http.get<Wishlist>(`${this.BASE}/${userId}`);
  }
  addBook(userId: number, item: { bookId: number; bookTitle: string; bookPrice: number }): Observable<Wishlist> {
    return this.http.post<Wishlist>(`${this.BASE}/${userId}/add/${item.bookId}`, {});
  }
  removeBook(userId: number, bookId: number): Observable<any> {
    return this.http.delete(`${this.BASE}/${userId}/remove/${bookId}`);
  }
  clearWishlist(userId: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE}/${userId}/clear`);
  }
  moveToCart(userId: number, bookId: number): Observable<any> {
    return this.http.post(`${this.BASE}/${userId}/move-to-cart/${bookId}`, {}, { responseType: 'text' });
  }
}
