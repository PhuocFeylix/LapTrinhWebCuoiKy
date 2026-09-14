package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<Address>> getByUser(@PathVariable Long userId) {

		return ResponseEntity.ok(addressService.findByUserId(userId));
	}

	@GetMapping("/{id}/user/{userId}")
	public ResponseEntity<Address> getById(@PathVariable Long id, @PathVariable Long userId) {

		return ResponseEntity.ok(addressService.findByIdAndUser(id, userId));
	}

	@GetMapping("/user/{userId}/default")
	public ResponseEntity<Address> getDefault(@PathVariable Long userId) {

		return ResponseEntity.ok(addressService.getDefault(userId));
	}

	@PostMapping
	public ResponseEntity<Address> create(@RequestBody Address address, @RequestParam Long userId) {

		return ResponseEntity.status(HttpStatus.CREATED).body(addressService.create(address, userId));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Address> update(@PathVariable Long id, @RequestParam Long userId,
			@RequestBody Address address) {

		return ResponseEntity.ok(addressService.update(id, address, userId));
	}

	@PutMapping("/{id}/default")
	public ResponseEntity<Address> setDefault(@PathVariable Long id, @RequestParam Long userId) {

		return ResponseEntity.ok(addressService.setDefault(id, userId));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam Long userId) {

		addressService.delete(id, userId);

		return ResponseEntity.noContent().build();
	}
}