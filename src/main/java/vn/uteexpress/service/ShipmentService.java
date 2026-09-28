package vn.uteexpress.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.Order;
import vn.uteexpress.entity.OrderStatus;
<<<<<<< HEAD
import vn.uteexpress.entity.ShopOrder;
=======
import vn.uteexpress.entity.Product;
import vn.uteexpress.entity.ShopOrder;
import vn.uteexpress.entity.ShopOrderItem;
>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
import vn.uteexpress.entity.ShopOrderStatus;
import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.ShipmentStatus;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.OrderRepository;
import vn.uteexpress.repository.ShipmentRepository;
import vn.uteexpress.repository.ShopOrderRepository;
import vn.uteexpress.repository.UserRepository;

@Service
public class ShipmentService {

	private final ShipmentRepository shipmentRepository;
	private final OrderRepository orderRepository;
	private final UserRepository userRepository;
	private final ShopOrderRepository shopOrderRepository;

	public ShipmentService(ShipmentRepository shipmentRepository, OrderRepository orderRepository,
			UserRepository userRepository, ShopOrderRepository shopOrderRepository) {

		this.shipmentRepository = shipmentRepository;
		this.orderRepository = orderRepository;
		this.userRepository = userRepository;
		this.shopOrderRepository = shopOrderRepository;
	}

	// =========================================================
	// ASSIGN SHIPPER
	// =========================================================

