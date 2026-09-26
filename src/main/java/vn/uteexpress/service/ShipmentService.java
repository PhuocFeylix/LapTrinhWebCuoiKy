package vn.uteexpress.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.*;
import vn.uteexpress.repository.*;

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

	/**
	 * Tạo Shipment trực tiếp từ Order.
	 *
	 * Lưu ý: Với hệ thống hiện tại, luồng chính nên dùng
	 * createShipmentFromShopOrder().
	 */
	@Transactional
	public Shipment createShipment(Long orderId) {

		Order order = findOrder(orderId);

		if (order.getStatus() == OrderStatus.CANCELLED) {
			throw new IllegalArgumentException("Không thể tạo vận đơn cho đơn hàng đã hủy");
		}

		if (!shipmentRepository.findByOrderId(orderId).isEmpty()) {
			throw new IllegalArgumentException("Đơn hàng đã có vận đơn");
		}

		Shipment shipment = new Shipment();

		shipment.setOrder(order);
		shipment.setTrackingCode(generateTrackingCode());
		shipment.setStatus(ShipmentStatus.READY);

		return shipmentRepository.save(shipment);
	}

	/**
	 * Gán Shipper cho Shipment.
	 */
	@Transactional
	public Shipment assignShipper(Long shipmentId, Long shipperId) {

		Shipment shipment = findShipment(shipmentId);

		User shipper = findShipper(shipperId);

		if (shipment.getStatus() != ShipmentStatus.READY) {
			throw new IllegalArgumentException("Chỉ có Shipment ở trạng thái READY mới được gán Shipper");
		}

		shipment.setShipper(shipper);
		shipment.setStatus(ShipmentStatus.ASSIGNED);
		shipment.setAssignedAt(LocalDateTime.now());

		return shipmentRepository.save(shipment);
	}

	/**
	 * Lấy danh sách User có role SHIPPER.
	 */
	@Transactional(readOnly = true)
	public List<User> getShippers() {

		return userRepository.findByRoleName("SHIPPER");
	}

	/**
	 * Cập nhật trạng thái Shipment.
	 *
	 * Đồng thời cập nhật ShopOrder tương ứng. Sau đó mới tính lại trạng thái Order
	 * tổng.
	 */
	@Transactional
	public Shipment updateStatus(Long shipmentId, ShipmentStatus newStatus) {

		Shipment shipment = findShipment(shipmentId);

		if (newStatus == null) {
			throw new IllegalArgumentException("Trạng thái vận đơn không được null");
		}

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
		 * Nếu Shipment thuộc một ShopOrder, trạng thái ShopOrder phải được cập nhật
		 * trước.
		 */
		ShopOrder shopOrder = shipment.getShopOrder();

		if (shopOrder != null) {
			updateShopOrderStatusFromShipment(shopOrder, newStatus);

			shopOrderRepository.save(shopOrder);

			/*
			 * Sau khi ShopOrder thay đổi, tính lại trạng thái Order tổng.
			 */
			Order order = shopOrder.getOrder();

			if (order != null) {
				updateOrderStatusFromShopOrders(order);
				orderRepository.save(order);
			}
		} else {

			/*
			 * Trường hợp Shipment cũ được tạo trực tiếp từ Order mà chưa liên kết
			 * ShopOrder.
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

	/**
	 * Đồng bộ trạng thái ShopOrder theo Shipment.
	 */
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
			 * READY / ASSIGNED không làm thay đổi trạng thái ShopOrder.
			 */
			break;
		}
	}

	/**
	 * Tính lại trạng thái Order tổng dựa trên toàn bộ ShopOrder.
	 *
	 * Ví dụ:
	 *
	 * Shop A = DELIVERED Shop B = SHIPPING
	 *
	 * => Order = SHIPPING
	 *
	 * Chỉ khi tất cả ShopOrder = DELIVERED thì Order mới = DELIVERED.
	 */
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
		 * Chỉ cần một ShopOrder đang giao thì Order tổng đang SHIPPING.
		 */
		if (hasShipping) {
			order.setStatus(OrderStatus.SHIPPING);
			return;
		}

		/*
		 * Có ShopOrder đã sẵn sàng giao.
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
		 * Mặc định.
		 */
		order.setStatus(OrderStatus.PENDING);
	}

	@Transactional(readOnly = true)
	public Shipment getById(Long id) {

		return findShipment(id);
	}

	/**
	 * Lấy Shipment theo Order.
	 *
	 * Vì một Order có thể có nhiều ShopOrder, nên phương thức này trả về List.
	 */
	@Transactional(readOnly = true)
	public List<Shipment> getByOrderId(Long orderId) {

		List<Shipment> shipments = shipmentRepository.findByOrderId(orderId);

		if (shipments.isEmpty()) {
			throw new RuntimeException("Đơn hàng chưa có vận đơn");
		}

		return shipments;
	}

	@Transactional(readOnly = true)
	public Shipment getByTrackingCode(String code) {

		return shipmentRepository.findByTrackingCode(code)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy vận đơn với mã: " + code));
	}

	@Transactional(readOnly = true)
	public List<Shipment> getShipmentsByShipper(Long shipperId) {

		return shipmentRepository.findByShipperIdOrderByCreatedAtDesc(shipperId);
	}

	@Transactional(readOnly = true)
	public List<Shipment> getUnassignedShipments() {

		return shipmentRepository.findByShipperIsNullOrderByCreatedAtDesc();
	}

	private Shipment findShipment(Long id) {

		return shipmentRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy vận đơn với ID: " + id));
	}

	private Order findOrder(Long id) {

		return orderRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + id));
	}

	private User findShipper(Long id) {

		User shipper = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy shipper"));

		if (shipper.getRole() == null || !"SHIPPER".equalsIgnoreCase(shipper.getRole().getName())) {

			throw new IllegalArgumentException("User được chọn không có role SHIPPER");
		}

		if (!shipper.isEnabled()) {

			throw new IllegalArgumentException("Tài khoản shipper đang bị khóa");
		}

		return shipper;
	}

	private String generateTrackingCode() {

		String code;

		do {

			code = "UTE" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

		} while (shipmentRepository.findByTrackingCode(code).isPresent());

		return code;
	}

	private void validateStatusTransition(ShipmentStatus current, ShipmentStatus next) {

		if (current == ShipmentStatus.CANCELLED || current == ShipmentStatus.DELIVERED) {

			throw new IllegalArgumentException("Vận đơn đã kết thúc, không thể thay đổi trạng thái");
		}

		if (next == current) {

			throw new IllegalArgumentException("Trạng thái mới giống trạng thái hiện tại");
		}

		if (next == ShipmentStatus.ASSIGNED && current != ShipmentStatus.READY) {

			throw new IllegalArgumentException("Chỉ READY mới chuyển sang ASSIGNED");
		}

		if (next == ShipmentStatus.PICKED_UP && current != ShipmentStatus.ASSIGNED) {

			throw new IllegalArgumentException("Chỉ ASSIGNED mới chuyển sang PICKED_UP");
		}

		if (next == ShipmentStatus.DELIVERING && current != ShipmentStatus.PICKED_UP) {

			throw new IllegalArgumentException("Chỉ PICKED_UP mới chuyển sang DELIVERING");
		}

		if (next == ShipmentStatus.DELIVERED && current != ShipmentStatus.DELIVERING) {

			throw new IllegalArgumentException("Chỉ DELIVERING mới chuyển sang DELIVERED");
		}

		if (next == ShipmentStatus.FAILED && current != ShipmentStatus.ASSIGNED && current != ShipmentStatus.PICKED_UP
				&& current != ShipmentStatus.DELIVERING) {

			throw new IllegalArgumentException("Không thể đánh dấu FAILED ở trạng thái hiện tại");
		}
	}

	/**
	 * Tạo Shipment từ ShopOrder.
	 *
	 * Đây là luồng chính nên sử dụng.
	 */
	@Transactional
	public Shipment createShipmentFromShopOrder(Long shopOrderId) {

		ShopOrder shopOrder = shopOrderRepository.findById(shopOrderId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ShopOrder"));

		if (shopOrder.getStatus() != ShopOrderStatus.READY_TO_SHIP) {

			throw new IllegalArgumentException("ShopOrder chưa ở trạng thái READY_TO_SHIP");
		}

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