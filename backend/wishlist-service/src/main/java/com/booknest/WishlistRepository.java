package com.booknest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {
    Wishlist findByUserId(int userId);
    boolean existsByUserId(int userId);
    Optional<Wishlist> findByWishlistId(int id);
    void deleteByUserId(int userId);
}
