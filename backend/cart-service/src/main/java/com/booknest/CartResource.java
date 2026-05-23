package com.booknest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartResource {
    @Autowired private CartService cartService;

    @GetMapping("/{userId}") public ResponseEntity<Cart> get(@PathVariable int userId) { return ResponseEntity.ok(cartService.getCartByUser(userId)); }
    @PostMapping("/{userId}/add") public ResponseEntity<Cart> add(@PathVariable int userId, @RequestBody Map<String,Object> body) {
        return ResponseEntity.ok(cartService.addItem(userId, (int)body.get("bookId"), (int)body.get("quantity")));
    }
    @PutMapping("/{userId}/update/{itemId}") public ResponseEntity<Cart> update(@PathVariable int userId, @PathVariable int itemId, @RequestBody Map<String,Integer> body) {
        return ResponseEntity.ok(cartService.updateQuantity(userId, itemId, body.get("quantity")));
    }
    @DeleteMapping("/{userId}/remove/{itemId}") public ResponseEntity<Void> remove(@PathVariable int userId, @PathVariable int itemId) {
        cartService.removeItem(userId, itemId); return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{userId}/clear") public ResponseEntity<Void> clear(@PathVariable int userId) {
        cartService.clearCart(userId); return ResponseEntity.ok().build();
    }
}
