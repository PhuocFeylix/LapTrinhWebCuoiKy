package vn.uteexpress.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.dto.vendor.VendorOrderItemResponse;
import vn.uteexpress.dto.vendor.VendorOrderResponse;
import vn.uteexpress.entity.Order;
import vn.uteexpress.entity.OrderItem;
import vn.uteexpress.entity.OrderStatus;
import vn.uteexpress.repository.OrderRepository;

@Service
public class VendorOrderService {

	private final OrderRepository orderRepository;

	public VendorOrderService(OrderRepository orderRepository) {

		this.orderRepository = orderRepository;
	}

	// =========================
	// GET ORDERS BY VENDOR
	// =========================

	@Transactional(readOnly = true)
	public List<VendorOrderResponse> getOrdersByVendor(Long vendorId, OrderStatus status) {

		if (vendorId == null) {
			throw new IllegalArgumentException("Vendor ID không được null");
		}

		List<Order> orders;

		if (status == null) {

			orders = orderRepository.findOrdersByVendorId(vendorId);

		} else {

			orders = orderRepository.findOrdersByVendorIdAndStatus(vendorId, status);
		}

		return orders.stream().map(order -> convertToVendorOrder(order, vendorId))
				.filter(order -> !order.getItems().isEmpty()).toList();
	}

	// =========================
	// GET ORDER DETAIL
	// =========================

	@Transactional(readOnly = true)
	public VendorOrderResponse getOrderDetail(Long vendorId, Long orderId) {

		if (vendorId == null) {
			throw new IllegalArgumentException("Vendor ID không được null");
		}

		if (orderId == null) {
			throw new IllegalArgumentException("Order ID không được null");
		}

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));

		VendorOrderResponse response = convertToVendorOrder(order, vendorId);

		if (response.getItems() == null || response.getItems().isEmpty()) {

			throw new IllegalArgumentException("Đơn hàng không thuộc Shop của Vendor");
		}

		return response;
	}

	// =========================
	// CONVERT ORDER
	// =========================

	private VendorOrderResponse convertToVendorOrder(Order order, Long vendorId) {

		List<VendorOrderItemResponse> items = order.getItems().stream()
				.filter(item -> item.getProduct() != null && item.getProduct().getShop() != null
						&& item.getProduct().getShop().getVendor() != null
						&& item.getProduct().getShop().getVendor().getId().equals(vendorId))
				.map(this::convertItem).toList();

		BigDecimal vendorSubtotal = items.stream().map(VendorOrderItemResponse::getSubtotal).reduce(BigDecimal.ZERO,
				BigDecimal::add);

		return new VendorOrderResponse(order.getId(), order.getUser().getId(), order.getStatus(), order.getCouponCode(),
				order.getCreatedAt(), vendorSubtotal, items);
	}

	// =========================
	// CONVERT ORDER ITEM
	// =========================

	private VendorOrderItemResponse convertItem(OrderItem item) {

		BigDecimal subtotal = item.getSubtotal();

		return new VendorOrderItemResponse(item.getId(), item.getProduct().getId(), item.getProductName(),
				item.getQuantity(), item.getUnitPrice(), subtotal);
	}
}