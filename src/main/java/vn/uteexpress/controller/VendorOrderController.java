package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.dto.vendor.VendorOrderResponse;
import vn.uteexpress.entity.OrderStatus;
import vn.uteexpress.service.VendorOrderService;

@RestController
@RequestMapping("/api/vendor/orders")
public class VendorOrderController {

	private final VendorOrderService vendorOrderService;

	public VendorOrderController(VendorOrderService vendorOrderService) {

		this.vendorOrderService = vendorOrderService;
	}

	@GetMapping("/vendor/{vendorId}")
	public ResponseEntity<List<VendorOrderResponse>> getOrders(@PathVariable Long vendorId,
			@RequestParam(required = false) OrderStatus status) {

		return ResponseEntity.ok(vendorOrderService.getOrdersByVendor(vendorId, status));
	}

	@GetMapping("/vendor/{vendorId}/{orderId}")
	public ResponseEntity<VendorOrderResponse> getOrderDetail(@PathVariable Long vendorId, @PathVariable Long orderId) {

		return ResponseEntity.ok(vendorOrderService.getOrderDetail(vendorId, orderId));
	}
}