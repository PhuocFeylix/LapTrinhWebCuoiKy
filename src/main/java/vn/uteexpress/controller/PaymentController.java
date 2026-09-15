package vn.uteexpress.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.uteexpress.dto.PaymentRequest;
import vn.uteexpress.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping
	public ResponseEntity<?> createPayment(@RequestBody PaymentRequest request) {
		return ResponseEntity.ok(paymentService.createPayment(request));
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getPaymentById(@PathVariable Long id) {
		return ResponseEntity.ok(paymentService.getPaymentById(id));
	}

	@PostMapping("/{id}/cancel")
	public ResponseEntity<?> cancelPayment(@PathVariable Long id) {
		return ResponseEntity.ok(paymentService.cancelPayment(id));
	}
}
