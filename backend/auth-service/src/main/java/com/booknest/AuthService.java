package com.booknest;

public interface AuthService {
    User register(User user);
    String login(String email, String password);
    void logout(String token);
    boolean validateToken(String token);
    String refreshToken(String token);
    User getUserByEmail(String email);
    User getUserById(int userId);
    User updateUser(int userId, User incoming);
    void changePassword(int userId, String newPassword);
}
