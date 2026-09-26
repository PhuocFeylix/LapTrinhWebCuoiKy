package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.ShipmentStatus;
import vn.uteexpress.entity.User;
import vn.uteexpress.service.ShipmentService;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

	private final ShipmentService shipmentService;

	public ShipmentController(ShipmentService shipmentService) {
		this.shipmentService = shipmentService;
	}

	@PostMapping("/order/{orderId}")
	public ResponseEntity<Shipment> createShipment(@PathVariable Long orderId) {

		return ResponseEntity.ok(shipmentService.createShipment(orderId));
	}

	@PutMapping("/{shipmentId}/assign/{shipperId}")
	public ResponseEntity<Shipment> assignShipper(@PathVariable Long shipmentId, @PathVariable Long shipperId) {

		return ResponseEntity.ok(shipmentService.assignShipper(shipmentId, shipperId));
	}

	@PutMapping("/{shipmentId}/status")
	public ResponseEntity<Shipment> updateStatus(@PathVariable Long shipmentId, @RequestParam ShipmentStatus status) {

		return ResponseEntity.ok(shipmentService.updateStatus(shipmentId, status));
	}

	@GetMapping("/{shipmentId}")
	public ResponseEntity<Shipment> getShipment(@PathVariable Long shipmentId) {

		return ResponseEntity.ok(shipmentService.getById(shipmentId));
	}

	@GetMapping("/order/{orderId}")
	public ResponseEntity<List<Shipment>> getByOrder(@PathVariable Long orderId) {

		return ResponseEntity.ok(shipmentService.getByOrderId(orderId));
	}

	@GetMapping("/tracking/{trackingCode}")
	public ResponseEntity<Shipment> getByTrackingCode(@PathVariable String trackingCode) {

		return ResponseEntity.ok(shipmentService.getByTrackingCode(trackingCode));
	}

	@GetMapping("/shipper/{shipperId}")
	public ResponseEntity<List<Shipment>> getByShipper(@PathVariable Long shipperId) {

		return ResponseEntity.ok(shipmentService.getShipmentsByShipper(shipperId));
	}

	@GetMapping("/shippers")
	public ResponseEntity<List<User>> getShippers() {

		return ResponseEntity.ok(shipmentService.getShippers());
	}

	@GetMapping("/unassigned")
	public ResponseEntity<List<Shipment>> getUnassigned() {

		return ResponseEntity.ok(shipmentService.getUnassignedShipments());
	}

	/**
	 * Vendor tạo Shipment khi ShopOrder đã READY_TO_SHIP.
	 */
	@PostMapping("/shop-order/{shopOrderId}")
	public ResponseEntity<Shipment> createFromShopOrder(@PathVariable Long shopOrderId) {

		return ResponseEntity.status(HttpStatus.CREATED).body(shipmentService.createShipmentFromShopOrder(shopOrderId));
	}
}