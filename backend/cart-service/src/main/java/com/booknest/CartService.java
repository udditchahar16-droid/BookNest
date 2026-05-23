package com.booknest;

import java.util.List;

public interface CartService {
    Cart getCartByUser(int userId);
    Cart addItem(int userId, int bookId, int quantity);
    Cart removeItem(int userId, int itemId);
    Cart updateQuantity(int userId, int itemId, int quantity);
    void clearCart(int userId);
    double cartTotal(Cart cart);
    List<Cart> getAllCarts();
}
