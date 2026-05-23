package com.booknest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WishlistServiceImpl implements WishlistService {

    @Autowired private WishlistRepository wishlistRepository;
    @Autowired private RestTemplate restTemplate;

    @Value("${service.cart.url:http://localhost:8083}")
    private String cartServiceUrl;

    @Value("${service.book.url:http://localhost:8082}")
    private String bookServiceUrl;

    @Override
    public Wishlist getWishlistByUser(int userId) { return wishlistRepository.findByUserId(userId); }

    @Override
    public Wishlist addBook(int userId, int bookId) {
        Wishlist w = wishlistRepository.findByUserId(userId);
        if (w == null) { w = new Wishlist(); w.setUserId(userId); }
        boolean dup = w.getBooks().stream().anyMatch(b -> b.getBookId() == bookId);
        if (!dup) {
            WishlistItem item = new WishlistItem();
            item.setBookId(bookId);
            // Fetch book details from book-service so title/price show in wishlist
            try {
                java.util.Map<?, ?> book = restTemplate.getForObject(
                    bookServiceUrl + "/api/books/" + bookId, java.util.Map.class);
                if (book != null) {
                    item.setBookTitle(book.get("title") != null ? book.get("title").toString() : "Book #" + bookId);
                    item.setBookPrice(book.get("price") != null ? ((Number) book.get("price")).doubleValue() : 0.0);
                } else {
                    item.setBookTitle("Book #" + bookId);
                }
            } catch (Exception e) {
                item.setBookTitle("Book #" + bookId);
            }
            w.getBooks().add(item);
        }
        return wishlistRepository.save(w);
    }

    @Override
    public Wishlist removeBook(int userId, int bookId) {
        Wishlist w = wishlistRepository.findByUserId(userId);
        if (w == null) return null;
        w.getBooks().removeIf(b -> b.getBookId() == bookId);
        return wishlistRepository.save(w);
    }

    @Override
    public void clearWishlist(int userId) {
        Wishlist w = wishlistRepository.findByUserId(userId);
        if (w == null) return;
        w.getBooks().clear();
        wishlistRepository.save(w);
    }

    // FIX: was only removing from wishlist — never added item to cart
    @Override
    public void moveToCart(int userId, int bookId) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("bookId", bookId);
            payload.put("quantity", 1);
            restTemplate.postForObject(
                cartServiceUrl + "/api/cart/" + userId + "/add", payload, Object.class);
        } catch (Exception e) {
            System.err.println("[WISHLIST] Could not add to cart: " + e.getMessage());
        }
        removeBook(userId, bookId);
    }

    @Override
    public List<Wishlist> getAllWishlists() { return wishlistRepository.findAll(); }
}
