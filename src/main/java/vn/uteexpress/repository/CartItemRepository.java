package vn.uteexpress.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.uteexpress.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
}
