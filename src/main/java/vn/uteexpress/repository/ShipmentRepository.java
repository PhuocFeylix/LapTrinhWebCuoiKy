package vn.uteexpress.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.ShipmentStatus;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

	List<Shipment> findByOrderId(Long orderId);

	Optional<Shipment> findByTrackingCode(String trackingCode);

	List<Shipment> findByShipperIdOrderByCreatedAtDesc(Long shipperId);

	List<Shipment> findByShipperIsNullOrderByCreatedAtDesc();

	Optional<Shipment> findByShopOrderId(Long shopOrderId);

	List<Shipment> findByShipperIdAndStatusOrderByCreatedAtDesc(Long shipperId, ShipmentStatus status);

	List<Shipment> findByShipperIdAndStatusInOrderByCreatedAtDesc(Long shipperId, List<ShipmentStatus> statuses);
}