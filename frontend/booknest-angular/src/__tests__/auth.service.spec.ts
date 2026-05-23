import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from '../app/services/auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const mockUser = {
    userId: 1,
    fullName: 'Ravi Kumar',
    email: 'ravi@booknest.com',
    role: 'CUSTOMER',
    mobile: '9876543210',
  };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  // ── isLoggedIn / currentUser ──────────────────────────────────────────────

  it('should return false for isLoggedIn when no user in localStorage', () => {
    expect(service.isLoggedIn).toBe(false);
  });

  it('should return true for isLoggedIn when user stored in localStorage', () => {
    localStorage.setItem('booknest_user', JSON.stringify(mockUser));
    const { HttpClient } = require('@angular/common/http');
    service = new (AuthService as any)(TestBed.inject(HttpClient));
    expect(service.isLoggedIn).toBe(true);
  });

  it('should return false for isAdmin when role is CUSTOMER', () => {
    localStorage.setItem('booknest_user', JSON.stringify(mockUser));
    const { HttpClient } = require('@angular/common/http');
    service = new (AuthService as any)(TestBed.inject(HttpClient));
    expect(service.isAdmin).toBe(false);
  });

  it('should return true for isAdmin when role is ADMIN', () => {
    localStorage.setItem('booknest_user', JSON.stringify({ ...mockUser, role: 'ADMIN' }));
    const { HttpClient } = require('@angular/common/http');
    service = new (AuthService as any)(TestBed.inject(HttpClient));
    expect(service.isAdmin).toBe(true);
  });

  // ── register ─────────────────────────────────────────────────────────────

  it('should POST to /api/auth/register and return User', () => {
    const req = { fullName: 'Ravi Kumar', email: 'ravi@booknest.com', password: 'Pass@123', mobile: '9876543210' };
    service.register(req).subscribe(user => {
      expect(user.userId).toBe(1);
      expect(user.email).toBe('ravi@booknest.com');
    });
    const httpReq = httpMock.expectOne('/api/auth/register');
    expect(httpReq.request.method).toBe('POST');
    expect(httpReq.request.body).toEqual(req);
    httpReq.flush(mockUser);
  });

  // ── login ─────────────────────────────────────────────────────────────────

  it('should POST to /api/auth/login and store token in localStorage', () => {
    const loginReq = { email: 'ravi@booknest.com', password: 'Pass@123' };
    service.login(loginReq).subscribe();

    const loginHttp = httpMock.expectOne('/api/auth/login');
    expect(loginHttp.request.method).toBe('POST');
    loginHttp.flush('mock-jwt-token');

    expect(localStorage.getItem('booknest_token')).toBe('mock-jwt-token');

    // profile fetch triggered inside tap
    const profileHttp = httpMock.expectOne(r => r.url.includes('/api/auth/profile'));
    profileHttp.flush(mockUser);

    // wallet creation triggered inside nested subscribe — actual URL is /api/wallet
    const walletHttp = httpMock.expectOne('/api/wallet');
    walletHttp.flush({});
  });

  it('should set token property from localStorage', () => {
    localStorage.setItem('booknest_token', 'abc123');
    expect(service.token).toBe('abc123');
  });

  // ── logout ────────────────────────────────────────────────────────────────

  it('should clear localStorage on logout when no token present', () => {
    localStorage.setItem('booknest_user', JSON.stringify(mockUser));
    service.logout();
    expect(localStorage.getItem('booknest_token')).toBeNull();
    expect(localStorage.getItem('booknest_user')).toBeNull();
  });

  it('should POST /api/auth/logout and clear localStorage when token present', () => {
    localStorage.setItem('booknest_token', 'tok');
    localStorage.setItem('booknest_user', JSON.stringify(mockUser));
    service.logout();
    const req = httpMock.expectOne(r => r.url.includes('/api/auth/logout'));
    expect(req.request.method).toBe('POST');
    req.flush({});
    expect(localStorage.getItem('booknest_token')).toBeNull();
    expect(localStorage.getItem('booknest_user')).toBeNull();
  });

  it('should emit null from user$ on logout', () => {
    service.logout();
    service.user$.subscribe(u => expect(u).toBeNull());
  });

  // ── getUser ───────────────────────────────────────────────────────────────

  it('should GET /api/auth/user/:id', () => {
    service.getUser(1).subscribe(u => expect(u.userId).toBe(1));
    const req = httpMock.expectOne('/api/auth/user/1');
    expect(req.request.method).toBe('GET');
    req.flush(mockUser);
  });

  // ── updateUser ────────────────────────────────────────────────────────────

  it('should PUT /api/auth/user/:id and update localStorage', () => {
    const updated = { ...mockUser, fullName: 'Ravi K Updated' };
    service.updateUser(1, { fullName: 'Ravi K Updated' }).subscribe(u => {
      expect(u.fullName).toBe('Ravi K Updated');
    });
    const req = httpMock.expectOne('/api/auth/user/1');
    expect(req.request.method).toBe('PUT');
    req.flush(updated);
    expect(JSON.parse(localStorage.getItem('booknest_user')!).fullName).toBe('Ravi K Updated');
  });

  // ── deleteUser ────────────────────────────────────────────────────────────

  it('should DELETE /api/auth/user/:id', () => {
    service.deleteUser(5).subscribe(msg => expect(msg).toBe('Deleted'));
    const req = httpMock.expectOne('/api/auth/user/5');
    expect(req.request.method).toBe('DELETE');
    req.flush('Deleted');
  });

  // ── getUsersByRole ────────────────────────────────────────────────────────

  it('should GET /api/auth/users/role/CUSTOMER', () => {
    service.getUsersByRole('CUSTOMER').subscribe(users => expect(users.length).toBe(1));
    const req = httpMock.expectOne('/api/auth/users/role/CUSTOMER');
    expect(req.request.method).toBe('GET');
    req.flush([mockUser]);
  });

  // ── setCurrentUser ────────────────────────────────────────────────────────

  it('should store user and emit via user$ on setCurrentUser', () => {
    service.setCurrentUser(mockUser);
    expect(JSON.parse(localStorage.getItem('booknest_user')!).email).toBe('ravi@booknest.com');
    service.user$.subscribe(u => expect(u?.userId).toBe(1));
  });

  // ── registerAdmin ─────────────────────────────────────────────────────────

  it('should POST /api/auth/register/admin', () => {
    const req = { fullName: 'Admin', email: 'admin@b.com', password: 'p', mobile: '0' };
    service.registerAdmin(req).subscribe(u => expect(u.role).toBe('ADMIN'));
    const http = httpMock.expectOne('/api/auth/register/admin');
    expect(http.request.method).toBe('POST');
    http.flush({ ...mockUser, role: 'ADMIN' });
  });
});
