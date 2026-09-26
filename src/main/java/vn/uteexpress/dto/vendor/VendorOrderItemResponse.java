package vn.uteexpress.dto.vendor;

import java.math.BigDecimal;

public class VendorOrderItemResponse {

	private Long orderItemId;
	private Long productId;
	private String productName;
	private int quantity;
	private BigDecimal unitPrice;
	private BigDecimal subtotal;

	public VendorOrderItemResponse() {
	}

	public VendorOrderItemResponse(Long orderItemId, Long productId, String productName, int quantity,
			BigDecimal unitPrice, BigDecimal subtotal) {

		this.orderItemId = orderItemId;
		this.productId = productId;
		this.productName = productName;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.subtotal = subtotal;
	}

	public Long getOrderItemId() {
		return orderItemId;
	}

	public void setOrderItemId(Long orderItemId) {
		this.orderItemId = orderItemId;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}
}