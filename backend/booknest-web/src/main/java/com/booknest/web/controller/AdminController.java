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
 * AdminController — Spring MVC controller for the admin panel.
 *
 * All methods begin with a role-guard that redirects non-admins to the home page.
 *
 * Handles: dashboard, book catalog management, inventory, order management,
 *          user management, platform analytics, and review moderation.
 *
 * NOTE: Thymeleaf template code is intentionally commented out inside each
 *       method to keep the controller readable. Real templates live under
 *       src/main/resources/templates/ — see the accompanying .html files.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private RestTemplate restTemplate;
    @Autowired private ServiceUrlConfig svc;

    // ── Helper — redirect non-admins ──────────────────────────────────────────

    private boolean isAdmin(HttpSession session) {
        return "ADMIN".equals(session.getAttribute("userRole"));
    }

    // ─── Dashboard ───────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";
        try {
            // Quick summary stats: total orders, total books
            Object[] orders = restTemplate.getForObject(svc.orderUrl + "/api/orders", Object[].class);
            Object[] books  = restTemplate.getForObject(svc.bookUrl  + "/api/books",  Object[].class);
            model.addAttribute("totalOrders", orders != null ? orders.length : 0);
            model.addAttribute("totalBooks",  books  != null ? books.length  : 0);

            // Recent 5 orders for the dashboard table
            model.addAttribute("recentOrders", orders);
        } catch (Exception e) {
            model.addAttribute("error", "Could not load dashboard data.");
        }
        return "admin/dashboard"; // templates/admin/dashboard.html
    }

    // ─── Book Catalog Management ──────────────────────────────────────────────

    @GetMapping("/books")
    public String manageBooks(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";
        try {
            Object[] books = restTemplate.getForObject(svc.bookUrl + "/api/books", Object[].class);
            model.addAttribute("books", books);
        } catch (Exception e) {
            model.addAttribute("books", List.of());
        }
        return "admin/books"; // templates/admin/books.html
    }

    @GetMapping("/books/add")
    public String addBookForm(HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        return "admin/book-form"; // templates/admin/book-form.html
    }

    @PostMapping("/books/add")
    public String addBook(@RequestParam String title,
                          @RequestParam String author,
                          @RequestParam String isbn,
                          @RequestParam String genre,
                          @RequestParam String publisher,
                          @RequestParam double price,
                          @RequestParam int stock,
                          @RequestParam(required = false) String description,
                          @RequestParam(required = false) String coverImageUrl,
                          HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("title", title);
            payload.put("author", author);
            payload.put("isbn", isbn);
            payload.put("genre", genre);
            payload.put("publisher", publisher);
            payload.put("price", price);
            payload.put("stock", stock);
            payload.put("description", description);
            payload.put("coverImageUrl", coverImageUrl);
            restTemplate.postForObject(svc.bookUrl + "/api/books", payload, Object.class);
            return "redirect:/admin/books?added=true";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to add book: " + e.getMessage());
            return "admin/book-form";
        }
    }

    @GetMapping("/books/edit/{bookId}")
    public String editBookForm(@PathVariable int bookId, HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";
        try {
            Object book = restTemplate.getForObject(svc.bookUrl + "/api/books/" + bookId, Object.class);
            model.addAttribute("book", book);
        } catch (Exception e) {
            return "redirect:/admin/books?error=notfound";
        }
        return "admin/book-form";
    }

    @PostMapping("/books/edit/{bookId}")
    public String editBook(@PathVariable int bookId,
                           @RequestParam String title,
                           @RequestParam String author,
                           @RequestParam double price,
                           @RequestParam int stock,
                           @RequestParam(required = false) String description,
                           HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        Map<String, Object> payload = new HashMap<>();
        payload.put("bookId", bookId);
        payload.put("title", title);
        payload.put("author", author);
        payload.put("price", price);
        payload.put("stock", stock);
        payload.put("description", description);
        restTemplate.put(svc.bookUrl + "/api/books/" + bookId, payload);
        return "redirect:/admin/books?updated=true";
    }

    @PostMapping("/books/delete/{bookId}")
    public String deleteBook(@PathVariable int bookId, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        restTemplate.delete(svc.bookUrl + "/api/books/" + bookId);
        return "redirect:/admin/books?deleted=true";
    }

    // ─── Inventory ───────────────────────────────────────────────────────────

    @GetMapping("/inventory")
    public String viewInventory(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";
        try {
            Object[] books = restTemplate.getForObject(svc.bookUrl + "/api/books", Object[].class);
            model.addAttribute("books", books);
        } catch (Exception e) {
            model.addAttribute("books", List.of());
        }
        return "admin/inventory"; // templates/admin/inventory.html
    }

    @PostMapping("/inventory/update/{bookId}")
    public String updateStock(@PathVariable int bookId,
                              @RequestParam int stock,
                              HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        Map<String, Integer> payload = new HashMap<>();
        payload.put("stock", stock);
        restTemplate.patchForObject(svc.bookUrl + "/api/books/" + bookId + "/stock", payload, String.class);
        return "redirect:/admin/inventory?updated=true";
    }

    // ─── Order Management ────────────────────────────────────────────────────

    @GetMapping("/orders")
    public String manageOrders(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";
        try {
            Object[] orders = restTemplate.getForObject(svc.orderUrl + "/api/orders", Object[].class);
            model.addAttribute("orders", orders);
        } catch (Exception e) {
            model.addAttribute("orders", List.of());
        }
        return "admin/orders"; // templates/admin/orders.html
    }

    @PostMapping("/orders/{orderId}/status")
    public String changeOrderStatus(@PathVariable int orderId,
                                    @RequestParam String status,
                                    HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        restTemplate.put(svc.orderUrl + "/api/orders/" + orderId + "/status?status=" + status, null);
        return "redirect:/admin/orders?updated=true";
    }

    // ─── User Management ─────────────────────────────────────────────────────

    @GetMapping("/users")
    public String manageUsers(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";
        try {
            Object[] users = restTemplate.getForObject(
                svc.authUrl + "/api/auth/users/role/CUSTOMER", Object[].class);
            model.addAttribute("users", users);
        } catch (Exception e) {
            model.addAttribute("users", List.of());
        }
        return "admin/users"; // templates/admin/users.html
    }

    @PostMapping("/users/{userId}/suspend")
    public String suspendUser(@PathVariable int userId, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        // Suspend = delete in current auth-service implementation
        restTemplate.delete(svc.authUrl + "/api/auth/user/" + userId);
        return "redirect:/admin/users?suspended=true";
    }

    // ─── Analytics ───────────────────────────────────────────────────────────

    @GetMapping("/analytics")
    public String viewPlatformAnalytics(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";
        try {
            Object[] orders = restTemplate.getForObject(svc.orderUrl + "/api/orders", Object[].class);
            model.addAttribute("orders", orders);
            // Total revenue: sum amountPaid across all orders (computed in Thymeleaf)
            model.addAttribute("totalOrders", orders != null ? orders.length : 0);
        } catch (Exception e) {
            model.addAttribute("error", "Could not load analytics.");
        }
        return "admin/analytics"; // templates/admin/analytics.html
    }

    // ─── Review Moderation ───────────────────────────────────────────────────

    @GetMapping("/reviews")
    public String viewAllReviews(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";
        try {
            Object[] reviews = restTemplate.getForObject(
                svc.reviewUrl + "/api/reviews", Object[].class);
            model.addAttribute("reviews", reviews);
        } catch (Exception e) {
            model.addAttribute("reviews", List.of());
        }
        return "admin/reviews"; // templates/admin/reviews.html
    }

    @PostMapping("/reviews/{reviewId}/delete")
    public String moderateReview(@PathVariable int reviewId, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        restTemplate.delete(svc.reviewUrl + "/api/reviews/" + reviewId);
        return "redirect:/admin/reviews?deleted=true";
    }
}
