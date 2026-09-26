package vn.uteexpress.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.Address;
import vn.uteexpress.entity.Cart;
import vn.uteexpress.entity.CartItem;
import vn.uteexpress.entity.Coupon;
import vn.uteexpress.entity.Order;
import vn.uteexpress.entity.OrderItem;
import vn.uteexpress.entity.OrderStatus;
import vn.uteexpress.entity.Product;
import vn.uteexpress.entity.ShopOrder;
import vn.uteexpress.entity.ShopOrderItem;
import vn.uteexpress.entity.ShopOrderStatus;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.AddressRepository;
import vn.uteexpress.repository.CartRepository;
import vn.uteexpress.repository.OrderRepository;
import vn.uteexpress.repository.ShopOrderRepository;
import vn.uteexpress.repository.UserRepository;

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

	// =========================================================
	// CHECKOUT
	// =========================================================

	@Transactional
	public Order checkout(Long userId, Long addressId, String couponCode) {

		if (userId == null) {
			throw new IllegalArgumentException("User ID không được null");
		}

		if (addressId == null) {
			throw new IllegalArgumentException("Address ID không được null");
		}

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

		Address address = addressRepository.findByIdAndUserId(addressId, userId)
				.orElseThrow(() -> new IllegalArgumentException("Địa chỉ giao hàng không thuộc người dùng"));

		Cart cart = cartRepository.findByUserId(userId)
				.orElseThrow(() -> new IllegalArgumentException("Người dùng chưa có giỏ hàng"));

		if (cart.getItems() == null || cart.getItems().isEmpty()) {

			throw new IllegalArgumentException("Không thể đặt hàng khi giỏ hàng đang trống");
		}

		Order order = new Order();

		order.setUser(user);
		order.setShippingAddress(address);
		order.setStatus(OrderStatus.PENDING);

		BigDecimal subtotal = BigDecimal.ZERO;

		Map<Long, ShopOrder> shopOrders = new HashMap<>();

		// =====================================================
		// PROCESS CART
		// =====================================================

		for (CartItem cartItem : cart.getItems()) {

			if (cartItem == null) {
				throw new IllegalArgumentException("Cart chứa CartItem không hợp lệ");
			}

			Product product = cartItem.getProduct();

			if (product == null) {
				throw new IllegalArgumentException("Cart chứa sản phẩm không hợp lệ");
			}

			// =========================
			// PRODUCT VALIDATION
			// =========================

			if (!product.isActive()) {

				throw new IllegalArgumentException("Sản phẩm đã ngừng kinh doanh: " + product.getName());
			}

			if (product.getShop() == null) {

				throw new IllegalArgumentException("Sản phẩm chưa thuộc Shop: " + product.getName());
			}

			/*
			 * Shop đã bị ngừng hoạt động thì không cho tạo đơn mới.
			 */
			if (!product.getShop().isActive()) {

				throw new IllegalArgumentException("Shop của sản phẩm đã ngừng hoạt động: " + product.getName());
			}

			if (cartItem.getQuantity() <= 0) {

				throw new IllegalArgumentException("Số lượng sản phẩm không hợp lệ: " + product.getName());
			}

			if (product.getStock() < 0) {

				throw new IllegalArgumentException("Tồn kho sản phẩm không hợp lệ: " + product.getName());
			}

			if (cartItem.getQuantity() > product.getStock()) {

				throw new IllegalArgumentException("Sản phẩm không đủ tồn kho: " + product.getName());
			}

			// =================================================
			// CALCULATE CURRENT PROMOTION PRICE
			// =================================================

			/*
			 * Không dùng cartItem.getUnitPrice() làm giá cuối cùng vì giá trong Cart có thể
			 * đã được tính từ trước khi checkout.
			 *
			 * Giá Promotion được tính lại tại thời điểm checkout.
			 */
			BigDecimal originalPrice = BigDecimal.valueOf(product.getPrice());

			if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) < 0) {

				throw new IllegalArgumentException("Giá sản phẩm không hợp lệ: " + product.getName());
			}

			BigDecimal finalPrice = promotionService.getFinalPriceForProduct(product.getId(), originalPrice);

			if (finalPrice == null || finalPrice.compareTo(BigDecimal.ZERO) < 0) {

				throw new IllegalArgumentException("Giá sau khuyến mãi không hợp lệ: " + product.getName());
			}

			// =================================================
			// TRỪ TỒN KHO
			// =================================================

			product.setStock(product.getStock() - cartItem.getQuantity());

			// =================================================
			// ORDER ITEM
			// =================================================

			OrderItem orderItem = new OrderItem();

			orderItem.setProduct(product);

			orderItem.setProductName(product.getName());

			orderItem.setUnitPrice(finalPrice);

			orderItem.setQuantity(cartItem.getQuantity());

			order.addItem(orderItem);

			subtotal = subtotal.add(orderItem.getSubtotal());

			// =================================================
			// SHOP ORDER
			// =================================================

			Long shopId = product.getShop().getId();

			if (shopId == null) {

				throw new IllegalArgumentException("Shop ID không hợp lệ");
			}

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

			// =================================================
			// SHOP ORDER ITEM
			// =================================================

			ShopOrderItem shopOrderItem = new ShopOrderItem();

			shopOrderItem.setShopOrder(shopOrder);

			shopOrderItem.setProduct(product);

			shopOrderItem.setProductName(product.getName());

			/*
			 * Phải dùng cùng finalPrice với OrderItem để tổng tiền ShopOrder khớp với
			 * Order.
			 */
			shopOrderItem.setUnitPrice(finalPrice);

			shopOrderItem.setQuantity(cartItem.getQuantity());

			shopOrder.getItems().add(shopOrderItem);

			shopOrder.setSubtotalAmount(shopOrder.getSubtotalAmount().add(shopOrderItem.getSubtotal()));
		}

		// =====================================================
		// COUPON
		// =====================================================

		BigDecimal discount = BigDecimal.ZERO;

		if (couponCode != null && !couponCode.trim().isEmpty()) {

			String normalizedCouponCode = couponCode.trim();

			Coupon coupon = couponService.getCouponByCode(normalizedCouponCode);

			if (coupon == null) {

				throw new IllegalArgumentException("Coupon không tồn tại");
			}

			/*
			 * calculateDiscount() tự kiểm tra: - active - thời gian - usage limit - minimum
			 * order amount
			 */
			discount = couponService.calculateDiscount(coupon, subtotal);

			if (discount == null || discount.compareTo(BigDecimal.ZERO) < 0) {

				throw new IllegalArgumentException("Giá trị giảm giá không hợp lệ");
			}

			order.setCouponCode(coupon.getCode());

			/*
			 * Transaction hiện tại sẽ rollback nếu checkout thất bại.
			 */
			couponService.increaseUsedCount(coupon);
		}

		// =====================================================
		// TOTAL
		// =====================================================

		BigDecimal total = subtotal.subtract(discount);

		if (total.compareTo(BigDecimal.ZERO) < 0) {

			total = BigDecimal.ZERO;
		}

		order.setSubtotalAmount(subtotal);

		order.setDiscountAmount(discount);

		order.setTotalAmount(total);

		// =====================================================
		// SAVE ORDER
		// =====================================================

		Order savedOrder = orderRepository.save(order);

		// =====================================================
		// CLEAR CART
		// =====================================================

		cart.getItems().clear();

		cart.setTotalAmount(BigDecimal.ZERO);

		cartRepository.save(cart);

		return savedOrder;
	}

	// =========================================================
	// GET ORDERS BY USER
	// =========================================================

	@Transactional(readOnly = true)
	public List<Order> getOrdersByUser(Long userId) {

		if (userId == null) {
			throw new IllegalArgumentException("User ID không được null");
		}

		return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
	}

	// =========================================================
	// GET ORDERS BY USER + STATUS
	// =========================================================

	@Transactional(readOnly = true)
	public List<Order> getOrdersByUserAndStatus(Long userId, OrderStatus status) {

		if (userId == null) {
			throw new IllegalArgumentException("User ID không được null");
		}

		if (status == null) {

			return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
		}

		return orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
	}

	// =========================================================
	// GET ORDER
	// =========================================================

	@Transactional(readOnly = true)
	public Order getOrder(Long orderId) {

		if (orderId == null) {
			throw new IllegalArgumentException("Order ID không được null");
		}

		return orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
	}

	// =========================================================
	// CANCEL ORDER
	// =========================================================

	@Transactional
	public Order cancelOrder(Long orderId, Long userId) {

		if (orderId == null) {
			throw new IllegalArgumentException("Order ID không được null");
		}

		if (userId == null) {
			throw new IllegalArgumentException("User ID không được null");
		}

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

		// =====================================================
		// OWNERSHIP
		// =====================================================

		if (order.getUser() == null || !order.getUser().getId().equals(userId)) {

			throw new IllegalArgumentException("Đơn hàng không thuộc người dùng này");
		}

		// =====================================================
		// CHECK STATUS
		// =====================================================

		if (order.getStatus() == OrderStatus.CANCELLED) {

			throw new IllegalArgumentException("Đơn hàng đã được hủy trước đó");
		}

		/*
		 * Chỉ cho phép hủy PENDING / CONFIRMED.
		 */
		if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {

			throw new IllegalArgumentException("Không thể hủy đơn hàng ở trạng thái " + order.getStatus());
		}

		// =====================================================
		// HOÀN TỒN KHO
		// =====================================================

		for (OrderItem item : order.getItems()) {

			Product product = item.getProduct();

			if (product != null) {

				product.setStock(product.getStock() + item.getQuantity());
			}
		}

		// =====================================================
		// HỦY SHOP ORDER
		// =====================================================

		if (order.getShopOrders() != null) {

			for (ShopOrder shopOrder : order.getShopOrders()) {

				if (shopOrder.getStatus() != ShopOrderStatus.DELIVERED
						&& shopOrder.getStatus() != ShopOrderStatus.SHIPPING) {

					shopOrder.setStatus(ShopOrderStatus.CANCELLED);
				}
			}
		}

		// =====================================================
		// HOÀN LƯỢT COUPON
		// =====================================================

		if (order.getCouponCode() != null && !order.getCouponCode().trim().isEmpty()) {

			Coupon coupon = couponService.getCouponByCode(order.getCouponCode());

			if (coupon != null) {

				couponService.decreaseUsedCount(coupon);
			}
		}

		// =====================================================
		// CANCEL ORDER
		// =====================================================

		order.setStatus(OrderStatus.CANCELLED);

		return orderRepository.save(order);
	}

	// =========================================================
	// GET SHOP ORDERS OF USER ORDER
	// =========================================================

	@Transactional(readOnly = true)
	public List<ShopOrder> getShopOrdersByUser(Long orderId, Long userId) {

		if (orderId == null) {
			throw new IllegalArgumentException("Order ID không được null");
		}

		if (userId == null) {
			throw new IllegalArgumentException("User ID không được null");
		}

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));

		if (order.getUser() == null || !order.getUser().getId().equals(userId)) {

			throw new IllegalArgumentException("Đơn hàng không thuộc người dùng này");
		}

		return shopOrderRepository.findByOrderId(orderId);
	}
}