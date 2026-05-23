# BookNest — Full Stack E-Commerce Bookstore Platform
## *Discover. Read. Belong.*

---

## 📁 Complete Project Structure

```
BookNest-Complete/
│
├── 📱 frontend/
│   └── booknest-angular/
│       ├── package.json                    ← npm config + Jest setup
│       ├── tsconfig.json
│       └── src/
│           ├── index.html
│           ├── main.ts
│           ├── styles.scss
│           ├── __tests__/                  ← ✅ JEST TEST FILES
│           │   ├── auth.service.spec.ts
│           │   ├── book.service.spec.ts
│           │   ├── cart-order-wallet.service.spec.ts
│           │   ├── review-notification-wishlist.service.spec.ts
│           │   └── guards.spec.ts
│           └── app/
│               ├── app.component.ts
│               ├── app.config.ts
│               ├── app.routes.ts
│               ├── models/models.ts
│               ├── guards/
│               │   ├── auth.guard.ts
│               │   └── admin.guard.ts
│               ├── services/
│               │   ├── auth.service.ts
│               │   ├── book.service.ts
│               │   ├── services.ts           ← Cart/Order/Wallet/Review/Notification/Wishlist
│               │   ├── razorpay.service.ts
│               │   └── auth.interceptor.ts
│               └── components/
│                   ├── auth/         (login, register, profile, oauth-callback)
│                   ├── books/        (catalog, book-detail)
│                   ├── cart/
│                   ├── orders/       (checkout, orders)
│                   ├── wallet/       (wallet, statements)
│                   ├── wishlist/
│                   ├── notifications/
│                   ├── admin/        (dashboard, books, orders, users, analytics, reviews, inventory)
│                   └── shared/       (navbar)
│
└── 🔧 backend/
    ├── auth-service/          (port 8081) ← User, Auth, JWT, OAuth
    ├── book-service/          (port 8082) ← Book catalog, search, stock
    ├── cart-service/          (port 8083) ← Cart, CartItem management
    ├── order-service/         (port 8084) ← Orders, COD/Online, Address
    ├── wallet-service/        (port 8085) ← E-Wallet, Statements
    ├── review-service/        (port 8086) ← Reviews, Ratings
    ├── notification-service/  (port 8087) ← In-app + Email Notifications
    └── wishlist-service/      (port 8088) ← Wishlist, Cart Transfer
        │
        └── Each service contains:
            ├── pom.xml                         ← Maven + JaCoCo
            └── src/
                ├── main/java/com/booknest/
                │   ├── *Entity.java            ← JPA Entity
                │   ├── *Repository.java        ← Spring Data interface
                │   ├── *Service.java           ← Service interface
                │   ├── *ServiceImpl.java       ← Business logic
                │   └── *Resource.java          ← REST controller
                ├── main/resources/
                │   └── application.properties
                └── test/java/com/booknest/
                    └── *ServiceImplTest.java   ← ✅ JUNIT 5 TESTS
```

---

## 🧪 Testing Summary

### Frontend — Jest Tests (5 files, ~66 tests)

| Test File | Tests | Covers |
|-----------|-------|--------|
| `auth.service.spec.ts` | 12 | register, login, logout, token, admin check, OAuth |
| `book.service.spec.ts` | 12 | getAll, getById, search, genre, author, CRUD, stock |
| `cart-order-wallet.service.spec.ts` | 18 | cart ops, COD/online orders, wallet deposit/debit |
| `review-notification-wishlist.service.spec.ts` | 18 | reviews, unread count, wishlist ops |
| `guards.spec.ts` | 6 | AuthGuard & AdminGuard redirect logic |

### Backend — JUnit 5 + Mockito Tests (8 files, ~94 tests)

| Test File | Tests | Covers |
|-----------|-------|--------|
| `AuthServiceImplTest` | 10 | register duplicate check, JWT login, password change |
| `BookServiceImplTest` | 13 | CRUD, keyword search, stock update, featured filter |
| `CartServiceImplTest` | 11 | addItem, quantity update, cartTotal computation |
| `OrderServiceImplTest` | 13 | COD/online placement, status transitions, address |
| `WalletServiceImplTest` | 13 | deposit, debit, insufficient balance exception |
| `ReviewServiceImplTest` | 12 | verified-only check, rating validation, avgRating |
| `NotificationServiceImplTest` | 11 | send, markRead, unread count badge |
| `WishlistServiceImplTest` | 11 | add, remove, duplicate prevention, moveToCart |

