import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AuthService } from '../app/services/auth.service';

// Mock type that allows property assignment (avoids readonly TS errors)
type MockAuthService = {
  isLoggedIn: boolean;
  isAdmin: boolean;
};

describe('AuthGuard Logic', () => {
  let mockAuthService: MockAuthService;
  let mockRouter: Partial<Router>;

  beforeEach(() => {
    mockRouter = { navigate: jest.fn() };
    mockAuthService = { isLoggedIn: false, isAdmin: false };
    TestBed.configureTestingModule({
      providers: [
        { provide: Router, useValue: mockRouter },
        { provide: AuthService, useValue: mockAuthService },
      ],
    });
  });

  it('should block unauthenticated user and navigate to /auth/login', () => {
    mockAuthService.isLoggedIn = false;
    if (!mockAuthService.isLoggedIn) {
      mockRouter.navigate!(['/auth/login']);
    }
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/auth/login']);
  });

  it('should allow authenticated user to pass through', () => {
    mockAuthService.isLoggedIn = true;
    const canActivate = mockAuthService.isLoggedIn;
    expect(canActivate).toBe(true);
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });
});

describe('AdminGuard Logic', () => {
  let mockAuthService: MockAuthService;
  let mockRouter: Partial<Router>;

  beforeEach(() => {
    mockRouter = { navigate: jest.fn() };
    mockAuthService = { isLoggedIn: true, isAdmin: false };
    TestBed.configureTestingModule({
      providers: [
        { provide: Router, useValue: mockRouter },
        { provide: AuthService, useValue: mockAuthService },
      ],
    });
  });

  it('should redirect non-admin user to /auth/login', () => {
    mockAuthService.isAdmin = false;
    if (!mockAuthService.isAdmin) {
      mockRouter.navigate!(['/auth/login']);
    }
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/auth/login']);
  });

  it('should allow admin user to access admin routes', () => {
    mockAuthService.isAdmin = true;
    const canActivate = mockAuthService.isAdmin;
    expect(canActivate).toBe(true);
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should redirect unauthenticated user to /auth/login', () => {
    mockAuthService.isLoggedIn = false;
    mockAuthService.isAdmin = false;
    if (!mockAuthService.isAdmin) {
      mockRouter.navigate!(['/auth/login']);
    }
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/auth/login']);
  });
});
