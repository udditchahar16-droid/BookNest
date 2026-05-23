package com.booknest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired private OrderRepository     orderRepository;
    @Autowired private AddressRepository   addressRepository;
    @Autowired private OrderEventPublisher eventPublisher;
    @Autowired private OrderEmailService   orderEmailService;

    // RestTemplate to call book-service and auth-service
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BOOK_SERVICE_URL = "http://localhost:8082/api/books";

    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceUrl;

    @Override
    public List<Order> getAllOrders() { return orderRepository.findAll(); }

    // ── COD Order ─────────────────────────────────────────────
    @Override
    public void placeOrder(Cart cart) {
        Address savedAddr = saveAddressIfPresent(cart);

        Order o = new Order();
        o.setUserId(cart.getUserId());
        o.setOrderDate(LocalDate.now());
        o.setModeOfPayment("COD");
        o.setOrderStatus("PLACED");
        o.setAmountPaid(cart.getTotalPrice());
        o.setQuantity(cart.getQuantity() > 0 ? cart.getQuantity() : 1);
        o.setBookId(cart.getBookId());
        o.setBookTitle(cart.getBookTitle());
        if (savedAddr != null) o.setAddress(savedAddr);
        orderRepository.save(o);

        // ── Decrease stock in book-service ──
        decreaseStock(cart.getBookId(), o.getQuantity());

        // ── Order Placed Email (direct SMTP) ──
        final Order savedOrder = o;
        new Thread(() -> {
            UserInfo user = fetchUserInfo(savedOrder.getUserId());
            if (user != null) {
                orderEmailService.sendOrderPlacedEmail(
                    user.email, user.fullName,
                    savedOrder.getOrderId(), savedOrder.getAmountPaid(),
                    savedOrder.getModeOfPayment(), savedOrder.getBookTitle()
                );
            }
        }).start();

        eventPublisher.publishOrderPlaced(new OrderEvent(
            o.getOrderId(), o.getUserId(),
            "ORDER_PLACED", "PLACED", "COD",
            o.getAmountPaid(), o.getOrderDate()));
    }

    // ── Online (Wallet/Razorpay) Order ────────────────────────
    @Override
    public void onlinePayment(Cart cart) {
        Address savedAddr = saveAddressIfPresent(cart);

        Order o = new Order();
        o.setUserId(cart.getUserId());
        o.setOrderDate(LocalDate.now());
        o.setModeOfPayment(cart.getModeOfPayment() != null
            ? cart.getModeOfPayment() : "WALLET");
        o.setOrderStatus("PLACED");
        o.setAmountPaid(cart.getTotalPrice());
        o.setQuantity(cart.getQuantity() > 0 ? cart.getQuantity() : 1);
        o.setBookId(cart.getBookId());
        o.setBookTitle(cart.getBookTitle());
        if (savedAddr != null) o.setAddress(savedAddr);
        orderRepository.save(o);

        // ── Decrease stock in book-service ──
        decreaseStock(cart.getBookId(), o.getQuantity());

        // ── Order Placed Email (direct SMTP) ──
        final Order savedOrder = o;
        new Thread(() -> {
            UserInfo user = fetchUserInfo(savedOrder.getUserId());
            if (user != null) {
                orderEmailService.sendOrderPlacedEmail(
                    user.email, user.fullName,
                    savedOrder.getOrderId(), savedOrder.getAmountPaid(),
                    savedOrder.getModeOfPayment(), savedOrder.getBookTitle()
                );
            }
        }).start();

        eventPublisher.publishOrderPlaced(new OrderEvent(
            o.getOrderId(), o.getUserId(),
            "ORDER_PLACED", "PLACED", o.getModeOfPayment(),
            o.getAmountPaid(), o.getOrderDate()));
    }

    // ── Status Change (Admin only — enforced in OrderResource) ─
    @Override
    public String changeStatus(String status, int orderId) {
        Order o = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        o.setOrderStatus(status);
        orderRepository.save(o);

        // ── Status Change Email (direct SMTP — RabbitMQ pe depend nahi) ──
        final Order updatedOrder = o;
        new Thread(() -> {
            UserInfo user = fetchUserInfo(updatedOrder.getUserId());
            if (user != null) {
                orderEmailService.sendOrderStatusEmail(
                    user.email, user.fullName,
                    updatedOrder.getOrderId(), status, updatedOrder.getBookTitle()
                );
            }
        }).start();

        eventPublisher.publishOrderStatusChanged(new OrderEvent(
            o.getOrderId(), o.getUserId(),
            "ORDER_STATUS_CHANGED", status,
            o.getModeOfPayment(), o.getAmountPaid(), o.getOrderDate()));
        return status;
    }

    // ── Fetch user email + name from auth-service ──────────────
    private UserInfo fetchUserInfo(int userId) {
        if (userId <= 0) return null;
        try {
            Map<?, ?> user = restTemplate.getForObject(
                authServiceUrl + "/api/auth/user/" + userId, Map.class);
            if (user != null) {
                String email = user.get("email") != null ? user.get("email").toString() : null;
                String name  = user.get("fullName") != null ? user.get("fullName").toString() : null;
                if (email != null) return new UserInfo(email, name);
            }
        } catch (Exception e) {
            System.err.println("[ORDER-SERVICE] Could not fetch user for userId=" + userId + ": " + e.getMessage());
        }
        return null;
    }

    // Simple inner class to hold email + name
    private static class UserInfo {
        final String email;
        final String fullName;
        UserInfo(String email, String fullName) {
            this.email    = email;
            this.fullName = fullName;
        }
    }

    // ── Decrease stock by calling book-service ─────────────────
    private void decreaseStock(int bookId, int quantity) {
        if (bookId <= 0) return;
        try {
            String url = BOOK_SERVICE_URL + "/" + bookId + "/decrease-stock?quantity=" + quantity;
            restTemplate.put(url, null);
            System.out.println("[ORDER-SERVICE] Stock decreased: bookId=" + bookId + " qty=" + quantity);
        } catch (Exception e) {
            System.out.println("[ORDER-SERVICE] Stock decrease failed (non-critical): " + e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────
    private Address saveAddressIfPresent(Cart cart) {
        Address a = cart.getAddress();
        if (a == null) return null;
        a.setCustomerId(cart.getUserId());
        return addressRepository.save(a);
    }

    @Override public void deleteOrder(int orderId) { orderRepository.deleteById(orderId); }
    @Override public List<Order> getOrderByUserId(int userId) { return orderRepository.findByUserId(userId); }
    @Override public void storeAddress(Address address) { addressRepository.save(address); }
    @Override public List<Address> getAddressByCustomerId(int id) { return addressRepository.findByCustomerId(id); }
    @Override public Optional<Order> getOrderById(int orderId) { return orderRepository.findById(orderId); }
}
