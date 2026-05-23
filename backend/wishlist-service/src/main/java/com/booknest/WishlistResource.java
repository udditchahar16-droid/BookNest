package com.booknest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "*")
public class WishlistResource {

    @Autowired private WishlistService wishlistService;

    @GetMapping("/{userId}")
    public ResponseEntity<Wishlist> getWishlist(@PathVariable int userId) {
        Wishlist w = wishlistService.getWishlistByUser(userId);
        if (w == null) {
            // Auto-create empty wishlist for new users
            w = new Wishlist();
            w.setUserId(userId);
            w.setBooks(new java.util.ArrayList<>());
        }
        return ResponseEntity.ok(w);
    }

    @PostMapping("/{userId}/add/{bookId}")
    public ResponseEntity<Wishlist> addBook(@PathVariable int userId,
                                            @PathVariable int bookId) {
        return ResponseEntity.ok(wishlistService.addBook(userId, bookId));
    }

    @DeleteMapping("/{userId}/remove/{bookId}")
    public ResponseEntity<Wishlist> removeBook(@PathVariable int userId,
                                               @PathVariable int bookId) {
        return ResponseEntity.ok(wishlistService.removeBook(userId, bookId));
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<Void> clearWishlist(@PathVariable int userId) {
        wishlistService.clearWishlist(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/move-to-cart/{bookId}")
    public ResponseEntity<String> moveToCart(@PathVariable int userId,
                                             @PathVariable int bookId) {
        wishlistService.moveToCart(userId, bookId);
        return ResponseEntity.ok("Moved to cart");
    }
}