	@Transactional
	public Shipment assignShipper(Long shipmentId, Long shipperId) {

		if (shipmentId == null) {
			throw new IllegalArgumentException("Shipment ID không được null");
		}

		if (shipperId == null) {
			throw new IllegalArgumentException("Shipper ID không được null");
		}

		Shipment shipment = findShipment(shipmentId);
<<<<<<< HEAD

		User shipper = findShipper(shipperId);

		if (shipment.getStatus() != ShipmentStatus.READY) {

=======
		User shipper = findShipper(shipperId);

		if (shipment.getStatus() != ShipmentStatus.READY) {
>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
			throw new IllegalArgumentException("Chỉ Shipment ở trạng thái READY mới được gán Shipper");
		}

		if (shipment.getShipper() != null) {
<<<<<<< HEAD

=======
>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
			throw new IllegalArgumentException("Shipment đã được gán Shipper");
		}

		shipment.setShipper(shipper);
		shipment.setStatus(ShipmentStatus.ASSIGNED);
		shipment.setAssignedAt(LocalDateTime.now());

		return shipmentRepository.save(shipment);
	}

	// =========================================================
<<<<<<< HEAD
=======
	// RETRY FAILED SHIPMENT
	// =========================================================

	/**
	 * Đưa Shipment FAILED về READY để Manager/Admin phân công lại.
	 *
	 * FAILED không đồng nghĩa với CANCELLED. ShopOrder vẫn giữ SHIPPING vì đơn vẫn
	 * đang trong quá trình xử lý giao hàng.
	 */
	@Transactional
	public Shipment retryFailedShipment(Long shipmentId) {

		if (shipmentId == null) {
			throw new IllegalArgumentException("Shipment ID không được null");
		}

		Shipment shipment = findShipment(shipmentId);

		if (shipment.getStatus() != ShipmentStatus.FAILED) {
			throw new IllegalArgumentException("Chỉ Shipment FAILED mới được giao lại");
		}

		shipment.setStatus(ShipmentStatus.READY);

		/*
		 * Xóa Shipper cũ để Manager/Admin có thể phân công Shipper mới.
		 */
		shipment.setShipper(null);
		shipment.setAssignedAt(null);
		shipment.setPickedUpAt(null);
		shipment.setDeliveredAt(null);

		/*
		 * Không thay đổi ShopOrder.
		 *
		 * ShopOrder vẫn là SHIPPING vì đơn chưa giao thành công.
		 */

		return shipmentRepository.save(shipment);
	}

	// =========================================================
>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
	// GET SHIPPERS
	// =========================================================

	@Transactional(readOnly = true)
	public List<User> getShippers() {

<<<<<<< HEAD
		return userRepository.findByRoleName("SHIPPER");
=======
		return userRepository.findByRoleName("SHIPPER").stream().filter(User::isEnabled).toList();
>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
	}

	// =========================================================
	// UPDATE SHIPMENT STATUS
	// =========================================================

	@Transactional
	public Shipment updateStatus(Long shipmentId, ShipmentStatus newStatus) {

		if (shipmentId == null) {
			throw new IllegalArgumentException("Shipment ID không được null");
		}

		if (newStatus == null) {
			throw new IllegalArgumentException("Trạng thái vận đơn không được null");
		}

		Shipment shipment = findShipment(shipmentId);

		ShipmentStatus currentStatus = shipment.getStatus();

		validateStatusTransition(currentStatus, newStatus);

		shipment.setStatus(newStatus);

		LocalDateTime now = LocalDateTime.now();

<<<<<<< HEAD
=======
		// -----------------------------------------------------
		// PICKED_UP
		// -----------------------------------------------------

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
		if (newStatus == ShipmentStatus.PICKED_UP) {
			shipment.setPickedUpAt(now);
		}

<<<<<<< HEAD
=======
		// -----------------------------------------------------
		// DELIVERED
		// -----------------------------------------------------

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
		if (newStatus == ShipmentStatus.DELIVERED) {
			shipment.setDeliveredAt(now);
		}

<<<<<<< HEAD
		/*
		 * Shipment thuộc ShopOrder.
		 */
=======
		// -----------------------------------------------------
		// FAILED
		// -----------------------------------------------------

		if (newStatus == ShipmentStatus.FAILED) {

			/*
			 * Không xóa Shipper.
			 *
			 * Giữ lại Shipper cũ để biết ai đã xử lý vận đơn bị giao thất bại.
			 */
			if (shipment.getNote() == null || shipment.getNote().isBlank()) {

				shipment.setNote("Giao hàng thất bại. Chờ Manager/Admin xử lý giao lại.");
			}
		}

		// -----------------------------------------------------
		// SHOP ORDER
		// -----------------------------------------------------

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
		ShopOrder shopOrder = shipment.getShopOrder();

		if (shopOrder != null) {

			updateShopOrderStatusFromShipment(shopOrder, newStatus);

			shopOrderRepository.save(shopOrder);

<<<<<<< HEAD
=======
			// -------------------------------------------------
			// ORDER CHA
			// -------------------------------------------------

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
			Order order = shopOrder.getOrder();

			if (order != null) {

				updateOrderStatusFromShopOrders(order);

				orderRepository.save(order);
			}

		} else {

			/*
<<<<<<< HEAD
			 * Hỗ trợ Shipment cũ được tạo trực tiếp từ Order.
=======
			 * Hỗ trợ dữ liệu Shipment cũ chỉ liên kết với Order.
>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
			 */
			Order order = shipment.getOrder();

			if (order != null) {

<<<<<<< HEAD
				switch (newStatus) {

				case PICKED_UP:
				case DELIVERING:

					order.setStatus(OrderStatus.SHIPPING);
					break;

				case DELIVERED:

					order.setStatus(OrderStatus.DELIVERED);
					break;

				case CANCELLED:

					order.setStatus(OrderStatus.CANCELLED);
					break;

				default:
					break;
				}
=======
				updateLegacyOrderStatus(order, newStatus);
>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)

				orderRepository.save(order);
			}
		}

		return shipmentRepository.save(shipment);
	}

	// =========================================================
<<<<<<< HEAD
	// UPDATE SHOP ORDER STATUS
=======
	// UPDATE SHOP ORDER FROM SHIPMENT
>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
	// =========================================================

	private void updateShopOrderStatusFromShipment(ShopOrder shopOrder, ShipmentStatus shipmentStatus) {

		switch (shipmentStatus) {

		case PICKED_UP:
		case DELIVERING:

			shopOrder.setStatus(ShopOrderStatus.SHIPPING);
<<<<<<< HEAD
=======

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
			break;

		case DELIVERED:

			shopOrder.setStatus(ShopOrderStatus.DELIVERED);
<<<<<<< HEAD
=======

			break;

		case FAILED:

			/*
			 * Giao thất bại chưa có nghĩa đơn bị hủy.
			 *
			 * ShopOrder vẫn SHIPPING để có thể giao lại.
			 */
			shopOrder.setStatus(ShopOrderStatus.SHIPPING);

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
			break;

		case CANCELLED:

<<<<<<< HEAD
			shopOrder.setStatus(ShopOrderStatus.CANCELLED);
			break;

		default:
			/*
			 * READY / ASSIGNED / FAILED không thay đổi ShopOrder.
			 */
=======
			/*
			 * CANCELLED chỉ được phép trước khi Shipper lấy hàng.
			 */
			shopOrder.setStatus(ShopOrderStatus.CANCELLED);

			break;

		case READY:
		case ASSIGNED:

			/*
			 * Chưa bắt đầu giao.
			 */
			break;

		default:

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
			break;
		}
	}

