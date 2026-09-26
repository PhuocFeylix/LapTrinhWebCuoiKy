package vn.uteexpress.controller;

import vn.uteexpress.entity.ViewedProduct;
import vn.uteexpress.service.ViewedProductService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/viewed-products")
public class ViewedProductController {

	private final ViewedProductService viewedProductService;

	public ViewedProductController(ViewedProductService viewedProductService) {

		this.viewedProductService = viewedProductService;
	}

	@PostMapping("/user/{userId}/product/{productId}")
	public ResponseEntity<ViewedProduct> recordView(@PathVariable Long userId, @PathVariable Long productId) {

		return ResponseEntity.ok(viewedProductService.recordView(userId, productId));
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<ViewedProduct>> getViewedProducts(@PathVariable Long userId) {

		return ResponseEntity.ok(viewedProductService.getViewedProducts(userId));
	}

	@DeleteMapping("/user/{userId}/product/{productId}")
	public ResponseEntity<Void> removeViewedProduct(@PathVariable Long userId, @PathVariable Long productId) {

		viewedProductService.removeViewedProduct(userId, productId);

		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/user/{userId}")
	public ResponseEntity<Void> clearHistory(@PathVariable Long userId) {

		viewedProductService.clearHistory(userId);

		return ResponseEntity.noContent().build();
	}
}