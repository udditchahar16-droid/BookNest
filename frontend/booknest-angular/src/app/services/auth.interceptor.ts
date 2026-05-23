import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('booknest_token');
  const userStr = localStorage.getItem('booknest_user');

  let headers: Record<string, string> = {};

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  // Send user role in every request so backend can enforce ADMIN-only actions
  if (userStr) {
    try {
      const user = JSON.parse(userStr);
      if (user?.role) {
        headers['X-User-Role'] = user.role;
      }
    } catch {}
  }

  if (Object.keys(headers).length > 0) {
    req = req.clone({ setHeaders: headers });
  }

  return next(req);
};
