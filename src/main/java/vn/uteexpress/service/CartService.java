package vn.uteexpress.service;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.*;
import vn.uteexpress.repository.*;

@Service
public class CartService {
	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final UserRepository userRepository;
	private final ProductRepository productRepository;
	private final PromotionService promotionService;

	public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
			UserRepository userRepository, ProductRepository productRepository, PromotionService promotionService) {

		this.cartRepository = cartRepository;
		this.cartItemRepository = cartItemRepository;
		this.userRepository = userRepository;
		this.productRepository = productRepository;
		this.promotionService = promotionService;
	}

	@Transactional
	public Cart getOrCreateCart(Long userId) {
		return cartRepository.findByUserId(userId).orElseGet(() -> {
			User user = userRepository.findById(userId)
					.orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
			Cart cart = new Cart();
			cart.setUser(user);
			cart.setTotalAmount(BigDecimal.ZERO);
			return cartRepository.save(cart);
		});
	}

	@Transactional
	public Cart addItem(Long userId, Long productId, int quantity) {
		if (quantity <= 0)
			throw new IllegalArgumentException("Số lượng phải lớn hơn 0");

		Cart cart = getOrCreateCart(userId);
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

		validateProduct(product);

		CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId).orElseGet(() -> {
			CartItem newItem = new CartItem();
			newItem.setCart(cart);
			newItem.setProduct(product);
			newItem.setQuantity(0);
			return newItem;
		});

		int newQuantity = item.getQuantity() + quantity;
		if (newQuantity > product.getStock())
			throw new IllegalArgumentException("Số lượng vượt quá tồn kho");

		item.setQuantity(newQuantity);
		BigDecimal finalPrice = promotionService.getFinalPriceForProduct(product.getId(),
				BigDecimal.valueOf(product.getPrice()));
		item.setUnitPrice(finalPrice);
		cartItemRepository.save(item);

		recalculateTotal(cart);
		return cartRepository.save(cart);
	}

	@Transactional
	public Cart updateItem(Long userId, Long productId, int quantity) {
		if (quantity <= 0)
			throw new IllegalArgumentException("Số lượng phải lớn hơn 0");

		Cart cart = getOrCreateCart(userId);
		CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
				.orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng"));

		validateProduct(item.getProduct());

		if (quantity > item.getProduct().getStock())
			throw new IllegalArgumentException("Số lượng vượt quá tồn kho");

		item.setQuantity(quantity);
		BigDecimal finalPrice = promotionService.getFinalPriceForProduct(item.getProduct().getId(),
				BigDecimal.valueOf(item.getProduct().getPrice()));

		item.setUnitPrice(finalPrice);
		cartItemRepository.save(item);

		recalculateTotal(cart);
		return cartRepository.save(cart);
	}

	@Transactional
	public Cart removeItem(Long userId, Long productId) {
		Cart cart = getOrCreateCart(userId);
		CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
				.orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng"));

		cartItemRepository.delete(item);
		cart.getItems().remove(item);
		recalculateTotal(cart);
		return cartRepository.save(cart);
	}

	@Transactional
	public Cart clearCart(Long userId) {
		Cart cart = getOrCreateCart(userId);
		cart.getItems().clear();
		cart.setTotalAmount(BigDecimal.ZERO);
		return cartRepository.save(cart);
	}

	private void recalculateTotal(Cart cart) {
		BigDecimal total = cart.getItems().stream().map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
		cart.setTotalAmount(total);
	}

	private void validateProduct(Product product) {
		if (!product.isActive())
			throw new IllegalArgumentException("Sản phẩm đã ngừng kinh doanh");
		if (product.getStock() <= 0)
			throw new IllegalArgumentException("Sản phẩm đã hết hàng");
	}
}
