package vn.uteexpress.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.Cart;
import vn.uteexpress.service.CartService;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/user/{userId}/items")
    public ResponseEntity<Cart> addItem(
            @PathVariable Long userId,
            @RequestParam Long productId,
            @RequestParam int quantity) {

        return ResponseEntity.ok(
                cartService.addItem(userId, productId, quantity));
    }

    @PutMapping("/user/{userId}/items/{productId}")
    public ResponseEntity<Cart> updateItem(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestParam int quantity) {

        return ResponseEntity.ok(
                cartService.updateItem(userId, productId, quantity));
    }

    @DeleteMapping("/user/{userId}/items/{productId}")
    public ResponseEntity<Cart> removeItem(
            @PathVariable Long userId,
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                cartService.removeItem(userId, productId));
    }

    @DeleteMapping("/user/{userId}/clear")
    public ResponseEntity<Cart> clearCart(@PathVariable Long userId) {
        return ResponseEntity.ok(
                cartService.clearCart(userId));
    }
}
