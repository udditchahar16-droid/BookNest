package com.booknest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserId(int userId);
    Order findFirstByOrderByOrderIdDesc();
    List<Order> findByOrderStatus(String status);
    List<Order> findByOrderDateBetween(LocalDate from, LocalDate to);
}
