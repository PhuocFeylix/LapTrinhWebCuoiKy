package vn.uteexpress.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.Order;
import vn.uteexpress.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping("/user/{userId}/checkout")
	public ResponseEntity<Order> checkout(@PathVariable Long userId, @RequestParam Long addressId,
			@RequestParam(required = false) String couponCode) {

		return ResponseEntity.ok(orderService.checkout(userId, addressId, couponCode));
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<Order>> getOrders(@PathVariable Long userId) {
		return ResponseEntity.ok(orderService.getOrdersByUser(userId));
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<Order> getOrder(@PathVariable Long orderId) {
		return ResponseEntity.ok(orderService.getOrder(orderId));
	}
}
