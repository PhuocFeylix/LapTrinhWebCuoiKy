package vn.uteexpress.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.ShipmentStatus;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.ShipmentRepository;
import vn.uteexpress.repository.UserRepository;

@Service
public class ShipperService {

	private final ShipmentRepository shipmentRepository;
	private final UserRepository userRepository;
	private final ShipmentService shipmentService;

	public ShipperService(ShipmentRepository shipmentRepository, UserRepository userRepository,
			ShipmentService shipmentService) {

		this.shipmentRepository = shipmentRepository;
		this.userRepository = userRepository;
		this.shipmentService = shipmentService;
	}

<<<<<<< HEAD
	/**
	 * Lấy Shipper và kiểm tra tài khoản.
	 */
	private User findShipper(Long shipperId) {

=======
	// =========================================================
	// FIND SHIPPER
	// =========================================================

	private User findShipper(Long shipperId) {

		if (shipperId == null) {

			throw new IllegalArgumentException("Shipper ID không được null");
		}

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
		User shipper = userRepository.findById(shipperId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Shipper"));

		if (shipper.getRole() == null || !"SHIPPER".equalsIgnoreCase(shipper.getRole().getName())) {

			throw new IllegalArgumentException("User này không có quyền Shipper");
		}

		if (!shipper.isEnabled()) {

			throw new IllegalArgumentException("Tài khoản Shipper đang bị khóa");
		}

		return shipper;
	}

<<<<<<< HEAD
	/**
	 * Lấy toàn bộ Shipment của Shipper.
	 */
=======
	// =========================================================
	// GET ALL SHIPMENTS
	// =========================================================

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
	@Transactional(readOnly = true)
	public List<Shipment> getShipments(Long shipperId) {

		findShipper(shipperId);

		return shipmentRepository.findByShipperIdOrderByCreatedAtDesc(shipperId);
	}

<<<<<<< HEAD
	/**
	 * Lấy các Shipment đang được giao cho Shipper.
	 *
	 * Bao gồm: ASSIGNED PICKED_UP DELIVERING
	 */
=======
	// =========================================================
	// GET ACTIVE SHIPMENTS
	// =========================================================

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
	@Transactional(readOnly = true)
	public List<Shipment> getActiveShipments(Long shipperId) {

		findShipper(shipperId);

		return shipmentRepository.findByShipperIdAndStatusInOrderByCreatedAtDesc(shipperId,
				List.of(ShipmentStatus.ASSIGNED, ShipmentStatus.PICKED_UP, ShipmentStatus.DELIVERING));
	}

<<<<<<< HEAD
	/**
	 * Lấy các Shipment đã giao thành công.
	 */
=======
	// =========================================================
	// GET DELIVERED
	// =========================================================

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
	@Transactional(readOnly = true)
	public List<Shipment> getDeliveredShipments(Long shipperId) {

		findShipper(shipperId);

		return shipmentRepository.findByShipperIdOrderByCreatedAtDesc(shipperId).stream()
				.filter(shipment -> shipment.getStatus() == ShipmentStatus.DELIVERED).toList();
	}

<<<<<<< HEAD
	/**
	 * Shipper cập nhật trạng thái Shipment.
	 *
	 * Quan trọng: Shipment phải thuộc đúng Shipper.
	 */
=======
	// =========================================================
	// GET FAILED
	// =========================================================

	@Transactional(readOnly = true)
	public List<Shipment> getFailedShipments(Long shipperId) {

		findShipper(shipperId);

		return shipmentRepository.findByShipperIdOrderByCreatedAtDesc(shipperId).stream()
				.filter(shipment -> shipment.getStatus() == ShipmentStatus.FAILED).toList();
	}

	// =========================================================
	// UPDATE SHIPMENT STATUS
	// =========================================================

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
	@Transactional
	public Shipment updateShipmentStatus(Long shipperId, Long shipmentId, ShipmentStatus newStatus) {

		findShipper(shipperId);

<<<<<<< HEAD
		Shipment shipment = shipmentRepository.findById(shipmentId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Shipment"));

		/*
		 * Kiểm tra Shipment có được giao cho đúng Shipper hay không.
		 */
=======
		if (newStatus == null) {

			throw new IllegalArgumentException("Trạng thái mới không được null");
		}

		Shipment shipment = shipmentRepository.findById(shipmentId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Shipment"));

		// -----------------------------------------------------
		// SHIPMENT PHẢI THUỘC SHIPPER
		// -----------------------------------------------------

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
		if (shipment.getShipper() == null || shipment.getShipper().getId() == null
				|| !shipment.getShipper().getId().equals(shipperId)) {

			throw new IllegalArgumentException("Shipment không thuộc Shipper này");
		}

<<<<<<< HEAD
		/*
		 * Shipper chỉ được cập nhật các trạng thái thuộc quy trình giao hàng.
		 */
=======
		// -----------------------------------------------------
		// SHIPPER CHỈ ĐƯỢC CẬP NHẬT CÁC TRẠNG THÁI NÀY
		// -----------------------------------------------------

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
		if (newStatus != ShipmentStatus.PICKED_UP && newStatus != ShipmentStatus.DELIVERING
				&& newStatus != ShipmentStatus.DELIVERED && newStatus != ShipmentStatus.FAILED) {

			throw new IllegalArgumentException("Shipper không được chuyển Shipment sang trạng thái " + newStatus);
		}

<<<<<<< HEAD
		return shipmentService.updateStatus(shipmentId, newStatus);
	}

	/**
	 * Shipper nhận Shipment.
	 *
	 * Thực tế Shipment phải được Admin/Manager assign trước, nên hàm này chỉ kiểm
	 * tra Shipment đã thuộc Shipper.
	 */
=======
		// -----------------------------------------------------
		// DELEGATE TO SHIPMENT SERVICE
		// -----------------------------------------------------

		return shipmentService.updateStatus(shipmentId, newStatus);
	}

	// =========================================================
	// GET SHIPMENT DETAIL
	// =========================================================

>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
	@Transactional(readOnly = true)
	public Shipment getShipment(Long shipperId, Long shipmentId) {

		findShipper(shipperId);

		Shipment shipment = shipmentRepository.findById(shipmentId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Shipment"));

		if (shipment.getShipper() == null || !shipment.getShipper().getId().equals(shipperId)) {

			throw new IllegalArgumentException("Shipment không thuộc Shipper này");
		}

		return shipment;
	}
}