package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.ShipmentStatus;
import vn.uteexpress.entity.ShopOrder;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.ShopOrderRepository;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.service.ShipmentService;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

	private final ShipmentService shipmentService;
	private final UserRepository userRepository;
	private final ShopOrderRepository shopOrderRepository;

	public ShipmentController(ShipmentService shipmentService, UserRepository userRepository,
			ShopOrderRepository shopOrderRepository) {

		this.shipmentService = shipmentService;
		this.userRepository = userRepository;
		this.shopOrderRepository = shopOrderRepository;
	}

	// =========================================================
	// ASSIGN SHIPPER
	// ADMIN / MANAGER ONLY
	// =========================================================

	@PutMapping("/{shipmentId}/assign/{shipperId}")
	public ResponseEntity<Shipment> assignShipper(@PathVariable Long shipmentId, @PathVariable Long shipperId,
			Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		checkManagerOrAdmin(currentUser);

		return ResponseEntity.ok(shipmentService.assignShipper(shipmentId, shipperId));
	}

	// =========================================================
	// UPDATE SHIPMENT STATUS
	// SHIPPER OWNER / ADMIN
	// =========================================================

	@PutMapping("/{shipmentId}/status")
	public ResponseEntity<Shipment> updateStatus(@PathVariable Long shipmentId, @RequestParam ShipmentStatus status,
			Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		checkCanUpdateShipmentStatus(shipmentId, currentUser);

		return ResponseEntity.ok(shipmentService.updateStatus(shipmentId, status));
	}

	// =========================================================
	// GET SHIPMENT
	// =========================================================

	@GetMapping("/{shipmentId}")
	public ResponseEntity<Shipment> getShipment(@PathVariable Long shipmentId, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		Shipment shipment = shipmentService.getById(shipmentId);

		checkCanViewShipment(shipment, currentUser);

		return ResponseEntity.ok(shipment);
	}

	// =========================================================
	// GET SHIPMENTS BY ORDER
	// =========================================================

	@GetMapping("/order/{orderId}")
	public ResponseEntity<List<Shipment>> getByOrder(@PathVariable Long orderId, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		List<Shipment> shipments = shipmentService.getByOrderId(orderId);

		return ResponseEntity.ok(filterShipmentsForUser(shipments, currentUser));
	}

	// =========================================================
	// PUBLIC TRACKING
	// =========================================================

	@GetMapping("/tracking/{trackingCode}")
	public ResponseEntity<Shipment> getByTrackingCode(@PathVariable String trackingCode) {

		return ResponseEntity.ok(shipmentService.getByTrackingCode(trackingCode));
	}

	// =========================================================
	// GET SHIPMENTS BY SHIPPER
	// =========================================================

	@GetMapping("/shipper/{shipperId}")
	public ResponseEntity<List<Shipment>> getByShipper(@PathVariable Long shipperId, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		checkShipperAccess(shipperId, currentUser);

		return ResponseEntity.ok(shipmentService.getShipmentsByShipper(shipperId));
	}

	// =========================================================
	// GET SHIPPERS
	// ADMIN / MANAGER
	// =========================================================

	@GetMapping("/shippers")
	public ResponseEntity<List<User>> getShippers(Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		checkManagerOrAdmin(currentUser);

		return ResponseEntity.ok(shipmentService.getShippers());
	}

	// =========================================================
	// GET UNASSIGNED
	// ADMIN / MANAGER
	// =========================================================

	@GetMapping("/unassigned")
	public ResponseEntity<List<Shipment>> getUnassigned(Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		checkManagerOrAdmin(currentUser);

		return ResponseEntity.ok(shipmentService.getUnassignedShipments());
	}

	// =========================================================
	// CREATE SHIPMENT
	// VENDOR / MANAGER / ADMIN
	// =========================================================

	@PostMapping("/shop-order/{shopOrderId}")
	public ResponseEntity<Shipment> createFromShopOrder(@PathVariable Long shopOrderId, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		ShopOrder shopOrder = shopOrderRepository.findById(shopOrderId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ShopOrder"));

		checkCanCreateShipment(shopOrder, currentUser);

		Shipment shipment = shipmentService.createShipmentFromShopOrder(shopOrderId);

		return ResponseEntity.status(HttpStatus.CREATED).body(shipment);
	}

	// =========================================================
	// CURRENT USER
	// =========================================================

	private User getCurrentUser(Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated()) {

			throw new IllegalArgumentException("Chưa đăng nhập");
		}

		return userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
	}

	// =========================================================
	// ROLE CHECK
	// =========================================================

	private boolean isAdmin(User user) {

		return user != null && user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getName());
	}

	private boolean isManager(User user) {

		return user != null && user.getRole() != null && "MANAGER".equalsIgnoreCase(user.getRole().getName());
	}

	private boolean isVendor(User user) {

		return user != null && user.getRole() != null && "VENDOR".equalsIgnoreCase(user.getRole().getName());
	}

	private boolean isShipper(User user) {

		return user != null && user.getRole() != null && "SHIPPER".equalsIgnoreCase(user.getRole().getName());
	}

	// =========================================================
	// MANAGER / ADMIN
	// =========================================================

	private void checkManagerOrAdmin(User currentUser) {

		if (!isManager(currentUser) && !isAdmin(currentUser)) {

			throw new IllegalArgumentException("Chỉ Manager hoặc Admin được phép thực hiện thao tác này");
		}
	}

	// =========================================================
	// SHIPPER ACCESS
	// =========================================================

	private void checkShipperAccess(Long shipperId, User currentUser) {

		if (isAdmin(currentUser) || isManager(currentUser)) {

			return;
		}

		if (!isShipper(currentUser) || !currentUser.getId().equals(shipperId)) {

			throw new IllegalArgumentException("Không được xem Shipment của Shipper khác");
		}
	}

	// =========================================================
	// SHIPMENT STATUS ACCESS
	// =========================================================

	private void checkCanUpdateShipmentStatus(Long shipmentId, User currentUser) {

		/*
		 * Admin được xử lý.
		 */
		if (isAdmin(currentUser)) {
			return;
		}

		/*
		 * Manager không trực tiếp đổi trạng thái giao hàng.
		 */
		if (isManager(currentUser)) {

			throw new IllegalArgumentException("Manager không được trực tiếp cập nhật trạng thái Shipment");
		}

		/*
		 * Chỉ Shipper được cập nhật.
		 */
		if (!isShipper(currentUser)) {

			throw new IllegalArgumentException("Chỉ Shipper hoặc Admin được cập nhật trạng thái Shipment");
		}

		Shipment shipment = shipmentService.getById(shipmentId);

		if (shipment.getShipper() == null || !shipment.getShipper().getId().equals(currentUser.getId())) {

			throw new IllegalArgumentException("Shipment không thuộc Shipper hiện tại");
		}
	}

	// =========================================================
	// VIEW SINGLE SHIPMENT
	// =========================================================

	private void checkCanViewShipment(Shipment shipment, User currentUser) {

		if (isAdmin(currentUser) || isManager(currentUser)) {

			return;
		}

		/*
		 * Shipper chỉ xem Shipment của mình.
		 */
		if (isShipper(currentUser)) {

			if (shipment.getShipper() == null || !shipment.getShipper().getId().equals(currentUser.getId())) {

				throw new IllegalArgumentException("Không được xem Shipment của Shipper khác");
			}

			return;
		}

		/*
		 * Vendor chỉ xem Shipment thuộc Shop của mình.
		 */
		if (isVendor(currentUser)) {

			if (!isVendorOwnerOfShipment(shipment, currentUser)) {

				throw new IllegalArgumentException("Không được xem Shipment của Shop khác");
			}

			return;
		}

		throw new IllegalArgumentException("Không có quyền xem Shipment");
	}

	// =========================================================
	// FILTER SHIPMENTS BY ORDER
	// =========================================================

	private List<Shipment> filterShipmentsForUser(List<Shipment> shipments, User currentUser) {

		if (shipments == null || shipments.isEmpty()) {

			return shipments;
		}

		/*
		 * Admin / Manager được xem toàn bộ.
		 */
		if (isAdmin(currentUser) || isManager(currentUser)) {

			return shipments;
		}

		/*
		 * Shipper chỉ nhận Shipment của chính mình.
		 */
		if (isShipper(currentUser)) {

			List<Shipment> result = shipments.stream().filter(shipment -> shipment.getShipper() != null
					&& shipment.getShipper().getId().equals(currentUser.getId())).toList();

			if (result.isEmpty()) {

				throw new IllegalArgumentException("Không được xem Shipment của Order này");
			}

			return result;
		}

		/*
		 * Vendor chỉ nhận Shipment thuộc Shop của chính mình.
		 */
		if (isVendor(currentUser)) {

			List<Shipment> result = shipments.stream()
					.filter(shipment -> isVendorOwnerOfShipment(shipment, currentUser)).toList();

			if (result.isEmpty()) {

				throw new IllegalArgumentException("Không được xem Shipment của Vendor khác");
			}

			return result;
		}

		throw new IllegalArgumentException("Không có quyền xem Shipment");
	}

	// =========================================================
	// CHECK VENDOR OWNERSHIP
	// =========================================================

	private boolean isVendorOwnerOfShipment(Shipment shipment, User currentUser) {

		if (shipment == null || shipment.getShopOrder() == null || shipment.getShopOrder().getShop() == null
				|| shipment.getShopOrder().getShop().getVendor() == null) {

			return false;
		}

		Long vendorId = shipment.getShopOrder().getShop().getVendor().getId();

		return vendorId != null && vendorId.equals(currentUser.getId());
	}

	// =========================================================
	// CREATE SHIPMENT ACCESS
	// =========================================================

	private void checkCanCreateShipment(ShopOrder shopOrder, User currentUser) {

		if (isAdmin(currentUser) || isManager(currentUser)) {

			return;
		}

		if (!isVendor(currentUser)) {

			throw new IllegalArgumentException("Chỉ Vendor, Manager hoặc Admin được tạo Shipment");
		}

		if (shopOrder.getShop() == null || shopOrder.getShop().getVendor() == null) {

			throw new IllegalArgumentException("ShopOrder không có Vendor hợp lệ");
		}

		Long vendorId = shopOrder.getShop().getVendor().getId();

		if (!currentUser.getId().equals(vendorId)) {

			throw new IllegalArgumentException("Không được tạo Shipment cho Shop của Vendor khác");
		}
	}
}