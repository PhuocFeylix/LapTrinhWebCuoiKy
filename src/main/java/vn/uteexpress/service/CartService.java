package vn.uteexpress.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.Cart;
import vn.uteexpress.entity.CartItem;
import vn.uteexpress.entity.Product;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.CartItemRepository;
import vn.uteexpress.repository.CartRepository;
import vn.uteexpress.repository.ProductRepository;
import vn.uteexpress.repository.UserRepository;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = findUser(userId);
            return cartRepository.save(new Cart(user));
        });
    }

    public Cart getCart(Long userId) {
        return getOrCreateCart(userId);
    }

    @Transactional
    public Cart addItem(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }

        Cart cart = getOrCreateCart(userId);
        Product product = findProduct(productId);
        validateProduct(product);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProduct(product);
                    return newItem;
                });

        int newQuantity = item.getId() == null
                ? quantity
                : item.getQuantity() + quantity;

        validateQuantity(newQuantity, product);

        item.setQuantity(newQuantity);
        item.setUnitPrice(BigDecimal.valueOf(product.getPrice()));

        cartItemRepository.save(item);
        recalculateTotal(cart);

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateItem(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }

        Cart cart = getCart(userId);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() ->
                        new RuntimeException("Sản phẩm không có trong giỏ hàng"));

        Product product = findProduct(productId);
        validateProduct(product);
        validateQuantity(quantity, product);

        item.setQuantity(quantity);
        item.setUnitPrice(BigDecimal.valueOf(product.getPrice()));

        cartItemRepository.save(item);
        recalculateTotal(cart);

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItem(Long userId, Long productId) {
        Cart cart = getCart(userId);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() ->
                        new RuntimeException("Sản phẩm không có trong giỏ hàng"));

        cartItemRepository.delete(item);
        recalculateTotal(cart);

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart clearCart(Long userId) {
        Cart cart = getCart(userId);
        cart.getItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);

        return cartRepository.save(cart);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy người dùng với ID: " + userId));
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));
    }

    private void validateProduct(Product product) {
        if (!product.isActive()) {
            throw new IllegalArgumentException("Sản phẩm đang ngừng kinh doanh");
        }
    }

    private void validateQuantity(int quantity, Product product) {
        if (quantity > product.getStock()) {
            throw new IllegalArgumentException(
                    "Số lượng vượt quá tồn kho. Tồn kho hiện tại: " + product.getStock());
        }
    }

    private void recalculateTotal(Cart cart) {
        BigDecimal total = cart.getItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalAmount(total);
    }
}
