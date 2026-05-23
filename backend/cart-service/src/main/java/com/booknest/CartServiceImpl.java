package com.booknest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    @Autowired private CartRepository cartRepository;
    @Autowired private RestTemplate restTemplate;

    @Value("${service.book.url:http://localhost:8082}")
    private String bookServiceUrl;

    // Book service se price AND title dono fetch karo
    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchBook(int bookId) {
        try {
            return restTemplate.getForObject(
                bookServiceUrl + "/api/books/" + bookId, Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    // Naya user ka cart null nahi hoga — empty cart return hoga
    @Override
    public Cart getCartByUser(int userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart = cartRepository.save(cart);
        }
        return cart;
    }

    @Override
    public Cart addItem(int userId, int bookId, int quantity) {
        Cart cart = getCartByUser(userId);

        // Agar book already cart mein hai toh quantity badhao
        boolean found = false;
        for (CartItem item : cart.getItems()) {
            if (item.getBookId() == bookId) {
                item.setQuantity(item.getQuantity() + quantity);
                found = true;
                break;
            }
        }

        if (!found) {
            // Book service se price aur title fetch karo
            Map<String, Object> book = fetchBook(bookId);
            CartItem item = new CartItem();
            item.setBookId(bookId);
            item.setQuantity(quantity);

            if (book != null) {
                // FIX: bookTitle bhi set karo — checkout mein zaruri hai
                item.setBookTitle(book.get("title") != null
                    ? book.get("title").toString() : "Book #" + bookId);
                item.setPrice(book.get("price") != null
                    ? ((Number) book.get("price")).doubleValue() : 0.0);
            } else {
                item.setBookTitle("Book #" + bookId);
                item.setPrice(0.0);
            }
            cart.getItems().add(item);
        }

        cart.setTotalPrice(cartTotal(cart));
        return cartRepository.save(cart);
    }

    @Override
    public Cart removeItem(int userId, int itemId) {
        Cart cart = getCartByUser(userId);
        cart.getItems().removeIf(i -> i.getItemId() == itemId);
        cart.setTotalPrice(cartTotal(cart));
        return cartRepository.save(cart);
    }

    @Override
    public Cart updateQuantity(int userId, int itemId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be > 0");
        Cart cart = getCartByUser(userId);
        cart.getItems().stream()
            .filter(i -> i.getItemId() == itemId)
            .findFirst()
            .ifPresent(i -> i.setQuantity(quantity));
        cart.setTotalPrice(cartTotal(cart));
        return cartRepository.save(cart);
    }

    @Override
    public void clearCart(int userId) {
        Cart cart = getCartByUser(userId);
        cart.getItems().clear();
        cart.setTotalPrice(0.0);
        cartRepository.save(cart);
    }

    @Override
    public double cartTotal(Cart cart) {
        return cart.getItems().stream()
            .mapToDouble(i -> i.getPrice() * i.getQuantity())
            .sum();
    }

    @Override
    public List<Cart> getAllCarts() { return cartRepository.findAll(); }
}
