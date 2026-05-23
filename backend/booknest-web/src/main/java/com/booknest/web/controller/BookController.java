package com.booknest.web.controller;

import com.booknest.web.config.ServiceUrlConfig;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BookController — Spring MVC controller for the book catalog and shopping flow.
 *
 * Handles: catalog browsing, search, genre/author filter, book detail,
 *          shopping cart, add-to-wishlist, reviews, checkout, and payment.
 *
 * NOTE: Thymeleaf template code is intentionally commented out inside each
 *       method to keep the controller readable. Real templates live under
 *       src/main/resources/templates/ — see the accompanying .html files.
 */
@Controller
public class BookController {

    @Autowired private RestTemplate restTemplate;
    @Autowired private ServiceUrlConfig svc;

    // ─── Catalog Browsing ────────────────────────────────────────────────────

    @GetMapping("/books")
    public String home(Model model) {
        try {
            Object[] books = restTemplate.getForObject(svc.bookUrl + "/api/books", Object[].class);
            model.addAttribute("books", books);
        } catch (Exception e) {
            model.addAttribute("books", List.of());
        }
        return "book/catalog"; // templates/book/catalog.html
    }

    @GetMapping("/books/search")
    public String searchBooks(@RequestParam String keyword, Model model) {
        try {
            Object[] books = restTemplate.getForObject(
                svc.bookUrl + "/api/books/search?keyword=" + keyword, Object[].class);
            model.addAttribute("books", books);
            model.addAttribute("keyword", keyword);
        } catch (Exception e) {
            model.addAttribute("books", List.of());
        }
        return "book/catalog";
    }

    @GetMapping("/books/genre/{genre}")
    public String getByGenre(@PathVariable String genre, Model model) {
        try {
            Object[] books = restTemplate.getForObject(
                svc.bookUrl + "/api/books/genre/" + genre, Object[].class);
            model.addAttribute("books", books);
            model.addAttribute("genre", genre);
        } catch (Exception e) {
            model.addAttribute("books", List.of());
        }
        return "book/catalog";
    }

    @GetMapping("/books/author/{author}")
    public String getByAuthor(@PathVariable String author, Model model) {
        try {
            Object[] books = restTemplate.getForObject(
                svc.bookUrl + "/api/books/author/" + author, Object[].class);
            model.addAttribute("books", books);
            model.addAttribute("author", author);
        } catch (Exception e) {
            model.addAttribute("books", List.of());
        }
        return "book/catalog";
    }

    @GetMapping("/books/featured")
    public String viewFeatured(Model model) {
        try {
            Object[] books = restTemplate.getForObject(
                svc.bookUrl + "/api/books/featured", Object[].class);
            model.addAttribute("books", books);
        } catch (Exception e) {
            model.addAttribute("books", List.of());
        }
        return "book/catalog";
    }

    // ─── Book Detail ─────────────────────────────────────────────────────────

    @GetMapping("/books/{bookId}")
    public String viewBookDetail(@PathVariable int bookId, Model model) {
        try {
            Object book = restTemplate.getForObject(
                svc.bookUrl + "/api/books/" + bookId, Object.class);
            model.addAttribute("book", book);
        } catch (Exception e) {
            return "redirect:/books?error=notfound";
        }
        try {
            Object[] reviews = restTemplate.getForObject(
                svc.reviewUrl + "/api/reviews/book/" + bookId, Object[].class);
            model.addAttribute("reviews", reviews);
        } catch (Exception e) {
            model.addAttribute("reviews", List.of());
        }
        try {
            Double avg = restTemplate.getForObject(
                svc.reviewUrl + "/api/reviews/book/" + bookId + "/avg-rating", Double.class);
            model.addAttribute("avgRating", avg != null ? avg : 0.0);
        } catch (Exception e) {
            model.addAttribute("avgRating", 0.0);
        }
        return "book/book-detail"; // templates/book/book-detail.html
    }

    // ─── Cart ────────────────────────────────────────────────────────────────

    @GetMapping("/book/cart")
    public String shoppingCart(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        try {
            Object cart = restTemplate.getForObject(
                svc.cartUrl + "/api/cart/" + userId, Object.class);
            model.addAttribute("cart", cart);
        } catch (Exception e) {
            model.addAttribute("cart", null);
        }
        return "cart/cart"; // templates/cart/cart.html
    }

