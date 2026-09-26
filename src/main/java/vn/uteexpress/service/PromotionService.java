package vn.uteexpress.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.Product;
import vn.uteexpress.entity.Promotion;
import vn.uteexpress.entity.PromotionProduct;
import vn.uteexpress.entity.PromotionType;
import vn.uteexpress.entity.Shop;
import vn.uteexpress.repository.ProductRepository;
import vn.uteexpress.repository.PromotionProductRepository;
import vn.uteexpress.repository.PromotionRepository;
import vn.uteexpress.repository.ShopRepository;

@Service
public class PromotionService {

	private final PromotionRepository promotionRepository;
	private final ShopRepository shopRepository;
	private final PromotionProductRepository promotionProductRepository;
	private final ProductRepository productRepository;

	public PromotionService(PromotionRepository promotionRepository, ShopRepository shopRepository,
			PromotionProductRepository promotionProductRepository, ProductRepository productRepository) {

		this.promotionRepository = promotionRepository;
		this.shopRepository = shopRepository;
		this.promotionProductRepository = promotionProductRepository;
		this.productRepository = productRepository;
	}

	// =========================
	// CREATE
	// =========================

	@Transactional
	public Promotion createPromotion(Promotion promotion, Long shopId) {

		if (promotion == null) {

			throw new IllegalArgumentException("Thông tin promotion không được null");
		}

		if (shopId == null) {

			throw new IllegalArgumentException("Shop ID không được null");
		}

		Shop shop = shopRepository.findById(shopId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy shop với ID: " + shopId));

		validatePromotion(promotion);

		promotion.setId(null);
		promotion.setShop(shop);
		promotion.setName(promotion.getName().trim());

		return promotionRepository.save(promotion);
	}

	// =========================
	// UPDATE
	// =========================

	@Transactional
	public Promotion updatePromotion(Long promotionId, Promotion promotionData, Long shopId) {

		if (promotionId == null) {

			throw new IllegalArgumentException("Promotion ID không được null");
		}

		if (shopId == null) {

			throw new IllegalArgumentException("Shop ID không được null");
		}

		Promotion promotion = findPromotion(promotionId);

		if (promotion.getShop() == null || !promotion.getShop().getId().equals(shopId)) {

			throw new IllegalArgumentException("Promotion không thuộc shop này");
		}

		validatePromotion(promotionData);

		promotion.setName(promotionData.getName().trim());

		promotion.setDescription(promotionData.getDescription());

		promotion.setType(promotionData.getType());

		promotion.setDiscountValue(promotionData.getDiscountValue());

		promotion.setStartAt(promotionData.getStartAt());

		promotion.setEndAt(promotionData.getEndAt());

		promotion.setActive(promotionData.isActive());

		return promotionRepository.save(promotion);
	}

	// =========================
	// DELETE
	// =========================

	@Transactional
	public void deletePromotion(Long promotionId, Long shopId) {

		if (promotionId == null) {

			throw new IllegalArgumentException("Promotion ID không được null");
		}

		if (shopId == null) {

			throw new IllegalArgumentException("Shop ID không được null");
		}

		Promotion promotion = findPromotion(promotionId);

		if (promotion.getShop() == null || !promotion.getShop().getId().equals(shopId)) {

			throw new IllegalArgumentException("Promotion không thuộc shop này");
		}

		promotionProductRepository.deleteByPromotionId(promotionId);

		promotionRepository.delete(promotion);
	}

	// =========================
	// GET BY ID
	// =========================

	@Transactional(readOnly = true)
	public Promotion getById(Long promotionId) {

		return findPromotion(promotionId);
	}

	// =========================
	// GET BY SHOP
	// =========================

	@Transactional(readOnly = true)
	public List<Promotion> getByShop(Long shopId) {

		if (shopId == null) {

			throw new IllegalArgumentException("Shop ID không được null");
		}

		return promotionRepository.findByShopIdOrderByStartAtDesc(shopId);
	}

	// =========================
	// GET ACTIVE BY SHOP
	// =========================

	@Transactional(readOnly = true)
	public List<Promotion> getActiveByShop(Long shopId) {

		if (shopId == null) {

			throw new IllegalArgumentException("Shop ID không được null");
		}

		return promotionRepository.findByShopIdAndActiveTrueOrderByStartAtDesc(shopId);
	}

	// =========================
	// CALCULATE DISCOUNT
	// =========================

	public BigDecimal calculateDiscount(Promotion promotion, BigDecimal originalPrice) {

		if (promotion == null) {
			return BigDecimal.ZERO;
		}

		if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0) {

			return BigDecimal.ZERO;
		}

		if (!promotion.isActive()) {
			return BigDecimal.ZERO;
		}

		if (promotion.getDiscountValue() == null) {
			return BigDecimal.ZERO;
		}

		if (promotion.getStartAt() == null || promotion.getEndAt() == null) {

			return BigDecimal.ZERO;
		}

		LocalDateTime now = LocalDateTime.now();

		if (now.isBefore(promotion.getStartAt()) || now.isAfter(promotion.getEndAt())) {

			return BigDecimal.ZERO;
		}

		BigDecimal discount;

		if (promotion.getType() == PromotionType.PERCENT) {

			discount = originalPrice.multiply(promotion.getDiscountValue()).divide(BigDecimal.valueOf(100));

		} else {

			discount = promotion.getDiscountValue();
		}

		if (discount.compareTo(BigDecimal.ZERO) < 0) {

			discount = BigDecimal.ZERO;
		}

		if (discount.compareTo(originalPrice) > 0) {

			discount = originalPrice;
		}

		return discount;
	}