	// =========================================================
<<<<<<< HEAD
	// UPDATE ORDER STATUS
=======
	// LEGACY ORDER STATUS
	// =========================================================

	private void updateLegacyOrderStatus(Order order, ShipmentStatus shipmentStatus) {

		switch (shipmentStatus) {

		case PICKED_UP:
		case DELIVERING:
		case FAILED:

			/*
			 * FAILED vẫn đang trong quá trình xử lý giao hàng.
			 */
			order.setStatus(OrderStatus.SHIPPING);

			break;

		case DELIVERED:

			order.setStatus(OrderStatus.DELIVERED);

			break;

		case CANCELLED:

			order.setStatus(OrderStatus.CANCELLED);

			break;

		default:

			break;
		}
	}

	// =========================================================
	// UPDATE PARENT ORDER
>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
	// =========================================================

	private void updateOrderStatusFromShopOrders(Order order) {

		List<ShopOrder> shopOrders = order.getShopOrders();

		if (shopOrders == null || shopOrders.isEmpty()) {
<<<<<<< HEAD

			return;
		}

=======
			return;
		}

		boolean hasShopOrder = false;

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
		boolean allDelivered = true;
		boolean allCancelled = true;

		boolean hasShipping = false;
		boolean hasReadyToShip = false;
		boolean hasPreparing = false;
		boolean hasConfirmed = false;
<<<<<<< HEAD
=======
		boolean hasPending = false;

		boolean allFinished = true;
>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)

		for (ShopOrder shopOrder : shopOrders) {

			if (shopOrder == null) {
				continue;
			}

<<<<<<< HEAD
			ShopOrderStatus status = shopOrder.getStatus();

=======
			hasShopOrder = true;

			ShopOrderStatus status = shopOrder.getStatus();

			// ---------------------------------------------
			// DELIVERED
			// ---------------------------------------------

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
			if (status != ShopOrderStatus.DELIVERED) {
				allDelivered = false;
			}

<<<<<<< HEAD
=======
			// ---------------------------------------------
			// CANCELLED
			// ---------------------------------------------

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
			if (status != ShopOrderStatus.CANCELLED) {
				allCancelled = false;
			}

<<<<<<< HEAD
=======
			// ---------------------------------------------
			// SHIPPING
			// ---------------------------------------------

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
			if (status == ShopOrderStatus.SHIPPING) {
				hasShipping = true;
			}

<<<<<<< HEAD
=======
			// ---------------------------------------------
			// READY TO SHIP
			// ---------------------------------------------

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
			if (status == ShopOrderStatus.READY_TO_SHIP) {
				hasReadyToShip = true;
			}

<<<<<<< HEAD
=======
			// ---------------------------------------------
			// PREPARING
			// ---------------------------------------------

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
			if (status == ShopOrderStatus.PREPARING) {
				hasPreparing = true;
			}

<<<<<<< HEAD
			if (status == ShopOrderStatus.CONFIRMED) {
				hasConfirmed = true;
			}
		}

		/*
		 * Tất cả ShopOrder đã giao.
		 */
		if (allDelivered) {

			order.setStatus(OrderStatus.DELIVERED);
=======
			// ---------------------------------------------
			// CONFIRMED
			// ---------------------------------------------

			if (status == ShopOrderStatus.CONFIRMED) {
				hasConfirmed = true;
			}

			// ---------------------------------------------
			// PENDING
			// ---------------------------------------------

			if (status == ShopOrderStatus.PENDING) {
				hasPending = true;
			}

			// ---------------------------------------------
			// TERMINAL CHECK
			// ---------------------------------------------

			if (status != ShopOrderStatus.DELIVERED && status != ShopOrderStatus.CANCELLED) {

				allFinished = false;
			}
		}

		if (!hasShopOrder) {
			return;
		}

		// =====================================================
		// TẤT CẢ SHOP ORDER ĐÃ HOÀN TẤT
		//
		// Có thể là:
		// DELIVERED + DELIVERED
		// DELIVERED + CANCELLED
		// CANCELLED + CANCELLED
		// =====================================================

		if (allFinished) {

			if (allDelivered) {

				order.setStatus(OrderStatus.DELIVERED);

			} else if (allCancelled) {

				order.setStatus(OrderStatus.CANCELLED);

			} else {

				/*
				 * Một phần giao thành công, một phần bị hủy.
				 *
				 * Order đã hoàn tất xử lý.
				 */
				order.setStatus(OrderStatus.DELIVERED);
			}
>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)

			return;
		}

