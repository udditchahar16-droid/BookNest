package com.booknest;
import java.util.List;
import java.util.Optional;
public interface OrderService {
    List<Order> getAllOrders();
    void placeOrder(Cart cart);
    void onlinePayment(Cart cart);
    String changeStatus(String status, int orderId);
    void deleteOrder(int orderId);
    List<Order> getOrderByUserId(int userId);
    void storeAddress(Address address);
    List<Address> getAddressByCustomerId(int customerId);
    Optional<Order> getOrderById(int orderId);
}
