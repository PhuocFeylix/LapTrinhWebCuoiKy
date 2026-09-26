package vn.uteexpress.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.uteexpress.entity.ShopOrder;
import vn.uteexpress.entity.ShopOrderStatus;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {

	List<ShopOrder> findByShopIdOrderByCreatedAtDesc(Long shopId);

	List<ShopOrder> findByShopIdAndStatusOrderByCreatedAtDesc(Long shopId, ShopOrderStatus status);

	Optional<ShopOrder> findByIdAndShopId(Long id, Long shopId);

	List<ShopOrder> findByOrderId(Long orderId);
	List<ShopOrder> findByStatusOrderByCreatedAtDesc(ShopOrderStatus status);
}