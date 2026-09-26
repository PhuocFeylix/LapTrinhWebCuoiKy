package vn.uteexpress.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.*;
import vn.uteexpress.repository.*;
import vn.uteexpress.service.CouponService;

@Service
public class OrderService {
	private final OrderRepository orderRepository;
	private final CartRepository cartRepository;
	private final AddressRepository addressRepository;
	private final UserRepository userRepository;
	private final PromotionService promotionService;
	private final CouponService couponService;

	public OrderService(OrderRepository orderRepository, CartRepository cartRepository,
			AddressRepository addressRepository, UserRepository userRepository, PromotionService promotionService,
			CouponService couponService) {

		this.orderRepository = orderRepository;
		this.cartRepository = cartRepository;
		this.addressRepository = addressRepository;
		this.userRepository = userRepository;
		this.promotionService = promotionService;
		this.couponService = couponService;
	}

	@Transactional
	public Order checkout(Long userId, Long addressId, String couponCode) {

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

		Address address = addressRepository.findByIdAndUserId(addressId, userId)
				.orElseThrow(() -> new IllegalArgumentException("Địa chỉ giao hàng không thuộc người dùng"));

		Cart cart = cartRepository.findByUserId(userId)
				.orElseThrow(() -> new IllegalArgumentException("Người dùng chưa có giỏ hàng"));

		if (cart.getItems().isEmpty()) {
			throw new IllegalArgumentException("Không thể đặt hàng khi giỏ hàng đang trống");
		}

		Order order = new Order();

		order.setUser(user);
		order.setShippingAddress(address);
		order.setStatus(OrderStatus.PENDING);

		BigDecimal subtotal = BigDecimal.ZERO;

		// =========================
		// TẠO ORDER ITEM
		// =========================

		for (CartItem cartItem : cart.getItems()) {

			Product product = cartItem.getProduct();

			if (!product.isActive()) {
				throw new IllegalArgumentException("Sản phẩm đã ngừng kinh doanh: " + product.getName());
			}

			if (cartItem.getQuantity() > product.getStock()) {
				throw new IllegalArgumentException("Sản phẩm không đủ tồn kho: " + product.getName());
			}

			product.setStock(product.getStock() - cartItem.getQuantity());

			OrderItem orderItem = new OrderItem();

			orderItem.setProduct(product);
			orderItem.setProductName(product.getName());

			// Giá đã bao gồm Promotion
			orderItem.setUnitPrice(cartItem.getUnitPrice());

			orderItem.setQuantity(cartItem.getQuantity());

			order.addItem(orderItem);

			subtotal = subtotal.add(orderItem.getSubtotal());
		}

		// =========================
		// COUPON
		// =========================

		BigDecimal discount = BigDecimal.ZERO;

		if (couponCode != null && !couponCode.trim().isEmpty()) {

			Coupon coupon = couponService.getCouponByCode(couponCode.trim());

			if (coupon == null) {
				throw new IllegalArgumentException("Coupon không tồn tại");
			}

			discount = couponService.calculateDiscount(coupon, subtotal);

			order.setCouponCode(coupon.getCode());

			// Tăng lượt sử dụng
			couponService.increaseUsedCount(coupon);
		}

		// =========================
		// TÍNH TỔNG
		// =========================

		BigDecimal total = subtotal.subtract(discount);

		if (total.compareTo(BigDecimal.ZERO) < 0) {
			total = BigDecimal.ZERO;
		}

		order.setSubtotalAmount(subtotal);
		order.setDiscountAmount(discount);
		order.setTotalAmount(total);

		// =========================
		// SAVE ORDER
		// =========================

		Order savedOrder = orderRepository.save(order);

		// =========================
		// CLEAR CART
		// =========================

		cart.getItems().clear();
		cart.setTotalAmount(BigDecimal.ZERO);

		cartRepository.save(cart);

		return savedOrder;
	}

	public List<Order> getOrdersByUser(Long userId) {
		return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
	}

	public Order getOrder(Long orderId) {
		return orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
	}
}
