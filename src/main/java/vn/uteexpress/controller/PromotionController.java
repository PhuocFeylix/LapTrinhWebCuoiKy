package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.Promotion;
import vn.uteexpress.service.PromotionService;
import vn.uteexpress.entity.Product;
import vn.uteexpress.entity.PromotionProduct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

	private final PromotionService promotionService;

	public PromotionController(PromotionService promotionService) {
		this.promotionService = promotionService;
	}

	@PostMapping("/shop/{shopId}")
	public ResponseEntity<Promotion> createPromotion(@PathVariable Long shopId, @RequestBody Promotion promotion) {

		return ResponseEntity.ok(promotionService.createPromotion(promotion, shopId));
	}

	@PutMapping("/{promotionId}/shop/{shopId}")
	public ResponseEntity<Promotion> updatePromotion(@PathVariable Long promotionId, @PathVariable Long shopId,
			@RequestBody Promotion promotion) {

		return ResponseEntity.ok(promotionService.updatePromotion(promotionId, promotion, shopId));
	}

	@DeleteMapping("/{promotionId}/shop/{shopId}")
	public ResponseEntity<Void> deletePromotion(@PathVariable Long promotionId, @PathVariable Long shopId) {

		promotionService.deletePromotion(promotionId, shopId);

		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{promotionId}")
	public ResponseEntity<Promotion> getPromotion(@PathVariable Long promotionId) {

		return ResponseEntity.ok(promotionService.getById(promotionId));
	}

	@GetMapping("/shop/{shopId}")
	public ResponseEntity<List<Promotion>> getByShop(@PathVariable Long shopId) {

		return ResponseEntity.ok(promotionService.getByShop(shopId));
	}

	@GetMapping("/shop/{shopId}/active")
	public ResponseEntity<List<Promotion>> getActiveByShop(@PathVariable Long shopId) {

		return ResponseEntity.ok(promotionService.getActiveByShop(shopId));
	}

	@PostMapping("/{promotionId}/products/{productId}/shop/{shopId}")
	public ResponseEntity<PromotionProduct> addProduct(@PathVariable Long promotionId, @PathVariable Long productId,
			@PathVariable Long shopId) {

		return ResponseEntity.ok(promotionService.addProductToPromotion(promotionId, productId, shopId));
	}

	@GetMapping("/{promotionId}/products")
	public ResponseEntity<List<Product>> getProducts(@PathVariable Long promotionId) {

		return ResponseEntity.ok(promotionService.getProductsByPromotion(promotionId));
	}

	@DeleteMapping("/{promotionId}/products/{productId}/shop/{shopId}")
	public ResponseEntity<Void> removeProduct(@PathVariable Long promotionId, @PathVariable Long productId,
			@PathVariable Long shopId) {

		promotionService.removeProductFromPromotion(promotionId, productId, shopId);

		return ResponseEntity.noContent().build();
	}

	@GetMapping("/product/{productId}/price")
	public ResponseEntity<Map<String, Object>> getProductPrice(@PathVariable Long productId,
			@RequestParam BigDecimal originalPrice) {

		Promotion promotion = promotionService.getActivePromotionForProduct(productId);

		BigDecimal finalPrice = promotionService.getFinalPriceForProduct(productId, originalPrice);

		Map<String, Object> result = new HashMap<>();

		result.put("productId", productId);
		result.put("originalPrice", originalPrice);
		result.put("finalPrice", finalPrice);
		result.put("promotion", promotion);

		return ResponseEntity.ok(result);
	}
}