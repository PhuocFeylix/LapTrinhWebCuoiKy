package vn.uteexpress.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.Product;
import vn.uteexpress.entity.Promotion;
import vn.uteexpress.entity.PromotionProduct;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.ShopRepository;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.service.PromotionService;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

	private final PromotionService promotionService;
	private final UserRepository userRepository;
	private final ShopRepository shopRepository;

	public PromotionController(PromotionService promotionService, UserRepository userRepository,
			ShopRepository shopRepository) {

		this.promotionService = promotionService;
		this.userRepository = userRepository;
		this.shopRepository = shopRepository;
	}

	// =========================
	// CREATE PROMOTION
	// =========================

	@PostMapping("/shop/{shopId}")
	public ResponseEntity<Promotion> createPromotion(@PathVariable Long shopId, @RequestBody Promotion promotion,
			Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		checkShopAccess(shopId, currentUser);

		return ResponseEntity.ok(promotionService.createPromotion(promotion, shopId));
	}

	// =========================
	// UPDATE PROMOTION
	// =========================

	@PutMapping("/{promotionId}/shop/{shopId}")
	public ResponseEntity<Promotion> updatePromotion(@PathVariable Long promotionId, @PathVariable Long shopId,
			@RequestBody Promotion promotion, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		checkShopAccess(shopId, currentUser);

		return ResponseEntity.ok(promotionService.updatePromotion(promotionId, promotion, shopId));
	}

	// =========================
	// DELETE PROMOTION
	// =========================

	@DeleteMapping("/{promotionId}/shop/{shopId}")
	public ResponseEntity<Void> deletePromotion(@PathVariable Long promotionId, @PathVariable Long shopId,
			Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		checkShopAccess(shopId, currentUser);

		promotionService.deletePromotion(promotionId, shopId);

		return ResponseEntity.noContent().build();
	}

	// =========================
	// GET PROMOTION
	// =========================

	@GetMapping("/{promotionId}")
	public ResponseEntity<Promotion> getPromotion(@PathVariable Long promotionId) {

		return ResponseEntity.ok(promotionService.getById(promotionId));
	}

	// =========================
	// GET PROMOTIONS BY SHOP
	// =========================

	@GetMapping("/shop/{shopId}")
	public ResponseEntity<List<Promotion>> getByShop(@PathVariable Long shopId) {

		return ResponseEntity.ok(promotionService.getByShop(shopId));
	}

	// =========================
	// GET ACTIVE PROMOTIONS
	// =========================

	@GetMapping("/shop/{shopId}/active")
	public ResponseEntity<List<Promotion>> getActiveByShop(@PathVariable Long shopId) {

		return ResponseEntity.ok(promotionService.getActiveByShop(shopId));
	}

	// =========================
	// ADD PRODUCT TO PROMOTION
	// =========================

	@PostMapping("/{promotionId}/products/{productId}/shop/{shopId}")
	public ResponseEntity<PromotionProduct> addProduct(@PathVariable Long promotionId, @PathVariable Long productId,
			@PathVariable Long shopId, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		checkShopAccess(shopId, currentUser);

		return ResponseEntity.ok(promotionService.addProductToPromotion(promotionId, productId, shopId));
	}

	// =========================
	// GET PRODUCTS OF PROMOTION
	// =========================

	@GetMapping("/{promotionId}/products")
	public ResponseEntity<List<Product>> getProducts(@PathVariable Long promotionId) {

		return ResponseEntity.ok(promotionService.getProductsByPromotion(promotionId));
	}

	// =========================
	// REMOVE PRODUCT
	// =========================

	@DeleteMapping("/{promotionId}/products/{productId}/shop/{shopId}")
	public ResponseEntity<Void> removeProduct(@PathVariable Long promotionId, @PathVariable Long productId,
			@PathVariable Long shopId, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		checkShopAccess(shopId, currentUser);

		promotionService.removeProductFromPromotion(promotionId, productId, shopId);

		return ResponseEntity.noContent().build();
	}

	// =========================
	// GET PRODUCT PRICE
	// =========================

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

	// =========================
	// GET CURRENT USER
	// =========================

	private User getCurrentUser(Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated()) {

			throw new IllegalArgumentException("Chưa đăng nhập");
		}

		return userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
	}

	// =========================
	// CHECK SHOP ACCESS
	// =========================

	private void checkShopAccess(Long shopId, User currentUser) {

		if (shopId == null) {

			throw new IllegalArgumentException("Shop ID không được null");
		}

		if (currentUser == null || currentUser.getRole() == null) {

			throw new IllegalArgumentException("Không có quyền truy cập");
		}

		String roleName = currentUser.getRole().getName();

		// ADMIN và MANAGER được quản lý promotion
		if ("ADMIN".equalsIgnoreCase(roleName) || "MANAGER".equalsIgnoreCase(roleName)) {

			return;
		}

		// Chỉ VENDOR mới được quản lý Shop của mình
		if (!"VENDOR".equalsIgnoreCase(roleName)) {

			throw new IllegalArgumentException("Chỉ Vendor, Manager hoặc Admin được quản lý promotion");
		}

		boolean ownsShop = shopRepository.findByIdAndVendorId(shopId, currentUser.getId()).isPresent();

		if (!ownsShop) {

			throw new IllegalArgumentException("Không được thao tác promotion của Shop khác");
		}
	}
}