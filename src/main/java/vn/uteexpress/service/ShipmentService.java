package vn.uteexpress.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.Order;
import vn.uteexpress.entity.OrderStatus;
import vn.uteexpress.entity.ShopOrder;
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

		User shipper = findShipper(shipperId);

		if (shipment.getStatus() != ShipmentStatus.READY) {

			throw new IllegalArgumentException("Chỉ Shipment ở trạng thái READY mới được gán Shipper");
		}

		if (shipment.getShipper() != null) {

			throw new IllegalArgumentException("Shipment đã được gán Shipper");
		}

		shipment.setShipper(shipper);
		shipment.setStatus(ShipmentStatus.ASSIGNED);
		shipment.setAssignedAt(LocalDateTime.now());

		return shipmentRepository.save(shipment);
	}

	// =========================================================
	// GET SHIPPERS
	// =========================================================

	@Transactional(readOnly = true)
	public List<User> getShippers() {

		return userRepository.findByRoleName("SHIPPER");
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

		if (newStatus == ShipmentStatus.PICKED_UP) {
			shipment.setPickedUpAt(now);
		}

		if (newStatus == ShipmentStatus.DELIVERED) {
			shipment.setDeliveredAt(now);
		}

		/*
		 * Shipment thuộc ShopOrder.
		 */
		ShopOrder shopOrder = shipment.getShopOrder();

		if (shopOrder != null) {

			updateShopOrderStatusFromShipment(shopOrder, newStatus);

			shopOrderRepository.save(shopOrder);

			Order order = shopOrder.getOrder();

			if (order != null) {

				updateOrderStatusFromShopOrders(order);

				orderRepository.save(order);
			}

		} else {

			/*
			 * Hỗ trợ Shipment cũ được tạo trực tiếp từ Order.
			 */
			Order order = shipment.getOrder();

			if (order != null) {

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

				orderRepository.save(order);
			}
		}

		return shipmentRepository.save(shipment);
	}

	// =========================================================
	// UPDATE SHOP ORDER STATUS
	// =========================================================

	private void updateShopOrderStatusFromShipment(ShopOrder shopOrder, ShipmentStatus shipmentStatus) {

		switch (shipmentStatus) {

		case PICKED_UP:
		case DELIVERING:

			shopOrder.setStatus(ShopOrderStatus.SHIPPING);
			break;

		case DELIVERED:

			shopOrder.setStatus(ShopOrderStatus.DELIVERED);
			break;

		case CANCELLED:

			shopOrder.setStatus(ShopOrderStatus.CANCELLED);
			break;

		default:
			/*
			 * READY / ASSIGNED / FAILED không thay đổi ShopOrder.
			 */
			break;
		}
	}

	// =========================================================
	// UPDATE ORDER STATUS
	// =========================================================

	private void updateOrderStatusFromShopOrders(Order order) {

		List<ShopOrder> shopOrders = order.getShopOrders();

		if (shopOrders == null || shopOrders.isEmpty()) {

			return;
		}

		boolean allDelivered = true;
		boolean allCancelled = true;

		boolean hasShipping = false;
		boolean hasReadyToShip = false;
		boolean hasPreparing = false;
		boolean hasConfirmed = false;

		for (ShopOrder shopOrder : shopOrders) {

			if (shopOrder == null) {
				continue;
			}

			ShopOrderStatus status = shopOrder.getStatus();

			if (status != ShopOrderStatus.DELIVERED) {
				allDelivered = false;
			}

			if (status != ShopOrderStatus.CANCELLED) {
				allCancelled = false;
			}

			if (status == ShopOrderStatus.SHIPPING) {
				hasShipping = true;
			}

			if (status == ShopOrderStatus.READY_TO_SHIP) {
				hasReadyToShip = true;
			}

			if (status == ShopOrderStatus.PREPARING) {
				hasPreparing = true;
			}

			if (status == ShopOrderStatus.CONFIRMED) {
				hasConfirmed = true;
			}
		}

		/*
		 * Tất cả ShopOrder đã giao.
		 */
		if (allDelivered) {

			order.setStatus(OrderStatus.DELIVERED);

			return;
		}

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
		if (hasShipping) {

			order.setStatus(OrderStatus.SHIPPING);

			return;
		}

		/*
		 * Có ShopOrder READY_TO_SHIP.
		 */
		if (hasReadyToShip) {

			order.setStatus(OrderStatus.CONFIRMED);

			return;
		}

		/*
		 * Có ShopOrder đang chuẩn bị.
		 */
		if (hasPreparing) {

			order.setStatus(OrderStatus.CONFIRMED);

			return;
		}

		/*
		 * Có ShopOrder đã xác nhận.
		 */
		if (hasConfirmed) {

			order.setStatus(OrderStatus.CONFIRMED);

			return;
		}

		/*
		 * Trường hợp mặc định.
		 */
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

			throw new RuntimeException("Đơn hàng chưa có vận đơn");
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

		return shipmentRepository.findByTrackingCode(code.trim())
				.orElseThrow(() -> new RuntimeException("Không tìm thấy vận đơn với mã: " + code));
	}

	// =========================================================
	// GET SHIPMENTS BY SHIPPER
	// =========================================================

	@Transactional(readOnly = true)
	public List<Shipment> getShipmentsByShipper(Long shipperId) {

		if (shipperId == null) {
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
				.orElseThrow(() -> new RuntimeException("Không tìm thấy vận đơn với ID: " + id));
	}

	// =========================================================
	// FIND SHIPPER
	// =========================================================

	private User findShipper(Long id) {

		User shipper = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy Shipper"));

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

		/*
		 * DELIVERED, CANCELLED và FAILED đều là trạng thái kết thúc.
		 */
		if (current == ShipmentStatus.DELIVERED || current == ShipmentStatus.CANCELLED
				|| current == ShipmentStatus.FAILED) {

			throw new IllegalArgumentException("Vận đơn đã kết thúc, không thể thay đổi trạng thái");
		}

		if (next == current) {

			throw new IllegalArgumentException("Trạng thái mới giống trạng thái hiện tại");
		}

		/*
		 * READY -> ASSIGNED
		 */
		if (next == ShipmentStatus.ASSIGNED && current != ShipmentStatus.READY) {

			throw new IllegalArgumentException("Chỉ READY mới chuyển sang ASSIGNED");
		}

		/*
		 * ASSIGNED -> PICKED_UP
		 */
		if (next == ShipmentStatus.PICKED_UP && current != ShipmentStatus.ASSIGNED) {

			throw new IllegalArgumentException("Chỉ ASSIGNED mới chuyển sang PICKED_UP");
		}

		/*
		 * PICKED_UP -> DELIVERING
		 */
		if (next == ShipmentStatus.DELIVERING && current != ShipmentStatus.PICKED_UP) {

			throw new IllegalArgumentException("Chỉ PICKED_UP mới chuyển sang DELIVERING");
		}

		/*
		 * DELIVERING -> DELIVERED
		 */
		if (next == ShipmentStatus.DELIVERED && current != ShipmentStatus.DELIVERING) {

			throw new IllegalArgumentException("Chỉ DELIVERING mới chuyển sang DELIVERED");
		}

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

		/*
		 * Chỉ ShopOrder đã sẵn sàng giao mới được tạo Shipment.
		 */
		if (shopOrder.getStatus() != ShopOrderStatus.READY_TO_SHIP) {

			throw new IllegalArgumentException("ShopOrder chưa ở trạng thái READY_TO_SHIP");
		}

		/*
		 * ShopOrder phải thuộc một Order.
		 */
		if (shopOrder.getOrder() == null) {

			throw new IllegalArgumentException("ShopOrder chưa thuộc Order nào");
		}

		/*
		 * Không cho tạo Shipment trùng.
		 */
		Optional<Shipment> existing = shipmentRepository.findByShopOrderId(shopOrderId);

		if (existing.isPresent()) {

			throw new IllegalArgumentException("ShopOrder đã có Shipment");
		}

		Shipment shipment = new Shipment();

		shipment.setOrder(shopOrder.getOrder());

		shipment.setShopOrder(shopOrder);

		shipment.setStatus(ShipmentStatus.READY);

		shipment.setTrackingCode(generateTrackingCode());

		return shipmentRepository.save(shipment);
	}
}