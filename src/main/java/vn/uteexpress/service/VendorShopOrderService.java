package vn.uteexpress.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.Order;
import vn.uteexpress.entity.OrderStatus;
import vn.uteexpress.entity.Product;
import vn.uteexpress.entity.ShopOrder;
import vn.uteexpress.entity.ShopOrderItem;
import vn.uteexpress.entity.ShopOrderStatus;
import vn.uteexpress.repository.OrderRepository;
import vn.uteexpress.repository.ShopOrderRepository;

@Service
public class VendorShopOrderService {

	private final ShopOrderRepository shopOrderRepository;
	private final OrderRepository orderRepository;

	public VendorShopOrderService(ShopOrderRepository shopOrderRepository, OrderRepository orderRepository) {

		this.shopOrderRepository = shopOrderRepository;
		this.orderRepository = orderRepository;
	}

	// =========================================================
	// GET ALL SHOP ORDERS
	// =========================================================

	@Transactional(readOnly = true)
	public List<ShopOrder> getShopOrders(Long shopId) {

		if (shopId == null) {
			throw new IllegalArgumentException("Shop ID không được null");
		}

		return shopOrderRepository.findByShopIdOrderByCreatedAtDesc(shopId);
	}

	// =========================================================
	// GET SHOP ORDERS BY STATUS
	// =========================================================

	@Transactional(readOnly = true)
	public List<ShopOrder> getShopOrdersByStatus(Long shopId, ShopOrderStatus status) {

		if (shopId == null) {
			throw new IllegalArgumentException("Shop ID không được null");
		}

		if (status == null) {
			throw new IllegalArgumentException("Trạng thái không được null");
		}

		return shopOrderRepository.findByShopIdAndStatusOrderByCreatedAtDesc(shopId, status);
	}

	// =========================================================
	// GET SHOP ORDER DETAIL
	// =========================================================

	@Transactional(readOnly = true)
	public ShopOrder getShopOrder(Long shopId, Long shopOrderId) {

		if (shopId == null) {
			throw new IllegalArgumentException("Shop ID không được null");
		}

		if (shopOrderId == null) {
			throw new IllegalArgumentException("ShopOrder ID không được null");
		}

		return shopOrderRepository.findByIdAndShopId(shopOrderId, shopId)
				.orElseThrow(() -> new IllegalArgumentException("ShopOrder không thuộc Shop này"));
	}

	// =========================================================
	// UPDATE STATUS
	// =========================================================

	@Transactional
	public ShopOrder updateStatus(Long shopId, Long shopOrderId, ShopOrderStatus newStatus) {

		ShopOrder shopOrder = getShopOrder(shopId, shopOrderId);

		ShopOrderStatus currentStatus = shopOrder.getStatus();

		validateStatusTransition(currentStatus, newStatus);

		/*
		 * Chỉ xử lý hoàn stock khi Vendor chuyển ShopOrder sang CANCELLED.
		 */
		if (newStatus == ShopOrderStatus.CANCELLED) {

			restoreStock(shopOrder);
		}

		shopOrder.setStatus(newStatus);

		ShopOrder savedShopOrder = shopOrderRepository.save(shopOrder);

		/*
		 * Đồng bộ trạng thái Order cha.
		 */
		Order order = shopOrder.getOrder();

		if (order != null) {

			updateParentOrderStatus(order);
		}

		return savedShopOrder;
	}

	// =========================================================
	// RESTORE STOCK
	// =========================================================

	private void restoreStock(ShopOrder shopOrder) {

		if (shopOrder.getItems() == null) {
			return;
		}

		for (ShopOrderItem item : shopOrder.getItems()) {

			if (item == null) {
				continue;
			}

			Product product = item.getProduct();

			if (product == null) {
				continue;
			}

			int quantity = item.getQuantity();

			if (quantity <= 0) {
				continue;
			}

			product.setStock(product.getStock() + quantity);
		}
	}

	// =========================================================
	// UPDATE PARENT ORDER STATUS
	// =========================================================

