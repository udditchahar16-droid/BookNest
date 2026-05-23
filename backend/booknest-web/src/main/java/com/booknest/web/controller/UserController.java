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
 * UserController — Spring MVC controller for customer-facing user operations.
 *
 * Handles: registration, login, logout, profile, order history,
 *          wallet top-up, wallet statements, notifications, and wishlist.
 *
 * Session attributes used:
 *   "userId"    — int, the logged-in user's ID
 *   "userEmail" — String, the logged-in user's email
 *   "userRole"  — String, "CUSTOMER" or "ADMIN"
 *   "token"     — String, the JWT returned by auth-service (stored for future use)
 *
 * NOTE: Thymeleaf template code is intentionally commented out inside each
 *       method to keep the controller readable. Real templates live under
 *       src/main/resources/templates/ — see the accompanying .html files.
 */
@Controller
public class UserController {

    @Autowired private RestTemplate restTemplate;
    @Autowired private ServiceUrlConfig svc;

    // ─── Home ────────────────────────────────────────────────────────────────

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        // Fetch featured books from book-service to show on the landing page
        try {
            Object[] featured = restTemplate.getForObject(
                svc.bookUrl + "/api/books/featured", Object[].class);
            model.addAttribute("featuredBooks", featured);
        } catch (Exception ignored) {
            model.addAttribute("featuredBooks", List.of());
        }
        model.addAttribute("userId", session.getAttribute("userId"));
        model.addAttribute("userRole", session.getAttribute("userRole"));
        return "home"; // templates/home.html
    }

    @GetMapping("/welcome")
    public String welcome(HttpSession session, Model model) {
        model.addAttribute("fullName", session.getAttribute("fullName"));
        return "welcome"; // templates/welcome.html  (post-login landing)
    }

    // ─── Registration ────────────────────────────────────────────────────────

    @GetMapping("/auth/register")
    public String registerForm() {
        return "auth/register"; // templates/auth/register.html
    }

    @PostMapping("/auth/register")
    public String register(@RequestParam String fullName,
                           @RequestParam String email,
                           @RequestParam String password,
                           Model model) {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("fullName", fullName);
            payload.put("email", email);
            payload.put("passwordHash", password);  // auth-service hashes it
            restTemplate.postForObject(svc.authUrl + "/api/auth/register", payload, Object.class);
            return "redirect:/auth/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "auth/register";
        }
    }

    // ─── Login / Logout ───────────────────────────────────────────────────────

    @GetMapping("/auth/login")
    public String loginForm(@RequestParam(required = false) String registered,
                            @RequestParam(required = false) String error,
                            @RequestParam(required = false) String passwordReset,
                            Model model) {
        if (registered != null)    model.addAttribute("info", "Account created! Please log in.");
        if (error != null)         model.addAttribute("error", "Invalid email or password.");
        if (passwordReset != null) model.addAttribute("info", "Password reset ho gaya! Ab login karein.");
        return "auth/login"; // templates/auth/login.html
    }

    @PostMapping("/auth/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        try {
            Map<String, String> credentials = new HashMap<>();
            credentials.put("email", email);
            credentials.put("password", password);

            // auth-service returns a JWT string on success
            String token = restTemplate.postForObject(
                svc.authUrl + "/api/auth/login", credentials, String.class);

            // Fetch user profile to get userId and role
            @SuppressWarnings("unchecked")
            Map<String, Object> user = restTemplate.getForObject(
                svc.authUrl + "/api/auth/profile?email=" + email, Map.class);

            session.setAttribute("token", token);
            session.setAttribute("userId", ((Number) user.get("userId")).intValue());
            session.setAttribute("userEmail", email);
            session.setAttribute("fullName", user.get("fullName"));
            session.setAttribute("userRole", user.get("role"));

            if ("ADMIN".equals(user.get("role"))) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/welcome";
        } catch (Exception e) {
            return "redirect:/auth/login?error=true";
        }
    }


    // ─── Forgot Password ──────────────────────────────────────────────────────

    @GetMapping("/auth/forgot-password")
    public String forgotPasswordForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/auth/forgot-password")
    public String sendOtp(@RequestParam String email, Model model) {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("email", email);
            restTemplate.postForObject(svc.authUrl + "/api/auth/forgot-password", payload, Object.class);
            model.addAttribute("email", email);
            model.addAttribute("info", "OTP aapke email par bhej diya gaya hai.");
            return "auth/verify-otp";
        } catch (Exception e) {
            model.addAttribute("error", "Email send fail ho gaya. Dobara try karein.");
            return "auth/forgot-password";
        }
    }

    @PostMapping("/auth/verify-otp")
    public String verifyOtp(@RequestParam String email,
                            @RequestParam String otp,
                            Model model) {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("email", email);
            payload.put("otp", otp);
            restTemplate.postForObject(svc.authUrl + "/api/auth/verify-otp", payload, Object.class);
            model.addAttribute("email", email);
            model.addAttribute("otp", otp);
            return "auth/reset-password";
        } catch (Exception e) {
            model.addAttribute("email", email);
            model.addAttribute("error", "OTP galat hai ya expire ho gaya.");
            return "auth/verify-otp";
        }
    }

    @PostMapping("/auth/reset-password")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String otp,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("email", email);
            model.addAttribute("otp", otp);
            model.addAttribute("error", "Dono passwords match nahi kar rahe.");
            return "auth/reset-password";
        }
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("email", email);
            payload.put("otp", otp);
            payload.put("newPassword", newPassword);
            restTemplate.postForObject(svc.authUrl + "/api/auth/reset-password", payload, Object.class);
            return "redirect:/auth/login?passwordReset=true";
        } catch (Exception e) {
            model.addAttribute("email", email);
            model.addAttribute("otp", otp);
            model.addAttribute("error", "Password reset fail: " + e.getMessage());
            return "auth/reset-password";
        }
    }

    @GetMapping("/auth/logout")
    public String logout(HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token != null) {
            try {
                restTemplate.postForObject(svc.authUrl + "/api/auth/logout?token=" + token, null, String.class);
            } catch (Exception ignored) {}
        }
        session.invalidate();
        return "redirect:/";
    }

    // ─── Profile ─────────────────────────────────────────────────────────────

    @GetMapping("/user/profile")
    public String viewProfile(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        try {
            Object user = restTemplate.getForObject(
                svc.authUrl + "/api/auth/user/" + userId, Object.class);
            model.addAttribute("user", user);
        } catch (Exception e) {
            model.addAttribute("error", "Could not load profile.");
        }
        return "auth/profile"; // templates/auth/profile.html
    }

    @PostMapping("/user/profile")
    public String updateProfile(@RequestParam String fullName,
                                @RequestParam(required = false) Long mobile,
                                HttpSession session,
                                Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("fullName", fullName);
            payload.put("mobile", mobile);
            restTemplate.put(svc.authUrl + "/api/auth/user/" + userId, payload);
            session.setAttribute("fullName", fullName);
            return "redirect:/user/profile?updated=true";
        } catch (Exception e) {
            model.addAttribute("error", "Profile update failed.");
            return "auth/profile";
        }
    }

    // ─── Order History ───────────────────────────────────────────────────────

    @GetMapping("/user/orders")
    public String viewOrders(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        try {
            Object[] orders = restTemplate.getForObject(
                svc.orderUrl + "/api/orders/user/" + userId, Object[].class);
            model.addAttribute("orders", orders);
        } catch (Exception e) {
            model.addAttribute("orders", List.of());
            model.addAttribute("error", "Could not load orders.");
        }
        return "order/my-orders"; // templates/order/my-orders.html
    }

    // ─── Wallet ──────────────────────────────────────────────────────────────

    @GetMapping("/user/wallet")
    public String viewWallet(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        try {
            // walletId == userId by convention in this platform
            Object wallet = restTemplate.getForObject(
                svc.walletUrl + "/api/wallet/" + userId, Object.class);
            model.addAttribute("wallet", wallet);
        } catch (Exception e) {
            model.addAttribute("error", "Wallet not found. It will be created on first top-up.");
        }
        return "wallet/wallet"; // templates/wallet/wallet.html
    }

    @PostMapping("/user/wallet/topup")
    public String topUp(@RequestParam double amount,
                        HttpSession session,
                        Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("amount", amount);
            payload.put("remarks", "Manual top-up");
            restTemplate.postForObject(
                svc.walletUrl + "/api/wallet/" + userId + "/add", payload, Object.class);
            return "redirect:/user/wallet?topup=success";
        } catch (Exception e) {
            return "redirect:/user/wallet?topup=error";
        }
    }

    @GetMapping("/user/wallet/statements")
    public String viewStatements(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        try {
            Object[] statements = restTemplate.getForObject(
                svc.walletUrl + "/api/wallet/" + userId + "/statements", Object[].class);
            model.addAttribute("statements", statements);
        } catch (Exception e) {
            model.addAttribute("statements", List.of());
        }
        return "wallet/statements"; // templates/wallet/statements.html
    }

    // ─── Notifications ───────────────────────────────────────────────────────

    @GetMapping("/user/notifications")
    public String viewNotifications(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        try {
            Object[] notifications = restTemplate.getForObject(
                svc.notificationUrl + "/api/notifications/user/" + userId, Object[].class);
            model.addAttribute("notifications", notifications);
            // Mark all as read automatically when the page is opened
            restTemplate.postForObject(
                svc.notificationUrl + "/api/notifications/user/" + userId + "/read-all", null, Void.class);
        } catch (Exception e) {
            model.addAttribute("notifications", List.of());
        }
        return "notification/notifications"; // templates/notification/notifications.html
    }

    // ─── Wishlist ────────────────────────────────────────────────────────────

    @GetMapping("/user/wishlist")
    public String viewWishlist(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        try {
            Object wishlist = restTemplate.getForObject(
                svc.wishlistUrl + "/api/wishlist/" + userId, Object.class);
            model.addAttribute("wishlist", wishlist);
        } catch (Exception e) {
            model.addAttribute("wishlist", null);
        }
        return "wishlist/wishlist"; // templates/wishlist/wishlist.html
    }

    @PostMapping("/user/wishlist/remove")
    public String removeFromWishlist(@RequestParam int bookId, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        restTemplate.delete(svc.wishlistUrl + "/api/wishlist/" + userId + "/remove/" + bookId);
        return "redirect:/user/wishlist";
    }

    @PostMapping("/user/wishlist/move-to-cart")
    public String moveWishlistToCart(@RequestParam int bookId, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        // Remove from wishlist
        restTemplate.delete(svc.wishlistUrl + "/api/wishlist/" + userId + "/remove/" + bookId);
        // Add to cart with qty 1
        Map<String, Object> payload = new HashMap<>();
        payload.put("bookId", bookId);
        payload.put("quantity", 1);
        restTemplate.postForObject(svc.cartUrl + "/api/cart/" + userId + "/add", payload, Object.class);
        return "redirect:/book/cart";
    }
}
