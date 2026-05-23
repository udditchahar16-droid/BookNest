package com.booknest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderResource {

    @Autowired private OrderService orderService;

    @GetMapping
    public List<Order> getAllOrders() { return orderService.getAllOrders(); }

    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUser(@PathVariable int userId) {
        return orderService.getOrderByUserId(userId);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable int orderId) {
        return orderService.getOrderById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/place")
    public ResponseEntity<String> placeOrder(@RequestBody Cart cart) {
        orderService.placeOrder(cart);
        return ResponseEntity.ok("Order placed successfully");
    }

    @PostMapping("/online-payment")
    public ResponseEntity<String> onlinePayment(@RequestBody Cart cart) {
        orderService.onlinePayment(cart);
        return ResponseEntity.ok("Online payment order placed");
    }

    /**
     * PUT /api/orders/{orderId}/status?status=CONFIRMED
     * Only ADMIN role can change order status.
     * Frontend sends X-User-Role header on every request.
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<String> changeStatus(
            @PathVariable int orderId,
            @RequestParam String status,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {

        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Access denied: Only admins can change order status.");
        }
        return ResponseEntity.ok(orderService.changeStatus(status, orderId));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable int orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/address")
    public ResponseEntity<String> storeAddress(@RequestBody Address address) {
        orderService.storeAddress(address);
        return ResponseEntity.ok("Address saved");
    }

    @GetMapping("/address/{customerId}")
    public List<Address> getAddresses(@PathVariable int customerId) {
        return orderService.getAddressByCustomerId(customerId);
    }
}
