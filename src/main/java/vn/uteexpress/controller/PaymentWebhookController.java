package vn.uteexpress.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.uteexpress.entity.Payment;
import vn.uteexpress.repository.PaymentRepository;

@RestController
@RequestMapping("/api/payments")
public class PaymentWebhookController {

	private final PaymentRepository paymentRepository;

	public PaymentWebhookController(PaymentRepository paymentRepository) {
		this.paymentRepository = paymentRepository;
	}

	@PostMapping("/webhook")
	public ResponseEntity<?> handleWebhook(@RequestHeader(value = "x-signature", required = false) String signature,
			@RequestBody Map<String, Object> payload) {
		String paymentId = String.valueOf(payload.getOrDefault("paymentId", ""));
		String status = String.valueOf(payload.getOrDefault("status", "")).toUpperCase();
		String transactionCode = String.valueOf(payload.getOrDefault("transactionCode", ""));

		if (paymentId.isBlank() || status.isBlank()) {
			return ResponseEntity.badRequest().body(Map.of("error", "Invalid webhook payload"));
		}

		Payment payment = paymentRepository.findById(Long.valueOf(paymentId))
				.orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

		if ("PAID".equals(status) || "SUCCESS".equals(status) || "COMPLETED".equals(status)) {
			if (!"PAID".equalsIgnoreCase(payment.getStatus())) {
				payment.setStatus("PAID");
				payment.setTransactionCode(transactionCode);
				payment.setUpdatedAt(LocalDateTime.now());
				paymentRepository.save(payment);
			}
			return ResponseEntity.ok(Map.of("status", "SUCCESS", "paymentId", payment.getId()));
		}

		if ("FAILED".equals(status) || "CANCELLED".equals(status)) {
			payment.setStatus("CANCELLED");
			payment.setUpdatedAt(LocalDateTime.now());
			paymentRepository.save(payment);
			return ResponseEntity.ok(Map.of("status", "UPDATED", "paymentId", payment.getId()));
		}

		return ResponseEntity.ok(Map.of("status", "IGNORED", "paymentId", payment.getId()));
	}
}
