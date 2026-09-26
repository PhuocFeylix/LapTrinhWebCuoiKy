package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.ShopOrder;
import vn.uteexpress.entity.ShopOrderStatus;
import vn.uteexpress.service.VendorShopOrderService;

@RestController
@RequestMapping("/api/vendor/shop-orders")
public class VendorShopOrderController {

	private final VendorShopOrderService service;

	public VendorShopOrderController(VendorShopOrderService service) {

		this.service = service;
	}

	@GetMapping("/shop/{shopId}")
	public ResponseEntity<List<ShopOrder>> getOrders(@PathVariable Long shopId,
			@RequestParam(required = false) ShopOrderStatus status) {

		if (status == null) {
			return ResponseEntity.ok(service.getShopOrders(shopId));
		}

		return ResponseEntity.ok(service.getShopOrdersByStatus(shopId, status));
	}

	@GetMapping("/shop/{shopId}/{shopOrderId}")
	public ResponseEntity<ShopOrder> getOrder(@PathVariable Long shopId, @PathVariable Long shopOrderId) {

		return ResponseEntity.ok(service.getShopOrder(shopId, shopOrderId));
	}

	@PutMapping("/{shopOrderId}/shop/{shopId}/status")
	public ResponseEntity<ShopOrder> updateStatus(@PathVariable Long shopId, @PathVariable Long shopOrderId,
			@RequestParam ShopOrderStatus status) {

		return ResponseEntity.ok(service.updateStatus(shopId, shopOrderId, status));
	}
}