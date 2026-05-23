package com.booknest;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    // Redis — JWT blacklist + session cache
    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
        "booknest-super-secret-key-must-be-32-bytes!!".getBytes()
    );
    private static final long TOKEN_EXPIRY = 86400000L; // 24 hours

    @Override
    public User register(User user) {
        if (userRepository.existsByEmail(user.getEmail()))
            throw new IllegalArgumentException("Email already registered: " + user.getEmail());
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        return userRepository.save(user);
    }

    @Override
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!passwordEncoder.matches(password, user.getPasswordHash()))
            throw new IllegalArgumentException("Invalid credentials");

        String token = Jwts.builder()
                .subject(email)
                .claim("userId", user.getUserId())
                .claim("role", user.getRole())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRY))
                .signWith(SECRET_KEY)
                .compact();

        // ── Redis: cache user session (userId → token) for 24 hours ──
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(
                    "session:user:" + user.getUserId(),
                    token,
                    24, TimeUnit.HOURS
                );
            } catch (Exception e) {
                System.out.println("[AUTH] Redis session cache failed (non-critical): " + e.getMessage());
            }
        }

        return token;
    }

    @Override
    public void logout(String token) {
        // ── Redis: blacklist token on logout ──
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(
                    "blacklist:token:" + token,
                    "logout",
                    24, TimeUnit.HOURS
                );
            } catch (Exception e) {
                System.out.println("[AUTH] Redis blacklist failed (non-critical): " + e.getMessage());
            }
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            // ── Redis: check blacklist first ──
            if (redisTemplate != null) {
                try {
                    Boolean blacklisted = redisTemplate.hasKey("blacklist:token:" + token);
                    if (Boolean.TRUE.equals(blacklisted)) return false;
                } catch (Exception e) {
                    System.out.println("[AUTH] Redis blacklist check failed (non-critical): " + e.getMessage());
                }
            }
            Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) { return false; }
    }

    @Override
    public String refreshToken(String token) {
        if (!validateToken(token)) throw new IllegalArgumentException("Invalid token");
        String email = Jwts.parser().verifyWith(SECRET_KEY).build()
                .parseSignedClaims(token).getPayload().getSubject();
        return Jwts.builder().subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRY))
                .signWith(SECRET_KEY).compact();
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    @Override
    public User getUserById(int userId) {
        User user = userRepository.findByUserId(userId);
        if (user == null) throw new RuntimeException("User not found: " + userId);
        return user;
    }

    @Override
    public User updateUser(int userId, User incoming) {
        User existing = getUserById(userId);
        if (incoming.getFullName() != null) existing.setFullName(incoming.getFullName());
        if (incoming.getMobile() != null)   existing.setMobile(incoming.getMobile());
        return userRepository.save(existing);
    }

    @Override
    public void changePassword(int userId, String newPassword) {
        User user = userRepository.findByUserId(userId);
        if (user == null) throw new RuntimeException("User not found: " + userId);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