	private void updateParentOrderStatus(Order order) {

		if (order.getShopOrders() == null || order.getShopOrders().isEmpty()) {

			return;
		}

		boolean allDelivered = true;
		boolean allCancelled = true;

		boolean hasShipping = false;
		boolean hasReadyToShip = false;
		boolean hasPreparing = false;
		boolean hasConfirmed = false;
		boolean hasPending = false;

		for (ShopOrder shopOrder : order.getShopOrders()) {

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

			if (status == ShopOrderStatus.PENDING) {
				hasPending = true;
			}
		}

		// =========================================
		// TẤT CẢ ĐÃ GIAO
		// =========================================

		if (allDelivered) {

			order.setStatus(OrderStatus.DELIVERED);

			orderRepository.save(order);

			return;
		}

		// =========================================
		// TẤT CẢ ĐÃ HỦY
		// =========================================

		if (allCancelled) {

			order.setStatus(OrderStatus.CANCELLED);

			orderRepository.save(order);

			return;
		}

		// =========================================
		// CÓ SHOP ĐANG GIAO
		// =========================================

		if (hasShipping) {

			order.setStatus(OrderStatus.SHIPPING);

			orderRepository.save(order);

			return;
		}

		// =========================================
		// CÓ SHOP READY_TO_SHIP
		// =========================================

		if (hasReadyToShip) {

			order.setStatus(OrderStatus.CONFIRMED);

			orderRepository.save(order);

			return;
		}

		// =========================================
		// CÓ SHOP ĐANG PREPARING
		// =========================================

		if (hasPreparing) {

			order.setStatus(OrderStatus.CONFIRMED);

			orderRepository.save(order);

			return;
		}

		// =========================================
		// CÓ SHOP CONFIRMED
		// =========================================

		if (hasConfirmed) {

			order.setStatus(OrderStatus.CONFIRMED);

			orderRepository.save(order);

			return;
		}

		// =========================================
		// CÒN SHOP PENDING
		// =========================================

		if (hasPending) {

			order.setStatus(OrderStatus.PENDING);

			orderRepository.save(order);
		}
	}

	// =========================================================
	// VALIDATE STATUS TRANSITION
	// =========================================================

	private void validateStatusTransition(ShopOrderStatus current, ShopOrderStatus next) {

		if (current == null) {

			throw new IllegalArgumentException("Trạng thái hiện tại không được null");
		}

		if (next == null) {

			throw new IllegalArgumentException("Trạng thái mới không được null");
		}

		if (current == next) {

			throw new IllegalArgumentException("Trạng thái mới giống trạng thái hiện tại");
		}

		// =========================================
		// TERMINAL STATES
		// =========================================

		if (current == ShopOrderStatus.CANCELLED) {

			throw new IllegalArgumentException("Đơn đã bị hủy, không thể thay đổi trạng thái");
		}

		if (current == ShopOrderStatus.DELIVERED) {

			throw new IllegalArgumentException("Đơn đã giao thành công, không thể thay đổi trạng thái");
		}

		// =========================================
		// SHIPMENT / SHIPPER MANAGED
		// =========================================

		if (current == ShopOrderStatus.READY_TO_SHIP) {

			throw new IllegalArgumentException(
					"ShopOrder đã READY_TO_SHIP. " + "Hãy tạo Shipment để Shipper tiếp tục xử lý.");
		}

		if (current == ShopOrderStatus.SHIPPING) {

			throw new IllegalArgumentException("ShopOrder đang được Shipper xử lý.");
		}

		// =========================================
		// STATUS FLOW
		// =========================================

		boolean valid = false;

		switch (current) {

		case PENDING:

			valid = next == ShopOrderStatus.CONFIRMED || next == ShopOrderStatus.CANCELLED;

			break;

		case CONFIRMED:

			valid = next == ShopOrderStatus.PREPARING || next == ShopOrderStatus.CANCELLED;

			break;

		case PREPARING:

			valid = next == ShopOrderStatus.READY_TO_SHIP;

			break;

		default:

			break;
		}

		if (!valid) {

			throw new IllegalArgumentException("Không thể chuyển trạng thái từ " + current + " sang " + next);
		}
	}
}