**Total: 160+ tests**

---

## 🚀 How to Run

### 1. Frontend

```bash
cd frontend/booknest-angular

# Install dependencies
npm install

# Start Angular dev server (port 4200)
npm start

# Run Jest tests with coverage
npm test

# Verbose test output
npm run test:verbose
```

Coverage report → `src/coverage/index.html`

---

### 2. Backend Microservices

**Prerequisites:** Java 17+, Maven 3.8+, MySQL 8+

#### Create databases:
```sql
CREATE DATABASE booknest_auth;
CREATE DATABASE booknest_books;
CREATE DATABASE booknest_cart;
CREATE DATABASE booknest_orders;
CREATE DATABASE booknest_wallet;
CREATE DATABASE booknest_reviews;
CREATE DATABASE booknest_notifications;
CREATE DATABASE booknest_wishlist;
```

#### Run each service:
```bash
# Start all services (run each in separate terminal)
cd backend/auth-service         && mvn spring-boot:run   # :8081
cd backend/book-service         && mvn spring-boot:run   # :8082
cd backend/cart-service         && mvn spring-boot:run   # :8083
cd backend/order-service        && mvn spring-boot:run   # :8084
cd backend/wallet-service       && mvn spring-boot:run   # :8085
cd backend/review-service       && mvn spring-boot:run   # :8086
cd backend/notification-service && mvn spring-boot:run   # :8087
cd backend/wishlist-service     && mvn spring-boot:run   # :8088
```

#### Run JUnit tests:
```bash
# Single service
cd backend/auth-service && mvn test

# Specific test class
mvn test -Dtest=AuthServiceImplTest

# All services (from backend/)
for svc in auth-service book-service cart-service order-service wallet-service review-service notification-service wishlist-service; do
  echo "Testing $svc..." && cd $svc && mvn test && cd ..
done

# With JaCoCo coverage report
mvn test jacoco:report
# Report: target/site/jacoco/index.html
```

---

## 🔗 API Endpoints

| Service | Base URL | Key Endpoints |
|---------|----------|---------------|
| Auth | `http://localhost:8081/api/auth` | POST /register, POST /login, GET /profile |
| Books | `http://localhost:8082/api/books` | GET /, GET /search, GET /genre/:g, POST, PUT, DELETE |
| Cart | `http://localhost:8083/api/cart` | GET /:userId, POST /:userId/add, DELETE /:userId/clear |
| Orders | `http://localhost:8084/api/orders` | POST /place, POST /online, PUT /:id/status |
| Wallet | `http://localhost:8085/api/wallet` | GET /user/:id, POST /user/:id/add-money, POST /user/:id/pay |
| Reviews | `http://localhost:8086/api/reviews` | GET /book/:id, POST, GET /book/:id/avg-rating |
| Notifications | `http://localhost:8087/api/notifications` | GET /user/:id, PUT /user/:id/read-all |
| Wishlist | `http://localhost:8088/api/wishlist` | GET /:userId, POST /:userId/add, DELETE /:userId/clear |

---

## 🛠 Technology Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Angular 17 (Standalone Components), TypeScript |
| Styling | SCSS, Bootstrap 5, Font Awesome |
| Frontend Testing | Jest 29, ts-jest, Angular HttpClientTestingModule |
| Backend | Java 17, Spring Boot 3.2, Spring MVC, Spring Security |
| Authentication | JWT (JJWT 0.12), Spring Security OAuth2, GitHub OAuth |
| Database | MySQL 8 (each service has own schema) |
| ORM | Spring Data JPA, Hibernate |
| Backend Testing | JUnit 5, Mockito 5.7, AssertJ 3.24 |
| Coverage | JaCoCo (backend), Jest coverage (frontend) |
| Payment | Razorpay Integration + Internal E-Wallet |
| Architecture | Microservices, REST via HTTP |

---

## 👥 Roles & Access

| Role | Access |
|------|--------|
| **Guest** | Browse & search books, view book details |
| **Customer** | All guest features + cart, wishlist, orders, wallet, reviews |
| **Admin** | Full platform management — catalog, orders, users, analytics, reviews |

---

*BookNest Platform v1.0 | 2026 | BridgeLabz*
