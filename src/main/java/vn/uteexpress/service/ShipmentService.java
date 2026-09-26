package vn.uteexpress.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.*;
import vn.uteexpress.repository.*;

@Service
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ShipmentService(ShipmentRepository shipmentRepository,
            OrderRepository orderRepository, UserRepository userRepository) {
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Shipment createShipment(Long orderId) {
        Order order = findOrder(orderId);
        if (order.getStatus() == OrderStatus.CANCELLED)
            throw new IllegalArgumentException("Không thể tạo vận đơn cho đơn hàng đã hủy");
        if (shipmentRepository.findByOrderId(orderId).isPresent())
            throw new IllegalArgumentException("Đơn hàng đã có vận đơn");

        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setTrackingCode(generateTrackingCode());
        shipment.setStatus(ShipmentStatus.READY);
        return shipmentRepository.save(shipment);
    }

    @Transactional
    public Shipment assignShipper(Long shipmentId, Long shipperId) {
        Shipment shipment = findShipment(shipmentId);
        User shipper = findShipper(shipperId);

        if (shipment.getStatus() == ShipmentStatus.DELIVERED ||
                shipment.getStatus() == ShipmentStatus.CANCELLED)
            throw new IllegalArgumentException("Không thể phân công vận đơn ở trạng thái hiện tại");

        shipment.setShipper(shipper);
        shipment.setStatus(ShipmentStatus.ASSIGNED);
        shipment.setAssignedAt(LocalDateTime.now());
        return shipmentRepository.save(shipment);
    }

    @Transactional
    public Shipment updateStatus(Long shipmentId, ShipmentStatus newStatus) {
        Shipment shipment = findShipment(shipmentId);
        if (newStatus == null)
            throw new IllegalArgumentException("Trạng thái vận đơn không được null");

        validateStatusTransition(shipment.getStatus(), newStatus);
        shipment.setStatus(newStatus);

        if (newStatus == ShipmentStatus.PICKED_UP)
            shipment.setPickedUpAt(LocalDateTime.now());
        if (newStatus == ShipmentStatus.DELIVERED)
            shipment.setDeliveredAt(LocalDateTime.now());

        Order order = shipment.getOrder();
        switch (newStatus) {
            case PICKED_UP, DELIVERING -> order.setStatus(OrderStatus.SHIPPING);
            case DELIVERED -> order.setStatus(OrderStatus.DELIVERED);
            case CANCELLED -> order.setStatus(OrderStatus.CANCELLED);
            default -> {}
        }
        orderRepository.save(order);
        return shipmentRepository.save(shipment);
    }

    public Shipment getById(Long id) { return findShipment(id); }

    public Shipment getByOrderId(Long orderId) {
        return shipmentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Đơn hàng chưa có vận đơn"));
    }

    public Shipment getByTrackingCode(String code) {
        return shipmentRepository.findByTrackingCode(code)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy vận đơn với mã: " + code));
    }

    public List<Shipment> getShipmentsByShipper(Long shipperId) {
        return shipmentRepository.findByShipperIdOrderByCreatedAtDesc(shipperId);
    }

    public List<Shipment> getUnassignedShipments() {
        return shipmentRepository.findByShipperIsNullOrderByCreatedAtDesc();
    }

    private Shipment findShipment(Long id) {
        return shipmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy vận đơn với ID: " + id));
    }

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + id));
    }

    private User findShipper(Long id) {
        User shipper = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy shipper"));
        if (shipper.getRole() == null || !"SHIPPER".equalsIgnoreCase(shipper.getRole().getName()))
            throw new IllegalArgumentException("User được chọn không có role SHIPPER");
        if (!shipper.isEnabled())
            throw new IllegalArgumentException("Tài khoản shipper đang bị khóa");
        return shipper;
    }

    private String generateTrackingCode() {
        String code;
        do {
            code = "UTE" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 12).toUpperCase();
        } while (shipmentRepository.findByTrackingCode(code).isPresent());
        return code;
    }

    private void validateStatusTransition(ShipmentStatus current, ShipmentStatus next) {
        if (current == ShipmentStatus.CANCELLED || current == ShipmentStatus.DELIVERED)
            throw new IllegalArgumentException("Vận đơn đã kết thúc, không thể thay đổi trạng thái");

        if (next == ShipmentStatus.ASSIGNED && current != ShipmentStatus.READY)
            throw new IllegalArgumentException("Chỉ READY mới chuyển sang ASSIGNED");
        if (next == ShipmentStatus.PICKED_UP && current != ShipmentStatus.ASSIGNED)
            throw new IllegalArgumentException("Chỉ ASSIGNED mới chuyển sang PICKED_UP");
        if (next == ShipmentStatus.DELIVERING && current != ShipmentStatus.PICKED_UP)
            throw new IllegalArgumentException("Chỉ PICKED_UP mới chuyển sang DELIVERING");
        if (next == ShipmentStatus.DELIVERED && current != ShipmentStatus.DELIVERING)
            throw new IllegalArgumentException("Chỉ DELIVERING mới chuyển sang DELIVERED");
        if (next == ShipmentStatus.FAILED &&
            current != ShipmentStatus.ASSIGNED &&
            current != ShipmentStatus.PICKED_UP &&
            current != ShipmentStatus.DELIVERING)
            throw new IllegalArgumentException("Không thể đánh dấu FAILED ở trạng thái hiện tại");
    }
}
