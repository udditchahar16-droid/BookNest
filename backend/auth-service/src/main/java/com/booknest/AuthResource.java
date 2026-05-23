package com.booknest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthResource {

    @Autowired private AuthService     authService;
    @Autowired private UserRepository  userRepository;
    @Autowired private EmailService    emailService;   // ← welcome email

    // ── Register Customer ─────────────────────────────────────────────────────
    /**
     * POST /api/auth/register
     * Body: { fullName, email, password, mobile }
     *
     * After successful registration:
     *   → sends "Welcome to BookNest" HTML email via SMTP
     */
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody Map<String, String> req) {
        User user = new User();
        user.setFullName(req.get("fullName"));
        user.setEmail(req.get("email"));
        user.setPasswordHash(req.get("password")); // AuthServiceImpl bcrypts this
        user.setRole("CUSTOMER");
        parseMobile(req.get("mobile"), user);

        User saved = authService.register(user);

        // ── Welcome Email ──────────────────────────────────────────────────────
        // Async call — email fail hone se registration fail nahi hogi
        new Thread(() ->
            emailService.sendWelcomeEmail(saved.getEmail(), saved.getFullName())
        ).start();

        return ResponseEntity.ok(saved);
    }

    // ── Register Admin ────────────────────────────────────────────────────────
    @PostMapping("/register/admin")
    public ResponseEntity<User> registerAdmin(@RequestBody Map<String, String> req) {
        User user = new User();
        user.setFullName(req.get("fullName"));
        user.setEmail(req.get("email"));
        user.setPasswordHash(req.get("password"));
        user.setRole("ADMIN");
        parseMobile(req.get("mobile"), user);

        User saved = authService.register(user);

        // Admin ko bhi welcome email bhejo
        new Thread(() ->
            emailService.sendWelcomeEmail(saved.getEmail(), saved.getFullName())
        ).start();

        return ResponseEntity.ok(saved);
    }

    // ── Login ─────────────────────────────────────────────────────────────────
    /** POST /api/auth/login → JWT token string */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> req) {
        String token = authService.login(req.get("email"), req.get("password"));
        return ResponseEntity.ok(token);
    }

    // ── Logout ────────────────────────────────────────────────────────────────
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String token) {
        authService.logout(token);
        return ResponseEntity.ok("Logged out");
    }

    // ── Profile ───────────────────────────────────────────────────────────────
    /** GET /api/auth/profile?email=... — called after login to get userId + role */
    @GetMapping("/profile")
    public ResponseEntity<User> profile(@RequestParam String email) {
        return ResponseEntity.ok(authService.getUserByEmail(email));
    }

    // ── User CRUD ─────────────────────────────────────────────────────────────
    @GetMapping("/user/{id}")
    public ResponseEntity<User> getUser(@PathVariable int id) {
        return ResponseEntity.ok(authService.getUserById(id));
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<User> updateUser(@PathVariable int id,
                                           @RequestBody User user) {
        return ResponseEntity.ok(authService.updateUser(id, user));
    }

    @Transactional
    @DeleteMapping("/user/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable int id) {
        userRepository.deleteByUserId(id);
        return ResponseEntity.ok("User deleted");
    }

    // ── Admin: get all users by role ──────────────────────────────────────────
    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable String role) {
        return ResponseEntity.ok(userRepository.findAllByRole(role));
    }

    // ── Helper ───────────────────────────────────────────────────────────────
    private void parseMobile(String mobile, User user) {
        if (mobile == null || mobile.isBlank()) return;
        try {
            user.setMobile(Long.parseLong(mobile.replaceAll("[^0-9]", "")));
        } catch (NumberFormatException ignored) {}
    }
}