<<<<<<< HEAD
		/*
		 * Tất cả ShopOrder đã hủy.
		 */
		if (allCancelled) {

			order.setStatus(OrderStatus.CANCELLED);

			return;
		}

		/*
		 * Có ít nhất một ShopOrder đang giao.
		 */
=======
		// =====================================================
		// CÓ SHOP ORDER ĐANG GIAO
		// =====================================================

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
		if (hasShipping) {

			order.setStatus(OrderStatus.SHIPPING);

			return;
		}

<<<<<<< HEAD
		/*
		 * Có ShopOrder READY_TO_SHIP.
		 */
=======
		// =====================================================
		// CÓ SHOP ORDER READY TO SHIP
		// =====================================================

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
		if (hasReadyToShip) {

			order.setStatus(OrderStatus.CONFIRMED);

			return;
		}

<<<<<<< HEAD
		/*
		 * Có ShopOrder đang chuẩn bị.
		 */
=======
		// =====================================================
		// CÓ SHOP ORDER ĐANG CHUẨN BỊ
		// =====================================================

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
		if (hasPreparing) {

			order.setStatus(OrderStatus.CONFIRMED);

			return;
		}

<<<<<<< HEAD
		/*
		 * Có ShopOrder đã xác nhận.
		 */
=======
		// =====================================================
		// CÓ SHOP ORDER ĐÃ XÁC NHẬN
		// =====================================================

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
		if (hasConfirmed) {

			order.setStatus(OrderStatus.CONFIRMED);

			return;
		}

<<<<<<< HEAD
		/*
		 * Trường hợp mặc định.
		 */
