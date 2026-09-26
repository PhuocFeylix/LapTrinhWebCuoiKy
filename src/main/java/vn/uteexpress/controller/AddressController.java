package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.Address;
import vn.uteexpress.service.AddressService;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

	private final AddressService addressService;

	public AddressController(AddressService addressService) {

		this.addressService = addressService;
	}

	// =========================
	// GET ALL
	// =========================

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<Address>> getUserAddresses(@PathVariable Long userId) {

		return ResponseEntity.ok(addressService.getUserAddresses(userId));
	}

	// =========================
	// GET ONE
	// =========================

	@GetMapping("/{addressId}/user/{userId}")
	public ResponseEntity<Address> getAddress(@PathVariable Long addressId, @PathVariable Long userId) {

		return ResponseEntity.ok(addressService.getAddress(userId, addressId));
	}

	// =========================
	// CREATE
	// =========================

	@PostMapping("/user/{userId}")
	public ResponseEntity<Address> createAddress(@PathVariable Long userId, @RequestParam String receiverName,
			@RequestParam String phone, @RequestParam String address, @RequestParam(required = false) String ward,
			@RequestParam(required = false) String district, @RequestParam String city,
			@RequestParam(defaultValue = "false") boolean defaultAddress) {

		return ResponseEntity.ok(addressService.createAddress(userId, receiverName, phone, address, ward, district,
				city, defaultAddress));
	}

	// =========================
	// UPDATE
	// =========================

	@PutMapping("/{addressId}/user/{userId}")
	public ResponseEntity<Address> updateAddress(@PathVariable Long addressId, @PathVariable Long userId,
			@RequestParam String receiverName, @RequestParam String phone, @RequestParam String address,
			@RequestParam(required = false) String ward, @RequestParam(required = false) String district,
			@RequestParam String city, @RequestParam(defaultValue = "false") boolean defaultAddress) {

		return ResponseEntity.ok(addressService.updateAddress(userId, addressId, receiverName, phone, address, ward,
				district, city, defaultAddress));
	}

	// =========================
	// SET DEFAULT
	// =========================

	@PutMapping("/{addressId}/user/{userId}/default")
	public ResponseEntity<Address> setDefaultAddress(@PathVariable Long addressId, @PathVariable Long userId) {

		return ResponseEntity.ok(addressService.setDefaultAddress(userId, addressId));
	}

	// =========================
	// DELETE
	// =========================

	@DeleteMapping("/{addressId}/user/{userId}")
	public ResponseEntity<Void> deleteAddress(@PathVariable Long addressId, @PathVariable Long userId) {

		addressService.deleteAddress(userId, addressId);

		return ResponseEntity.noContent().build();
	}
}