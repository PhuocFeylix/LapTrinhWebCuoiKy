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

	@Query("""
			    SELECT DISTINCT o
			    FROM Order o
			    JOIN o.items oi
			    JOIN oi.product p
			    JOIN p.shop s
			    WHERE s.vendor.id = :vendorId
			    ORDER BY o.createdAt DESC
			""")
	List<Order> findOrdersByVendorId(@Param("vendorId") Long vendorId);

	@Query("""
			    SELECT DISTINCT o
			    FROM Order o
			    JOIN o.items oi
			    JOIN oi.product p
			    JOIN p.shop s
			    WHERE s.vendor.id = :vendorId
			    AND o.status = :status
			    ORDER BY o.createdAt DESC
			""")
	List<Order> findOrdersByVendorIdAndStatus(@Param("vendorId") Long vendorId, @Param("status") OrderStatus status);
}