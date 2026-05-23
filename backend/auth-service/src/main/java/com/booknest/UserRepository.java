package com.booknest;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    User findByUserId(int userId);
    boolean existsByEmail(String email);
    List<User> findAllByRole(String role);
    void deleteByUserId(int userId);
}
