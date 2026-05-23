import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { adminGuard } from './guards/admin.guard';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./components/home/home.component').then(m => m.HomeComponent) },
  { path: 'auth/login', loadComponent: () => import('./components/auth/login.component').then(m => m.LoginComponent) },
  { path: 'auth/register', loadComponent: () => import('./components/auth/register.component').then(m => m.RegisterComponent) },
  { path: 'auth/register-admin', loadComponent: () => import('./components/auth/register-admin.component').then(m => m.RegisterAdminComponent) },
  { path: 'auth/forgot-password', loadComponent: () => import('./components/auth/forgot-password.component').then(m => m.ForgotPasswordComponent) },
  { path: 'books', loadComponent: () => import('./components/books/catalog.component').then(m => m.CatalogComponent) },
  { path: 'books/featured', loadComponent: () => import('./components/books/catalog.component').then(m => m.CatalogComponent) },
  { path: 'books/search', loadComponent: () => import('./components/books/catalog.component').then(m => m.CatalogComponent) },
  { path: 'books/genre/:genre', loadComponent: () => import('./components/books/catalog.component').then(m => m.CatalogComponent) },
  { path: 'books/author/:author', loadComponent: () => import('./components/books/catalog.component').then(m => m.CatalogComponent) },
  { path: 'books/:id', loadComponent: () => import('./components/books/book-detail.component').then(m => m.BookDetailComponent) },
  { path: 'cart', loadComponent: () => import('./components/cart/cart.component').then(m => m.CartComponent), canActivate: [authGuard] },
  { path: 'wishlist', loadComponent: () => import('./components/wishlist/wishlist.component').then(m => m.WishlistComponent), canActivate: [authGuard] },
  { path: 'checkout', loadComponent: () => import('./components/orders/checkout.component').then(m => m.CheckoutComponent), canActivate: [authGuard] },
  { path: 'orders', loadComponent: () => import('./components/orders/orders.component').then(m => m.OrdersComponent), canActivate: [authGuard] },
  { path: 'wallet', loadComponent: () => import('./components/wallet/wallet.component').then(m => m.WalletComponent), canActivate: [authGuard] },
  { path: 'wallet/statements', loadComponent: () => import('./components/wallet/statements.component').then(m => m.StatementsComponent), canActivate: [authGuard] },
  { path: 'notifications', loadComponent: () => import('./components/notifications/notifications.component').then(m => m.NotificationsComponent), canActivate: [authGuard] },
  { path: 'profile', loadComponent: () => import('./components/auth/profile.component').then(m => m.ProfileComponent), canActivate: [authGuard] },
  { path: 'admin', loadComponent: () => import('./components/admin/dashboard.component').then(m => m.AdminDashboardComponent), canActivate: [adminGuard] },
  { path: 'admin/books', loadComponent: () => import('./components/admin/admin-books.component').then(m => m.AdminBooksComponent), canActivate: [adminGuard] },
  { path: 'admin/books/add', loadComponent: () => import('./components/admin/book-form.component').then(m => m.BookFormComponent), canActivate: [adminGuard] },
  { path: 'admin/books/edit/:id', loadComponent: () => import('./components/admin/book-form.component').then(m => m.BookFormComponent), canActivate: [adminGuard] },
  { path: 'admin/orders', loadComponent: () => import('./components/admin/admin-orders.component').then(m => m.AdminOrdersComponent), canActivate: [adminGuard] },
  { path: 'admin/users', loadComponent: () => import('./components/admin/admin-users.component').then(m => m.AdminUsersComponent), canActivate: [adminGuard] },
  { path: 'admin/analytics', loadComponent: () => import('./components/admin/admin-analytics.component').then(m => m.AdminAnalyticsComponent), canActivate: [adminGuard] },
  { path: 'admin/reviews', loadComponent: () => import('./components/admin/admin-reviews.component').then(m => m.AdminReviewsComponent), canActivate: [adminGuard] },
  { path: 'admin/inventory', loadComponent: () => import('./components/admin/admin-inventory.component').then(m => m.AdminInventoryComponent), canActivate: [adminGuard] },
  { path: 'auth/oauth-callback', loadComponent: () => import('./components/auth/oauth-callback.component').then(m => m.OAuthCallbackComponent) },
  { path: '**', redirectTo: '' }
];
