package vn.uteexpress.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
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
	private final ShopOrderRepository shopOrderRepository;

	public OrderService(OrderRepository orderRepository, CartRepository cartRepository,
			AddressRepository addressRepository, UserRepository userRepository, PromotionService promotionService,
			CouponService couponService, ShopOrderRepository shopOrderRepository) {

		this.orderRepository = orderRepository;
		this.cartRepository = cartRepository;
		this.addressRepository = addressRepository;
		this.userRepository = userRepository;
		this.promotionService = promotionService;
		this.couponService = couponService;
		this.shopOrderRepository = shopOrderRepository;
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

		Map<Long, ShopOrder> shopOrders = new HashMap<>();

		for (CartItem cartItem : cart.getItems()) {

			Product product = cartItem.getProduct();

			if (!product.isActive()) {
				throw new IllegalArgumentException("Sản phẩm đã ngừng kinh doanh: " + product.getName());
			}

			if (cartItem.getQuantity() > product.getStock()) {
				throw new IllegalArgumentException("Sản phẩm không đủ tồn kho: " + product.getName());
			}

			if (product.getShop() == null) {
				throw new IllegalArgumentException("Sản phẩm chưa thuộc Shop: " + product.getName());
			}

			product.setStock(product.getStock() - cartItem.getQuantity());

			// =========================
			// ORDER ITEM CŨ
			// =========================

			OrderItem orderItem = new OrderItem();

			orderItem.setProduct(product);
			orderItem.setProductName(product.getName());
			orderItem.setUnitPrice(cartItem.getUnitPrice());
			orderItem.setQuantity(cartItem.getQuantity());

			order.addItem(orderItem);

			subtotal = subtotal.add(orderItem.getSubtotal());

			// =========================
			// SHOP ORDER
			// =========================

			Long shopId = product.getShop().getId();

			ShopOrder shopOrder = shopOrders.get(shopId);

			if (shopOrder == null) {

				shopOrder = new ShopOrder();

				shopOrder.setOrder(order);
				shopOrder.setShop(product.getShop());
				shopOrder.setStatus(ShopOrderStatus.PENDING);
				shopOrder.setSubtotalAmount(BigDecimal.ZERO);

				shopOrders.put(shopId, shopOrder);
				order.getShopOrders().add(shopOrder);
			}

			// =========================
			// SHOP ORDER ITEM
			// =========================

			ShopOrderItem shopOrderItem = new ShopOrderItem();

			shopOrderItem.setShopOrder(shopOrder);
			shopOrderItem.setProduct(product);
			shopOrderItem.setProductName(product.getName());
			shopOrderItem.setUnitPrice(cartItem.getUnitPrice());
			shopOrderItem.setQuantity(cartItem.getQuantity());

			shopOrder.getItems().add(shopOrderItem);

			shopOrder.setSubtotalAmount(shopOrder.getSubtotalAmount().add(shopOrderItem.getSubtotal()));
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
