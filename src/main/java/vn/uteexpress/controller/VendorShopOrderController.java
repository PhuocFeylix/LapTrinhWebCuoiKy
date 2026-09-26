package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.ShopOrder;
import vn.uteexpress.entity.ShopOrderStatus;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.ShopRepository;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.service.VendorShopOrderService;

@RestController
@RequestMapping("/api/vendor/shop-orders")
public class VendorShopOrderController {

	private final VendorShopOrderService service;
	private final UserRepository userRepository;
	private final ShopRepository shopRepository;

	public VendorShopOrderController(VendorShopOrderService service, UserRepository userRepository,
			ShopRepository shopRepository) {

		this.service = service;
		this.userRepository = userRepository;
		this.shopRepository = shopRepository;
	}

	// =========================
	// GET SHOP ORDERS
	// =========================

	@GetMapping("/shop/{shopId}")
	public ResponseEntity<List<ShopOrder>> getOrders(@PathVariable Long shopId,
			@RequestParam(required = false) ShopOrderStatus status, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		checkShopAccess(shopId, currentUser);

		if (status == null) {

			return ResponseEntity.ok(service.getShopOrders(shopId));
		}

		return ResponseEntity.ok(service.getShopOrdersByStatus(shopId, status));
	}

	// =========================
	// GET SHOP ORDER DETAIL
	// =========================

	@GetMapping("/shop/{shopId}/{shopOrderId}")
	public ResponseEntity<ShopOrder> getOrder(@PathVariable Long shopId, @PathVariable Long shopOrderId,
			Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		checkShopAccess(shopId, currentUser);

		return ResponseEntity.ok(service.getShopOrder(shopId, shopOrderId));
	}

	// =========================
	// UPDATE SHOP ORDER STATUS
	// =========================

	@PutMapping("/{shopOrderId}/shop/{shopId}/status")
	public ResponseEntity<ShopOrder> updateStatus(@PathVariable Long shopId, @PathVariable Long shopOrderId,
			@RequestParam ShopOrderStatus status, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		checkShopAccess(shopId, currentUser);

		return ResponseEntity.ok(service.updateStatus(shopId, shopOrderId, status));
	}

	// =========================
	// GET CURRENT USER
	// =========================

	private User getCurrentUser(Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated()) {

			throw new IllegalArgumentException("Chưa đăng nhập");
		}

		return userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
	}

	// =========================
	// CHECK SHOP ACCESS
	// =========================

	private void checkShopAccess(Long shopId, User currentUser) {

		if (shopId == null) {

			throw new IllegalArgumentException("Shop ID không được null");
		}

		if (currentUser == null || currentUser.getRole() == null) {

			throw new IllegalArgumentException("Không có quyền truy cập");
		}

		String roleName = currentUser.getRole().getName();

		// ADMIN / MANAGER được quản lý ShopOrder
		if ("ADMIN".equalsIgnoreCase(roleName) || "MANAGER".equalsIgnoreCase(roleName)) {

			return;
		}

		// Vendor phải sở hữu Shop
		if (!"VENDOR".equalsIgnoreCase(roleName)) {

			throw new IllegalArgumentException("Chỉ Vendor, Manager hoặc Admin được truy cập ShopOrder");
		}

		boolean ownsShop = shopRepository.existsByIdAndVendorId(shopId, currentUser.getId());

		if (!ownsShop) {

			throw new IllegalArgumentException("Không được truy cập Shop của Vendor khác");
		}
	}
}