package vn.uteexpress.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.uteexpress.entity.Shipment;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByOrderId(Long orderId);
    Optional<Shipment> findByTrackingCode(String trackingCode);
    List<Shipment> findByShipperIdOrderByCreatedAtDesc(Long shipperId);
    List<Shipment> findByShipperIsNullOrderByCreatedAtDesc();
}
