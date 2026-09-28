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

<<<<<<< HEAD
	Optional<Shipment> findByShopOrderId(Long shopOrderId);
=======
	Optional<Shipment> findByShopOrder_Id(Long shopOrderId);
>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)

	List<Shipment> findByShipperIdAndStatusOrderByCreatedAtDesc(Long shipperId, ShipmentStatus status);

	List<Shipment> findByShipperIdAndStatusInOrderByCreatedAtDesc(Long shipperId, List<ShipmentStatus> statuses);
}