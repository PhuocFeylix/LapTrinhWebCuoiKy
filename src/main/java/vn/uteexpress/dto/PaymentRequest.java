package vn.uteexpress.dto;

import java.math.BigDecimal;

public class PaymentRequest {

	private Long orderId;
	private BigDecimal amount;
	private String currency = "VND";
	private String provider = "MANUAL";
	private String method = "BANK_TRANSFER";

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
}
