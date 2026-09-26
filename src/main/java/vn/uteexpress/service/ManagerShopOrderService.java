package vn.uteexpress.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.ShopOrder;
import vn.uteexpress.entity.ShopOrderStatus;
import vn.uteexpress.repository.ShopOrderRepository;

@Service
public class ManagerShopOrderService {

	private final ShopOrderRepository shopOrderRepository;

	public ManagerShopOrderService(ShopOrderRepository shopOrderRepository) {
		this.shopOrderRepository = shopOrderRepository;
	}

	@Transactional(readOnly = true)
	public List<ShopOrder> getShopOrdersByStatus(ShopOrderStatus status) {

		if (status == null) {
			return shopOrderRepository.findAll();
		}

		return shopOrderRepository.findByStatusOrderByCreatedAtDesc(status);
	}

	@Transactional(readOnly = true)
	public List<ShopOrder> getReadyToShipOrders() {
		return shopOrderRepository.findByStatusOrderByCreatedAtDesc(ShopOrderStatus.READY_TO_SHIP);
	}

	@Transactional(readOnly = true)
	public ShopOrder getShopOrder(Long shopOrderId) {

		return shopOrderRepository.findById(shopOrderId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ShopOrder với ID: " + shopOrderId));
	}
}