package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.uteexpress.entity.User;
import vn.uteexpress.entity.ViewedProduct;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.service.ViewedProductService;

@RestController
@RequestMapping("/api/viewed-products")
public class ViewedProductController {

	private final ViewedProductService viewedProductService;
	private final UserRepository userRepository;

	public ViewedProductController(ViewedProductService viewedProductService, UserRepository userRepository) {

		this.viewedProductService = viewedProductService;
		this.userRepository = userRepository;
	}

	// =========================
	// RECORD VIEW
	// =========================

	@PostMapping("/user/{userId}/product/{productId}")
	public ResponseEntity<ViewedProduct> recordView(@PathVariable Long userId, @PathVariable Long productId,
			Authentication authentication) {

		checkUserAccess(userId, authentication);

		return ResponseEntity.ok(viewedProductService.recordView(userId, productId));
	}

	// =========================
	// GET HISTORY
	// =========================

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<ViewedProduct>> getViewedProducts(@PathVariable Long userId,
			Authentication authentication) {

		checkUserAccess(userId, authentication);

		return ResponseEntity.ok(viewedProductService.getViewedProducts(userId));
	}

	// =========================
	// REMOVE ONE
	// =========================

	@DeleteMapping("/user/{userId}/product/{productId}")
	public ResponseEntity<Void> removeViewedProduct(@PathVariable Long userId, @PathVariable Long productId,
			Authentication authentication) {

		checkUserAccess(userId, authentication);

		viewedProductService.removeViewedProduct(userId, productId);

		return ResponseEntity.noContent().build();
	}

	// =========================
	// CLEAR HISTORY
	// =========================

	@DeleteMapping("/user/{userId}")
	public ResponseEntity<Void> clearHistory(@PathVariable Long userId, Authentication authentication) {

		checkUserAccess(userId, authentication);

		viewedProductService.clearHistory(userId);

		return ResponseEntity.noContent().build();
	}

	// =========================
	// CHECK USER ACCESS
	// =========================

	private void checkUserAccess(Long userId, Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated()) {

			throw new IllegalArgumentException("Chưa đăng nhập");
		}

		User currentUser = userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

		boolean isAdmin = currentUser.getRole() != null && "ADMIN".equalsIgnoreCase(currentUser.getRole().getName());

		if (!isAdmin && !currentUser.getId().equals(userId)) {

			throw new IllegalArgumentException("Không được truy cập sản phẩm đã xem của người dùng khác");
		}
	}
}