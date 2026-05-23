package com.booknest;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Component
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired private UserRepository userRepository;

    @Value("${jwt.secret:dGhpcyBpcyBhIDMyLWJ5dGUgc2VjcmV0IGtleQ==}")
    private String jwtSecret;

    private static final long TOKEN_EXPIRY = 86400000L; // 24h
    private static final String REDIRECT_URI = "http://localhost:4200/auth/oauth-callback";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse res,
                                        Authentication auth) throws IOException {
        OAuth2User oauth2User = (OAuth2User) auth.getPrincipal();

        String email = oauth2User.getAttribute("email");
        if (email == null || email.isBlank()) {
            String login = oauth2User.getAttribute("login");
            email = (login != null ? login : "user") + "@github.com";
        }
        String name = oauth2User.getAttribute("name");
        if (name == null || name.isBlank()) name = oauth2User.getAttribute("login");

        final String finalEmail = email;
        final String finalName  = name != null ? name : "GitHub User";

        Optional<User> existing = userRepository.findByEmail(finalEmail);
        User user = existing.orElseGet(() -> {
            User u = new User();
            u.setEmail(finalEmail);
            u.setFullName(finalName);
            u.setPasswordHash("GITHUB_OAUTH");
            u.setRole("CUSTOMER");
            u.setProvider("github");
            return userRepository.save(u);
        });

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
            .subject(user.getEmail())
            .claim("userId", user.getUserId())
            .claim("role", user.getRole())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRY))
            .signWith(key)
            .compact();

        String redirectUrl = REDIRECT_URI
            + "?token="  + URLEncoder.encode(token, StandardCharsets.UTF_8)
            + "&email="  + URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8)
            + "&name="   + URLEncoder.encode(user.getFullName(), StandardCharsets.UTF_8)
            + "&userId=" + user.getUserId()
            + "&role="   + user.getRole();

        getRedirectStrategy().sendRedirect(req, res, redirectUrl);
    }
}
