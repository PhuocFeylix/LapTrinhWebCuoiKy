package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.dto.vendor.VendorOrderResponse;
import vn.uteexpress.entity.User;
import vn.uteexpress.entity.OrderStatus;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.service.VendorOrderService;

@RestController
@RequestMapping("/api/vendor/orders")
public class VendorOrderController {

	private final VendorOrderService vendorOrderService;
	private final UserRepository userRepository;

	public VendorOrderController(VendorOrderService vendorOrderService, UserRepository userRepository) {

		this.vendorOrderService = vendorOrderService;
		this.userRepository = userRepository;
	}

	// =========================
	// GET ORDERS BY VENDOR
	// =========================

	@GetMapping("/vendor/{vendorId}")
	public ResponseEntity<List<VendorOrderResponse>> getOrders(@PathVariable Long vendorId,
			@RequestParam(required = false) OrderStatus status, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		checkVendorAccess(vendorId, currentUser);

		return ResponseEntity.ok(vendorOrderService.getOrdersByVendor(vendorId, status));
	}

	// =========================
	// GET ORDER DETAIL
	// =========================

	@GetMapping("/vendor/{vendorId}/{orderId}")
	public ResponseEntity<VendorOrderResponse> getOrderDetail(@PathVariable Long vendorId, @PathVariable Long orderId,
			Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		checkVendorAccess(vendorId, currentUser);

		return ResponseEntity.ok(vendorOrderService.getOrderDetail(vendorId, orderId));
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
	// CHECK VENDOR ACCESS
	// =========================

	private void checkVendorAccess(Long vendorId, User currentUser) {

		if (vendorId == null) {

			throw new IllegalArgumentException("Vendor ID không được null");
		}

		if (currentUser == null || currentUser.getRole() == null) {

			throw new IllegalArgumentException("Không có quyền truy cập");
		}

		String roleName = currentUser.getRole().getName();

		// ADMIN / MANAGER được xem dữ liệu Vendor
		if ("ADMIN".equalsIgnoreCase(roleName) || "MANAGER".equalsIgnoreCase(roleName)) {

			return;
		}

		// VENDOR chỉ được xem đơn của chính mình
		if ("VENDOR".equalsIgnoreCase(roleName) && currentUser.getId().equals(vendorId)) {

			return;
		}

		throw new IllegalArgumentException("Không được truy cập đơn hàng của Vendor khác");
	}
}