package vn.uteexpress.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.ShopOrder;
import vn.uteexpress.entity.ShopOrderStatus;
import vn.uteexpress.repository.ShopOrderRepository;

@Service
public class VendorShopOrderService {

	private final ShopOrderRepository shopOrderRepository;

	public VendorShopOrderService(ShopOrderRepository shopOrderRepository) {

		this.shopOrderRepository = shopOrderRepository;
	}

	@Transactional(readOnly = true)
	public List<ShopOrder> getShopOrders(Long shopId) {

		return shopOrderRepository.findByShopIdOrderByCreatedAtDesc(shopId);
	}

	@Transactional(readOnly = true)
	public List<ShopOrder> getShopOrdersByStatus(Long shopId, ShopOrderStatus status) {

		return shopOrderRepository.findByShopIdAndStatusOrderByCreatedAtDesc(shopId, status);
	}

	@Transactional(readOnly = true)
	public ShopOrder getShopOrder(Long shopId, Long shopOrderId) {

		return shopOrderRepository.findByIdAndShopId(shopOrderId, shopId)
				.orElseThrow(() -> new IllegalArgumentException("ShopOrder không thuộc Shop này"));
	}

	@Transactional
	public ShopOrder updateStatus(Long shopId, Long shopOrderId, ShopOrderStatus newStatus) {

		ShopOrder shopOrder = getShopOrder(shopId, shopOrderId);

		ShopOrderStatus currentStatus = shopOrder.getStatus();

		validateStatusTransition(currentStatus, newStatus);

		shopOrder.setStatus(newStatus);

		return shopOrderRepository.save(shopOrder);
	}

	/**
	 * Vendor chỉ được quản lý trạng thái đến READY_TO_SHIP.
	 *
	 * Sau READY_TO_SHIP: Shipment/Shipper sẽ quản lý.
	 */
	private void validateStatusTransition(ShopOrderStatus current, ShopOrderStatus next) {

		if (next == null) {
			throw new IllegalArgumentException("Trạng thái mới không được null");
		}

		if (current == next) {
			throw new IllegalArgumentException("Trạng thái mới giống trạng thái hiện tại");
		}

		if (current == ShopOrderStatus.CANCELLED) {
			throw new IllegalArgumentException("Đơn đã bị hủy, không thể thay đổi trạng thái");
		}

		if (current == ShopOrderStatus.DELIVERED) {
			throw new IllegalArgumentException("Đơn đã giao thành công, không thể thay đổi trạng thái");
		}

		/*
		 * Sau khi đã READY_TO_SHIP, Vendor không được tự chuyển sang
		 * SHIPPING/DELIVERED.
		 */
		if (current == ShopOrderStatus.READY_TO_SHIP) {
			throw new IllegalArgumentException(
					"ShopOrder đã READY_TO_SHIP. " + "Hãy tạo Shipment để Shipper tiếp tục xử lý.");
		}

		if (current == ShopOrderStatus.SHIPPING) {
			throw new IllegalArgumentException("ShopOrder đang được Shipper xử lý.");
		}

		boolean valid = false;

		switch (current) {

		case PENDING:

			valid = next == ShopOrderStatus.CONFIRMED || next == ShopOrderStatus.CANCELLED;

			break;

		case CONFIRMED:

			valid = next == ShopOrderStatus.PREPARING || next == ShopOrderStatus.CANCELLED;

			break;

		case PREPARING:

			valid = next == ShopOrderStatus.READY_TO_SHIP;

			break;

		default:
			break;
		}

		if (!valid) {

			throw new IllegalArgumentException("Không thể chuyển trạng thái từ " + current + " sang " + next);
		}
	}
}