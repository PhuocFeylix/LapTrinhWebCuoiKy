package vn.uteexpress.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import vn.uteexpress.dto.PaymentRequest;
import vn.uteexpress.entity.Order;
import vn.uteexpress.entity.Payment;
import vn.uteexpress.repository.OrderRepository;
import vn.uteexpress.repository.PaymentRepository;

@Service
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final OrderRepository orderRepository;

	public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
		this.paymentRepository = paymentRepository;
		this.orderRepository = orderRepository;
	}

	public Payment createPayment(PaymentRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Payment request is required");
		}
		if (request.getOrderId() == null) {
			throw new IllegalArgumentException("Order id is required");
		}
		if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Payment amount must be greater than zero");
		}

		Order order = orderRepository.findById(request.getOrderId())
				.orElseThrow(() -> new RuntimeException("Order not found: " + request.getOrderId()));

		Payment payment = new Payment();
		payment.setOrder(order);
		payment.setAmount(request.getAmount());
		payment.setCurrency(request.getCurrency() == null ? "VND" : request.getCurrency().toUpperCase());
		payment.setProvider(request.getProvider() == null ? "MANUAL" : request.getProvider());
		payment.setStatus("PENDING");
		payment.setCreatedAt(LocalDateTime.now());

		return paymentRepository.save(payment);
	}

	public Payment getPaymentById(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("Payment id is required");
		}

		return paymentRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Payment not found: " + id));
	}

	public Payment cancelPayment(Long id) {
		Payment payment = getPaymentById(id);
		if ("PAID".equalsIgnoreCase(payment.getStatus())) {
			throw new IllegalStateException("Cannot cancel a paid payment");
		}

		payment.setStatus("CANCELLED");
		payment.setUpdatedAt(LocalDateTime.now());
		return paymentRepository.save(payment);
	}
}
