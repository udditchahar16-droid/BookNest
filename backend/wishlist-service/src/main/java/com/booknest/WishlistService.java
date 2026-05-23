package com.booknest;
import java.util.List;
public interface WishlistService {
    Wishlist getWishlistByUser(int userId);
    Wishlist addBook(int userId, int bookId);
    Wishlist removeBook(int userId, int bookId);
    void clearWishlist(int userId);
    void moveToCart(int userId, int bookId);
    List<Wishlist> getAllWishlists();
}
