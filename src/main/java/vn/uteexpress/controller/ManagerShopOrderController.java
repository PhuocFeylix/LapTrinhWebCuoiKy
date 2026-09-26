package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.ShopOrder;
import vn.uteexpress.entity.ShopOrderStatus;
import vn.uteexpress.service.ManagerShopOrderService;

@RestController
@RequestMapping("/api/manager/shop-orders")
public class ManagerShopOrderController {

	private final ManagerShopOrderService managerShopOrderService;

	public ManagerShopOrderController(ManagerShopOrderService managerShopOrderService) {

		this.managerShopOrderService = managerShopOrderService;
	}

	@GetMapping
	public ResponseEntity<List<ShopOrder>> getShopOrders(@RequestParam(required = false) ShopOrderStatus status) {

		return ResponseEntity.ok(managerShopOrderService.getShopOrdersByStatus(status));
	}

	@GetMapping("/ready-to-ship")
	public ResponseEntity<List<ShopOrder>> getReadyToShipOrders() {

		return ResponseEntity.ok(managerShopOrderService.getReadyToShipOrders());
	}

	@GetMapping("/{shopOrderId}")
	public ResponseEntity<ShopOrder> getShopOrder(@PathVariable Long shopOrderId) {

		return ResponseEntity.ok(managerShopOrderService.getShopOrder(shopOrderId));
	}
}