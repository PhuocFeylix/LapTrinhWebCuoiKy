package vn.uteexpress.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.Promotion;
import vn.uteexpress.entity.PromotionType;
import vn.uteexpress.entity.Shop;
import vn.uteexpress.repository.PromotionRepository;
import vn.uteexpress.repository.ShopRepository;
import vn.uteexpress.entity.Product;
import vn.uteexpress.entity.PromotionProduct;
import vn.uteexpress.repository.ProductRepository;
import vn.uteexpress.repository.PromotionProductRepository;

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

		if (promotion.getStartAt() == null) {
			throw new IllegalArgumentException("Thời gian bắt đầu không được để trống");
		}

		if (promotion.getEndAt() == null) {
			throw new IllegalArgumentException("Thời gian kết thúc không được để trống");
		}

		if (!promotion.getEndAt().isAfter(promotion.getStartAt())) {
			throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu");
		}

		return promotionRepository.save(promotion);
	}

	@Transactional
	public Promotion updatePromotion(Long promotionId, Promotion promotionData, Long shopId) {

		Promotion promotion = findPromotion(promotionId);

		if (shopId == null) {
			throw new IllegalArgumentException("Shop ID không được null");
		}

		if (promotion.getShop() == null || !promotion.getShop().getId().equals(shopId)) {

			throw new IllegalArgumentException("Promotion không thuộc shop này");
		}

		validatePromotion(promotionData);

		promotion.setName(promotionData.getName());
		promotion.setDescription(promotionData.getDescription());
		promotion.setType(promotionData.getType());
		promotion.setDiscountValue(promotionData.getDiscountValue());
		promotion.setStartAt(promotionData.getStartAt());
		promotion.setEndAt(promotionData.getEndAt());
		promotion.setActive(promotionData.isActive());

		if (!promotion.getEndAt().isAfter(promotion.getStartAt())) {
			throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu");
		}

		return promotionRepository.save(promotion);
	}

	@Transactional
	public void deletePromotion(Long promotionId, Long shopId) {

		Promotion promotion = findPromotion(promotionId);

		if (promotion.getShop() == null || !promotion.getShop().getId().equals(shopId)) {

			throw new IllegalArgumentException("Promotion không thuộc shop này");
		}

		promotionProductRepository.deleteByPromotionId(promotionId);

		promotionRepository.delete(promotion);
	}

	public Promotion getById(Long promotionId) {
		return findPromotion(promotionId);
	}

	public List<Promotion> getByShop(Long shopId) {
		return promotionRepository.findByShopIdOrderByStartAtDesc(shopId);
	}

	public List<Promotion> getActiveByShop(Long shopId) {
		return promotionRepository.findByShopIdAndActiveTrueOrderByStartAtDesc(shopId);
	}

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

		LocalDateTime now = LocalDateTime.now();

		if (promotion.getStartAt() == null || promotion.getEndAt() == null) {

			return BigDecimal.ZERO;
		}

		if (now.isBefore(promotion.getStartAt()) || now.isAfter(promotion.getEndAt())) {

			return BigDecimal.ZERO;
		}

		BigDecimal discount;

		if (promotion.getType() == PromotionType.PERCENT) {

			discount = originalPrice.multiply(promotion.getDiscountValue()).divide(BigDecimal.valueOf(100));

		} else {

			discount = promotion.getDiscountValue();
		}

		if (discount.compareTo(originalPrice) > 0) {
			discount = originalPrice;
		}

		return discount;
	}

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

	private void validatePromotion(Promotion promotion) {

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
	}

	private Promotion findPromotion(Long promotionId) {

		return promotionRepository.findById(promotionId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy promotion với ID: " + promotionId));
	}

	@Transactional
	public PromotionProduct addProductToPromotion(Long promotionId, Long productId, Long shopId) {

		Promotion promotion = findPromotion(promotionId);

		if (promotion.getShop() == null || !promotion.getShop().getId().equals(shopId)) {

			throw new IllegalArgumentException("Promotion không thuộc shop này");
		}

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy product với ID: " + productId));

		if (product.getShop() == null || !product.getShop().getId().equals(shopId)) {

			throw new IllegalArgumentException("Product không thuộc shop này");
		}

		if (promotionProductRepository.findByPromotionIdAndProductId(promotionId, productId).isPresent()) {

			throw new IllegalArgumentException("Product đã được thêm vào promotion");
		}

		PromotionProduct promotionProduct = new PromotionProduct();

		promotionProduct.setPromotion(promotion);
		promotionProduct.setProduct(product);

		return promotionProductRepository.save(promotionProduct);
	}

	public List<Product> getProductsByPromotion(Long promotionId) {

		return promotionProductRepository.findByPromotionId(promotionId).stream().map(PromotionProduct::getProduct)
				.collect(Collectors.toList());
	}

	@Transactional
	public void removeProductFromPromotion(Long promotionId, Long productId, Long shopId) {

		Promotion promotion = findPromotion(promotionId);

		if (promotion.getShop() == null || !promotion.getShop().getId().equals(shopId)) {

			throw new IllegalArgumentException("Promotion không thuộc shop này");
		}

		PromotionProduct promotionProduct = promotionProductRepository
				.findByPromotionIdAndProductId(promotionId, productId)
				.orElseThrow(() -> new RuntimeException("Product chưa được áp dụng promotion"));

		promotionProductRepository.delete(promotionProduct);
	}

	public Promotion getActivePromotionForProduct(Long productId) {

		List<PromotionProduct> promotionProducts = promotionProductRepository.findByProductId(productId);

		LocalDateTime now = LocalDateTime.now();

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

		BigDecimal originalPrice = BigDecimal.valueOf(product.getPrice());

		return promotionProducts.stream().map(PromotionProduct::getPromotion).filter(Promotion::isActive)
				.filter(promotion -> promotion.getStartAt() != null && promotion.getEndAt() != null
						&& !now.isBefore(promotion.getStartAt()) && !now.isAfter(promotion.getEndAt()))
				.max((p1, p2) -> {

					BigDecimal discount1 = calculateDiscount(p1, originalPrice);

					BigDecimal discount2 = calculateDiscount(p2, originalPrice);

					return discount1.compareTo(discount2);
				}).orElse(null);
	}

	public BigDecimal getFinalPriceForProduct(Long productId, BigDecimal originalPrice) {

		Promotion promotion = getActivePromotionForProduct(productId);

		if (promotion == null) {
			return originalPrice;
		}

		return calculateFinalPrice(promotion, originalPrice);
	}
}