	// =========================
	// CALCULATE FINAL PRICE
	// =========================

	public BigDecimal calculateFinalPrice(Promotion promotion, BigDecimal originalPrice) {

		if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0) {

			return BigDecimal.ZERO;
		}

		BigDecimal discount = calculateDiscount(promotion, originalPrice);

		BigDecimal finalPrice = originalPrice.subtract(discount);

		if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {

			return BigDecimal.ZERO;
		}

		return finalPrice;
	}

	// =========================
	// ADD PRODUCT
	// =========================

	@Transactional
	public PromotionProduct addProductToPromotion(Long promotionId, Long productId, Long shopId) {

		validateIds(promotionId, productId, shopId);

		Promotion promotion = findPromotion(promotionId);

		checkPromotionBelongsToShop(promotion, shopId);

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy product với ID: " + productId));

		checkProductBelongsToShop(product, shopId);

		if (promotionProductRepository.findByPromotionIdAndProductId(promotionId, productId).isPresent()) {

			throw new IllegalArgumentException("Product đã được thêm vào promotion");
		}

		PromotionProduct promotionProduct = new PromotionProduct();

		promotionProduct.setPromotion(promotion);

		promotionProduct.setProduct(product);

		return promotionProductRepository.save(promotionProduct);
	}

	// =========================
	// GET PRODUCTS
	// =========================

	@Transactional(readOnly = true)
	public List<Product> getProductsByPromotion(Long promotionId) {

		if (promotionId == null) {

			throw new IllegalArgumentException("Promotion ID không được null");
		}

		// Kiểm tra promotion tồn tại
		findPromotion(promotionId);

		return promotionProductRepository.findByPromotionId(promotionId).stream().map(PromotionProduct::getProduct)
				.collect(Collectors.toList());
	}

	// =========================
	// REMOVE PRODUCT
	// =========================

	@Transactional
	public void removeProductFromPromotion(Long promotionId, Long productId, Long shopId) {

		validateIds(promotionId, productId, shopId);

		Promotion promotion = findPromotion(promotionId);

		checkPromotionBelongsToShop(promotion, shopId);

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy product với ID: " + productId));

		checkProductBelongsToShop(product, shopId);

		PromotionProduct promotionProduct = promotionProductRepository
				.findByPromotionIdAndProductId(promotionId, productId)
				.orElseThrow(() -> new RuntimeException("Product chưa được áp dụng promotion"));

		promotionProductRepository.delete(promotionProduct);
	}

	// =========================
	// GET ACTIVE PROMOTION
	// =========================

	@Transactional(readOnly = true)
	public Promotion getActivePromotionForProduct(Long productId) {

		if (productId == null) {

			throw new IllegalArgumentException("Product ID không được null");
		}

		List<PromotionProduct> promotionProducts = promotionProductRepository.findByProductId(productId);

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

		BigDecimal originalPrice = BigDecimal.valueOf(product.getPrice());

		LocalDateTime now = LocalDateTime.now();

		return promotionProducts.stream().map(PromotionProduct::getPromotion).filter(Promotion::isActive)
				.filter(promotion -> promotion.getStartAt() != null && promotion.getEndAt() != null
						&& !now.isBefore(promotion.getStartAt()) && !now.isAfter(promotion.getEndAt()))
				.max((p1, p2) -> {

					BigDecimal discount1 = calculateDiscount(p1, originalPrice);

					BigDecimal discount2 = calculateDiscount(p2, originalPrice);

					return discount1.compareTo(discount2);
				}).orElse(null);
	}

	// =========================
	// GET FINAL PRICE
	// =========================

	@Transactional(readOnly = true)
	public BigDecimal getFinalPriceForProduct(Long productId, BigDecimal originalPrice) {

		if (productId == null) {

			throw new IllegalArgumentException("Product ID không được null");
		}

		if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) < 0) {

			throw new IllegalArgumentException("Giá sản phẩm không hợp lệ");
		}

		Promotion promotion = getActivePromotionForProduct(productId);

		if (promotion == null) {

			return originalPrice;
		}

		return calculateFinalPrice(promotion, originalPrice);
	}

	// =========================
	// VALIDATE PROMOTION
	// =========================

	private void validatePromotion(Promotion promotion) {

		if (promotion == null) {

			throw new IllegalArgumentException("Promotion không được null");
		}

		if (promotion.getName() == null || promotion.getName().trim().isEmpty()) {

			throw new IllegalArgumentException("Tên promotion không được để trống");
		}

		if (promotion.getType() == null) {

			throw new IllegalArgumentException("Loại promotion không được để trống");
		}

		if (promotion.getDiscountValue() == null || promotion.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {

			throw new IllegalArgumentException("Giá trị giảm phải lớn hơn 0");
		}

		if (promotion.getType() == PromotionType.PERCENT
				&& promotion.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {

			throw new IllegalArgumentException("Phần trăm giảm không được lớn hơn 100%");
		}

		if (promotion.getStartAt() == null) {

			throw new IllegalArgumentException("Thời gian bắt đầu không được để trống");
		}

		if (promotion.getEndAt() == null) {

			throw new IllegalArgumentException("Thời gian kết thúc không được để trống");
		}

		if (!promotion.getEndAt().isAfter(promotion.getStartAt())) {

			throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu");
		}
	}

	// =========================
	// FIND PROMOTION
	// =========================

	private Promotion findPromotion(Long promotionId) {

		if (promotionId == null) {

			throw new IllegalArgumentException("Promotion ID không được null");
		}

		return promotionRepository.findById(promotionId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy promotion với ID: " + promotionId));
	}

	// =========================
	// CHECK PROMOTION SHOP
	// =========================

	private void checkPromotionBelongsToShop(Promotion promotion, Long shopId) {

		if (promotion == null || promotion.getShop() == null || promotion.getShop().getId() == null
				|| !promotion.getShop().getId().equals(shopId)) {

			throw new IllegalArgumentException("Promotion không thuộc shop này");
		}
	}

	// =========================
	// CHECK PRODUCT SHOP
	// =========================

	private void checkProductBelongsToShop(Product product, Long shopId) {

		if (product == null || product.getShop() == null || product.getShop().getId() == null
				|| !product.getShop().getId().equals(shopId)) {

			throw new IllegalArgumentException("Product không thuộc shop này");
		}
	}

	// =========================
	// VALIDATE IDS
	// =========================

	private void validateIds(Long promotionId, Long productId, Long shopId) {

		if (promotionId == null) {

			throw new IllegalArgumentException("Promotion ID không được null");
		}

		if (productId == null) {

			throw new IllegalArgumentException("Product ID không được null");
		}

		if (shopId == null) {

			throw new IllegalArgumentException("Shop ID không được null");
		}
	}
}