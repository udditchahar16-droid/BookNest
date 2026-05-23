package com.booknest;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    Cart findByUserId(int userId);
    boolean existsByUserId(int userId);
    void deleteByUserId(int userId);
}
