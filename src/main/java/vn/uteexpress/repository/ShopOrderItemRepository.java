package vn.uteexpress.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.uteexpress.entity.ShopOrderItem;

public interface ShopOrderItemRepository extends JpaRepository<ShopOrderItem, Long> {

	List<ShopOrderItem> findByShopOrderId(Long shopOrderId);
}