=======
		// =====================================================
		// CÓ SHOP ORDER PENDING
		// =====================================================

		if (hasPending) {

			order.setStatus(OrderStatus.PENDING);

			return;
		}

		// =====================================================
		// DEFAULT
		// =====================================================

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
		order.setStatus(OrderStatus.PENDING);
	}

	// =========================================================
	// GET SHIPMENT BY ID
	// =========================================================

	@Transactional(readOnly = true)
	public Shipment getById(Long id) {

		if (id == null) {
			throw new IllegalArgumentException("Shipment ID không được null");
		}

		return findShipment(id);
	}

	// =========================================================
	// GET SHIPMENTS BY ORDER
	// =========================================================

	@Transactional(readOnly = true)
	public List<Shipment> getByOrderId(Long orderId) {

		if (orderId == null) {
			throw new IllegalArgumentException("Order ID không được null");
		}

		List<Shipment> shipments = shipmentRepository.findByOrderId(orderId);

		if (shipments.isEmpty()) {

<<<<<<< HEAD
			throw new RuntimeException("Đơn hàng chưa có vận đơn");
=======
			throw new IllegalArgumentException("Đơn hàng chưa có vận đơn");
>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
		}

		return shipments;
	}

	// =========================================================
	// GET BY TRACKING CODE
	// =========================================================

	@Transactional(readOnly = true)
	public Shipment getByTrackingCode(String code) {

		if (code == null || code.trim().isEmpty()) {

			throw new IllegalArgumentException("Tracking code không được để trống");
		}

<<<<<<< HEAD
		return shipmentRepository.findByTrackingCode(code.trim())
				.orElseThrow(() -> new RuntimeException("Không tìm thấy vận đơn với mã: " + code));
=======
		String trackingCode = code.trim().toUpperCase();

		return shipmentRepository.findByTrackingCode(trackingCode)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vận đơn với mã: " + trackingCode));
>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
	}

	// =========================================================
	// GET SHIPMENTS BY SHIPPER
	// =========================================================

	@Transactional(readOnly = true)
	public List<Shipment> getShipmentsByShipper(Long shipperId) {

		if (shipperId == null) {
<<<<<<< HEAD
=======

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
			throw new IllegalArgumentException("Shipper ID không được null");
		}

		return shipmentRepository.findByShipperIdOrderByCreatedAtDesc(shipperId);
	}

	// =========================================================
	// GET UNASSIGNED SHIPMENTS
	// =========================================================

	@Transactional(readOnly = true)
	public List<Shipment> getUnassignedShipments() {

		return shipmentRepository.findByShipperIsNullOrderByCreatedAtDesc();
	}

	// =========================================================
	// FIND SHIPMENT
	// =========================================================

	private Shipment findShipment(Long id) {

		return shipmentRepository.findById(id)
<<<<<<< HEAD
				.orElseThrow(() -> new RuntimeException("Không tìm thấy vận đơn với ID: " + id));
=======
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vận đơn với ID: " + id));
>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
	}

	// =========================================================
	// FIND SHIPPER
	// =========================================================

	private User findShipper(Long id) {

<<<<<<< HEAD
		User shipper = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy Shipper"));
=======
		User shipper = userRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Shipper"));
>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)

		if (shipper.getRole() == null || !"SHIPPER".equalsIgnoreCase(shipper.getRole().getName())) {

			throw new IllegalArgumentException("User được chọn không có role SHIPPER");
		}

		if (!shipper.isEnabled()) {

			throw new IllegalArgumentException("Tài khoản Shipper đang bị khóa");
		}

		return shipper;
	}

	// =========================================================
	// GENERATE TRACKING CODE
	// =========================================================

	private String generateTrackingCode() {

		String code;

		do {

			code = "UTE" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

		} while (shipmentRepository.findByTrackingCode(code).isPresent());

		return code;
	}

	// =========================================================
	// VALIDATE STATUS TRANSITION
	// =========================================================

	private void validateStatusTransition(ShipmentStatus current, ShipmentStatus next) {

		if (current == null) {

			throw new IllegalArgumentException("Shipment hiện tại chưa có trạng thái");
		}

		if (next == null) {

			throw new IllegalArgumentException("Trạng thái mới không được null");
		}

<<<<<<< HEAD
		/*
		 * DELIVERED, CANCELLED và FAILED đều là trạng thái kết thúc.
		 */
		if (current == ShipmentStatus.DELIVERED || current == ShipmentStatus.CANCELLED
				|| current == ShipmentStatus.FAILED) {

			throw new IllegalArgumentException("Vận đơn đã kết thúc, không thể thay đổi trạng thái");
		}

=======
>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
		if (next == current) {

			throw new IllegalArgumentException("Trạng thái mới giống trạng thái hiện tại");
		}

<<<<<<< HEAD
		/*
		 * READY -> ASSIGNED
		 */
=======
		// =====================================================
		// TERMINAL
		// =====================================================

		if (current == ShipmentStatus.DELIVERED || current == ShipmentStatus.CANCELLED) {

			throw new IllegalArgumentException("Vận đơn đã kết thúc, không thể thay đổi trạng thái");
		}

		// =====================================================
		// FAILED
		//
		// FAILED KHÔNG được đổi trực tiếp bằng updateStatus.
		// Muốn giao lại phải dùng retryFailedShipment().
		// =====================================================

		if (current == ShipmentStatus.FAILED) {

			throw new IllegalArgumentException("Shipment FAILED phải được Manager/Admin xử lý giao lại");
		}

		// =====================================================
		// READY -> ASSIGNED
		// =====================================================

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
		if (next == ShipmentStatus.ASSIGNED && current != ShipmentStatus.READY) {

			throw new IllegalArgumentException("Chỉ READY mới chuyển sang ASSIGNED");
		}

<<<<<<< HEAD
		/*
		 * ASSIGNED -> PICKED_UP
		 */
=======
		// =====================================================
		// ASSIGNED -> PICKED_UP
		// =====================================================

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
		if (next == ShipmentStatus.PICKED_UP && current != ShipmentStatus.ASSIGNED) {

			throw new IllegalArgumentException("Chỉ ASSIGNED mới chuyển sang PICKED_UP");
		}

<<<<<<< HEAD
		/*
		 * PICKED_UP -> DELIVERING
		 */
=======
		// =====================================================
		// PICKED_UP -> DELIVERING
		// =====================================================

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
		if (next == ShipmentStatus.DELIVERING && current != ShipmentStatus.PICKED_UP) {

			throw new IllegalArgumentException("Chỉ PICKED_UP mới chuyển sang DELIVERING");
		}

<<<<<<< HEAD
		/*
		 * DELIVERING -> DELIVERED
		 */
=======
		// =====================================================
		// DELIVERING -> DELIVERED
		// =====================================================

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
		if (next == ShipmentStatus.DELIVERED && current != ShipmentStatus.DELIVERING) {

			throw new IllegalArgumentException("Chỉ DELIVERING mới chuyển sang DELIVERED");
		}

<<<<<<< HEAD
		/*
		 * ASSIGNED / PICKED_UP / DELIVERING có thể chuyển FAILED.
		 */
		if (next == ShipmentStatus.FAILED && current != ShipmentStatus.ASSIGNED && current != ShipmentStatus.PICKED_UP
				&& current != ShipmentStatus.DELIVERING) {

			throw new IllegalArgumentException("Không thể đánh dấu FAILED ở trạng thái hiện tại");
		}

		/*
		 * Cho phép hủy Shipment trước khi hoàn tất.
		 */
		if (next == ShipmentStatus.CANCELLED) {

			if (current != ShipmentStatus.READY && current != ShipmentStatus.ASSIGNED
					&& current != ShipmentStatus.PICKED_UP && current != ShipmentStatus.DELIVERING) {

				throw new IllegalArgumentException("Không thể hủy Shipment ở trạng thái hiện tại");
			}
=======
		// =====================================================
		// FAILED
		//
		// Có thể thất bại trong 3 trạng thái:
		// ASSIGNED
		// PICKED_UP
		// DELIVERING
		// =====================================================

		if (next == ShipmentStatus.FAILED) {

			if (current != ShipmentStatus.ASSIGNED && current != ShipmentStatus.PICKED_UP
					&& current != ShipmentStatus.DELIVERING) {

				throw new IllegalArgumentException("Không thể đánh dấu FAILED ở trạng thái hiện tại");
			}

			return;
		}

		// =====================================================
		// CANCELLED
		//
		// Chỉ được hủy trước khi Shipper lấy hàng.
		//
		// READY -> CANCELLED
		// ASSIGNED -> CANCELLED
		//
		// PICKED_UP / DELIVERING không được hủy ở đây.
		// =====================================================

		if (next == ShipmentStatus.CANCELLED) {

			if (current != ShipmentStatus.READY && current != ShipmentStatus.ASSIGNED) {

				throw new IllegalArgumentException("Chỉ có thể hủy Shipment ở trạng thái READY hoặc ASSIGNED");
			}

			return;
>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
		}
	}

	// =========================================================
	// CREATE SHIPMENT FROM SHOP ORDER
	// =========================================================

	@Transactional
	public Shipment createShipmentFromShopOrder(Long shopOrderId) {

		if (shopOrderId == null) {

			throw new IllegalArgumentException("ShopOrder ID không được null");
		}

		ShopOrder shopOrder = shopOrderRepository.findById(shopOrderId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ShopOrder"));

<<<<<<< HEAD
		/*
		 * Chỉ ShopOrder đã sẵn sàng giao mới được tạo Shipment.
		 */
=======
		// =====================================================
		// SHOP ORDER PHẢI READY
		// =====================================================

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
		if (shopOrder.getStatus() != ShopOrderStatus.READY_TO_SHIP) {

			throw new IllegalArgumentException("ShopOrder chưa ở trạng thái READY_TO_SHIP");
		}

<<<<<<< HEAD
		/*
		 * ShopOrder phải thuộc một Order.
		 */
=======
		// =====================================================
		// PHẢI CÓ ORDER
		// =====================================================

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
		if (shopOrder.getOrder() == null) {

			throw new IllegalArgumentException("ShopOrder chưa thuộc Order nào");
		}

<<<<<<< HEAD
		/*
		 * Không cho tạo Shipment trùng.
		 */
		Optional<Shipment> existing = shipmentRepository.findByShopOrderId(shopOrderId);
=======
		// =====================================================
		// KHÔNG TẠO TRÙNG SHIPMENT
		// =====================================================

		Optional<Shipment> existing = shipmentRepository.findByShopOrder_Id(shopOrderId);
>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)

		if (existing.isPresent()) {

			throw new IllegalArgumentException("ShopOrder đã có Shipment");
		}

<<<<<<< HEAD
=======
		// =====================================================
		// CREATE
		// =====================================================

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
		Shipment shipment = new Shipment();

		shipment.setOrder(shopOrder.getOrder());

		shipment.setShopOrder(shopOrder);

		shipment.setStatus(ShipmentStatus.READY);

		shipment.setTrackingCode(generateTrackingCode());

		return shipmentRepository.save(shipment);
	}
}