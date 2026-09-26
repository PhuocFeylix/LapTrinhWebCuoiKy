package vn.uteexpress.dto.vendor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import vn.uteexpress.entity.OrderStatus;

public class VendorOrderResponse {

	private Long orderId;
	private Long userId;

	private OrderStatus status;

	private String couponCode;

	private LocalDateTime createdAt;

	private BigDecimal vendorSubtotal;

	private List<VendorOrderItemResponse> items;

	public VendorOrderResponse() {
	}

	public VendorOrderResponse(Long orderId, Long userId, OrderStatus status, String couponCode,
			LocalDateTime createdAt, BigDecimal vendorSubtotal, List<VendorOrderItemResponse> items) {

		this.orderId = orderId;
		this.userId = userId;
		this.status = status;
		this.couponCode = couponCode;
		this.createdAt = createdAt;
		this.vendorSubtotal = vendorSubtotal;
		this.items = items;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public String getCouponCode() {
		return couponCode;
	}

	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public BigDecimal getVendorSubtotal() {
		return vendorSubtotal;
	}

	public void setVendorSubtotal(BigDecimal vendorSubtotal) {
		this.vendorSubtotal = vendorSubtotal;
	}

	public List<VendorOrderItemResponse> getItems() {
		return items;
	}

	public void setItems(List<VendorOrderItemResponse> items) {
		this.items = items;
	}
}