    @PostMapping("/book/cart/add")
    public String addToCart(@RequestParam int bookId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        Map<String, Object> payload = new HashMap<>();
        payload.put("bookId", bookId);
        payload.put("quantity", quantity);
        restTemplate.postForObject(svc.cartUrl + "/api/cart/" + userId + "/add", payload, Object.class);
        return "redirect:/book/cart";
    }

    @PostMapping("/book/cart/remove")
    public String removeFromCart(@RequestParam int itemId, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        restTemplate.delete(svc.cartUrl + "/api/cart/" + userId + "/remove/" + itemId);
        return "redirect:/book/cart";
    }

    @PostMapping("/book/cart/update")
    public String updateCartQuantity(@RequestParam int itemId,
                                     @RequestParam int quantity,
                                     HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        Map<String, Integer> payload = new HashMap<>();
        payload.put("quantity", quantity);
        restTemplate.put(svc.cartUrl + "/api/cart/" + userId + "/update/" + itemId, payload);
        return "redirect:/book/cart";
    }

    // ─── Add to Wishlist ─────────────────────────────────────────────────────

    @PostMapping("/book/wishlist/add")
    public String addToWishlist(@RequestParam int bookId, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        Map<String, Integer> payload = new HashMap<>();
        payload.put("bookId", bookId);
        restTemplate.postForObject(
            svc.wishlistUrl + "/api/wishlist/" + userId + "/add", payload, Object.class);
        return "redirect:/books/" + bookId + "?wishlisted=true";
    }

    // ─── Reviews ─────────────────────────────────────────────────────────────

    @PostMapping("/books/{bookId}/review")
    public String addReview(@PathVariable int bookId,
                            @RequestParam int rating,
                            @RequestParam String comment,
                            HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        Map<String, Object> payload = new HashMap<>();
        payload.put("bookId", bookId);
        payload.put("userId", userId);
        payload.put("rating", rating);
        payload.put("comment", comment);
        restTemplate.postForObject(svc.reviewUrl + "/api/reviews", payload, Object.class);
        return "redirect:/books/" + bookId + "#reviews";
    }

    // ─── Checkout ────────────────────────────────────────────────────────────

    @GetMapping("/book/checkout")
    public String checkout(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        try {
            Object cart = restTemplate.getForObject(
                svc.cartUrl + "/api/cart/" + userId, Object.class);
            model.addAttribute("cart", cart);
            // Load saved addresses
            Object[] addresses = restTemplate.getForObject(
                svc.orderUrl + "/api/orders/addresses/" + userId, Object[].class);
            model.addAttribute("addresses", addresses);
        } catch (Exception e) {
            model.addAttribute("cart", null);
            model.addAttribute("addresses", List.of());
        }
        return "order/checkout"; // templates/order/checkout.html
    }

    @GetMapping("/book/payment-mode")
    public String paymentMode(@RequestParam(required = false) Integer addressId,
                              HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        session.setAttribute("checkoutAddressId", addressId);
        // Load wallet balance for display on the payment-mode page
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> wallet = restTemplate.getForObject(
                svc.walletUrl + "/api/wallet/" + userId, Map.class);
            model.addAttribute("walletBalance", wallet != null ? wallet.get("currentBalance") : 0.0);
        } catch (Exception e) {
            model.addAttribute("walletBalance", 0.0);
        }
        return "order/payment-mode"; // templates/order/payment-mode.html
    }

    @PostMapping("/book/order/cod")
    public String cashOnDelivery(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        try {
            // Fetch current cart and submit COD order
            Object cart = restTemplate.getForObject(
                svc.cartUrl + "/api/cart/" + userId, Object.class);
            restTemplate.postForObject(svc.orderUrl + "/api/orders/place", cart, Void.class);
            // Clear cart after successful order
            restTemplate.delete(svc.cartUrl + "/api/cart/" + userId + "/clear");
            return "redirect:/user/orders?ordered=cod";
        } catch (Exception e) {
            return "redirect:/book/payment-mode?error=true";
        }
    }

    @PostMapping("/book/order/pay")
    public String payMoney(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        try {
            Object cart = restTemplate.getForObject(
                svc.cartUrl + "/api/cart/" + userId, Object.class);
            restTemplate.postForObject(svc.orderUrl + "/api/orders/online", cart, Void.class);
            restTemplate.delete(svc.cartUrl + "/api/cart/" + userId + "/clear");
            return "redirect:/user/orders?ordered=online";
        } catch (Exception e) {
            return "redirect:/book/payment-mode?error=insufficient";
        }
    }
}
