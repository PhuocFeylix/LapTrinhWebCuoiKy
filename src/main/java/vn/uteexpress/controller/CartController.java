package vn.uteexpress.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.Cart;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.service.CartService;

@RestController
@RequestMapping("/api/carts")
public class CartController {

	private final CartService cartService;
	private final UserRepository userRepository;

	public CartController(CartService cartService, UserRepository userRepository) {

		this.cartService = cartService;
		this.userRepository = userRepository;
	}

	private void checkUserAccess(Long userId, Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated()) {

			throw new IllegalArgumentException("Chưa đăng nhập");
		}

		User currentUser = userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

		boolean isAdmin = currentUser.getRole() != null && "ADMIN".equalsIgnoreCase(currentUser.getRole().getName());

		if (!isAdmin && !currentUser.getId().equals(userId)) {

			throw new IllegalArgumentException("Không được truy cập giỏ hàng của người dùng khác");
		}
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<Cart> getCart(@PathVariable Long userId, Authentication authentication) {

		checkUserAccess(userId, authentication);

		return ResponseEntity.ok(cartService.getOrCreateCart(userId));
	}

	@PostMapping("/user/{userId}/items")
	public ResponseEntity<Cart> addItem(@PathVariable Long userId, @RequestParam Long productId,
			@RequestParam int quantity, Authentication authentication) {

		checkUserAccess(userId, authentication);

		return ResponseEntity.ok(cartService.addItem(userId, productId, quantity));
	}

	@PutMapping("/user/{userId}/items/{productId}")
	public ResponseEntity<Cart> updateItem(@PathVariable Long userId, @PathVariable Long productId,
			@RequestParam int quantity, Authentication authentication) {

		checkUserAccess(userId, authentication);

		return ResponseEntity.ok(cartService.updateItem(userId, productId, quantity));
	}

	@DeleteMapping("/user/{userId}/items/{productId}")
	public ResponseEntity<Cart> removeItem(@PathVariable Long userId, @PathVariable Long productId,
			Authentication authentication) {

		checkUserAccess(userId, authentication);

		return ResponseEntity.ok(cartService.removeItem(userId, productId));
	}

	@DeleteMapping("/user/{userId}/clear")
	public ResponseEntity<Cart> clearCart(@PathVariable Long userId, Authentication authentication) {

		checkUserAccess(userId, authentication);

		return ResponseEntity.ok(cartService.clearCart(userId));
	}
}