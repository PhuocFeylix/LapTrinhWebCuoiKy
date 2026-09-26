package vn.uteexpress.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.uteexpress.entity.Order;
import vn.uteexpress.entity.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {

	List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

	@Query("""
			    SELECT COUNT(o) > 0
			    FROM Order o
			    JOIN o.items oi
			    WHERE o.user.id = :userId
			    AND oi.product.id = :productId
			    AND o.status = :status
			""")
	boolean existsDeliveredOrderForProduct(@Param("userId") Long userId, @Param("productId") Long productId,
			@Param("status") OrderStatus status);
}