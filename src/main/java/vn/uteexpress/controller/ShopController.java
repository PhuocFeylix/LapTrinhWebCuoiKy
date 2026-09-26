package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.Shop;
import vn.uteexpress.service.ShopService;

@RestController
@RequestMapping("/api/shops")
public class ShopController {

	private final ShopService shopService;

	public ShopController(ShopService shopService) {
		this.shopService = shopService;
	}

	@GetMapping
	public ResponseEntity<List<Shop>> getAll() {

		return ResponseEntity.ok(shopService.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Shop> getById(@PathVariable Long id) {

		return ResponseEntity.ok(shopService.findById(id));
	}

	@GetMapping("/vendor/{vendorId}")
	public ResponseEntity<Shop> getByVendor(@PathVariable Long vendorId) {

		return ResponseEntity.ok(shopService.findByVendorId(vendorId));
	}

	@GetMapping("/search")
	public ResponseEntity<List<Shop>> search(@RequestParam(required = false) String keyword) {

		return ResponseEntity.ok(shopService.search(keyword));
	}

	@PostMapping
	public ResponseEntity<Shop> create(@RequestBody Shop shop, @RequestParam Long vendorId) {

		return ResponseEntity.status(HttpStatus.CREATED).body(shopService.create(shop, vendorId));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Shop> update(@PathVariable Long id, @RequestBody Shop shop) {

		return ResponseEntity.ok(shopService.update(id, shop));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {

		shopService.delete(id);

		return ResponseEntity.noContent().build();
	}

	// =========================
	// REGISTER SHOP
	// =========================

	@PostMapping("/vendor/{vendorId}")
	public ResponseEntity<Shop> registerShop(@PathVariable Long vendorId, @RequestParam String name,
			@RequestParam(required = false) String description, @RequestParam String address,
			@RequestParam String phone, @RequestParam(required = false) String logo) {

		return ResponseEntity.ok(shopService.registerShop(vendorId, name, description, address, phone, logo));
	}

	// =========================
	// UPDATE SHOP
	// =========================

	@PutMapping("/{shopId}/vendor/{vendorId}")
	public ResponseEntity<Shop> updateShop(@PathVariable Long shopId, @PathVariable Long vendorId,
			@RequestParam String name, @RequestParam(required = false) String description, @RequestParam String address,
			@RequestParam String phone, @RequestParam(required = false) String logo) {

		return ResponseEntity.ok(shopService.updateShop(vendorId, shopId, name, description, address, phone, logo));
	}
}