export interface User {
  userId: number;
  fullName: string;
  email: string;
  role: string;
  mobile?: string;
  provider?: string;
  createdAt?: string;
}

export interface Book {
  bookId: number;
  title: string;
  author: string;
  isbn?: string;
  genre?: string;
  publisher?: string;
  price: number;
  stock: number;
  rating?: number;
  description?: string;
  coverImageUrl?: string;
  publishedDate?: string;
  featured?: boolean;
}

export interface CartItem {
  itemId: number;
  bookId: number;
  bookTitle: string;
  price: number;
  quantity: number;
}

export interface Cart {
  cartId: number;
  userId: number;
  totalPrice: number;
  items: CartItem[];
}

export interface Address {
  addressId?: number;
  customerId?: number;
  fullName: string;
  mobileNumber: string;
  flatNumber: string;
  city: string;
  state: string;
  pincode: string;
}

export interface Order {
  orderId: number;
  userId: number;
  bookId: number;
  bookTitle?: string;
  orderDate: string;
  amountPaid: number;
  modeOfPayment: string;
  orderStatus: string;
  quantity: number;
  address?: Address;
}

export interface PlaceOrderRequest {
  userId: number;
  bookId: number;
  bookTitle: string;
  quantity: number;
  amountPaid: number;
  modeOfPayment: string;
  address: Address;
}

export interface Wallet {
  walletId: number;
  currentBalance: number;
  statements?: Statement[];
}

export interface Statement {
  statementId: number;
  transactionType: string;
  amount: number;
  dateTime: string;
  orderId?: number;
  transactionRemarks?: string;
}

export interface Review {
  reviewId: number;
  bookId: number;
  userId: number;
  rating: number;
  comment: string;
  reviewDate: string;
  verified: boolean;
}

export interface Notification {
  notificationId: number;
  userId: number;
  type: string;
  message: string;
  isRead: boolean;
  createdAt: string;
}

export interface WishlistItem {
  itemId: number;
  bookId: number;
  bookTitle: string;
  bookPrice: number;
}

export interface Wishlist {
  wishlistId: number;
  userId: number;
  createdAt: string;
  books: WishlistItem[];
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  mobile: string;
}
