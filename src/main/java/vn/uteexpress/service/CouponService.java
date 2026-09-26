package vn.uteexpress.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.Coupon;
import vn.uteexpress.entity.CouponType;
import vn.uteexpress.entity.Shop;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.CouponRepository;
import vn.uteexpress.repository.ShopRepository;

@Service
public class CouponService {

	private final CouponRepository couponRepository;
	private final ShopRepository shopRepository;

	public CouponService(CouponRepository couponRepository, ShopRepository shopRepository) {

		this.couponRepository = couponRepository;
		this.shopRepository = shopRepository;
	}

	// =========================
	// CREATE
	// =========================

	@Transactional
	public Coupon createCoupon(Coupon coupon, Long shopId, User currentUser) {

		checkShopAccess(shopId, currentUser);

		if (coupon == null) {
			throw new IllegalArgumentException("Coupon không được null");
		}

		if (coupon.getCode() == null || coupon.getCode().trim().isEmpty()) {

			throw new IllegalArgumentException("Mã coupon không được để trống");
		}

		String code = coupon.getCode().trim().toUpperCase();

		if (couponRepository.existsByCodeIgnoreCase(code)) {
			throw new IllegalArgumentException("Mã coupon đã tồn tại");
		}

		coupon.setCode(code);

		validateCouponData(coupon);

		Shop shop = shopRepository.findById(shopId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Shop"));

		coupon.setShop(shop);

		return couponRepository.save(coupon);
	}

	// =========================
	// UPDATE
	// =========================

	@Transactional
	public Coupon updateCoupon(Long couponId, Coupon newData, Long shopId, User currentUser) {

		checkShopAccess(shopId, currentUser);

		Coupon coupon = getCoupon(couponId);

		checkCouponBelongsToShop(coupon, shopId);

		if (newData == null) {
			throw new IllegalArgumentException("Dữ liệu coupon không được null");
		}

		coupon.setDescription(newData.getDescription());

		coupon.setType(newData.getType());

		coupon.setDiscountValue(newData.getDiscountValue());

		coupon.setMinOrderAmount(newData.getMinOrderAmount());

		coupon.setMaxDiscountAmount(newData.getMaxDiscountAmount());

		coupon.setStartAt(newData.getStartAt());

		coupon.setEndAt(newData.getEndAt());

		coupon.setUsageLimit(newData.getUsageLimit());

		coupon.setActive(newData.isActive());

		validateCouponData(coupon);

		return couponRepository.save(coupon);
	}

	// =========================
	// DELETE
	// =========================

	@Transactional
	public void deleteCoupon(Long couponId, Long shopId, User currentUser) {

		checkShopAccess(shopId, currentUser);

		Coupon coupon = getCoupon(couponId);

		checkCouponBelongsToShop(coupon, shopId);

		couponRepository.delete(coupon);
	}

	// =========================
	// GET
	// =========================

	@Transactional(readOnly = true)
	public Coupon getCoupon(Long couponId) {

		if (couponId == null) {
			throw new IllegalArgumentException("Coupon ID không được null");
		}

		return couponRepository.findById(couponId).orElseThrow(() -> new RuntimeException("Không tìm thấy Coupon"));
	}

	// =========================
	// GET BY CODE
	// =========================

	@Transactional(readOnly = true)
	public Coupon getCouponByCode(String code) {

		if (code == null || code.trim().isEmpty()) {

			return null;
		}

		return couponRepository.findByCodeIgnoreCase(code.trim()).orElse(null);
	}

	// =========================
	// GET BY SHOP
	// =========================

	@Transactional(readOnly = true)
	public List<Coupon> getCouponsByShop(Long shopId) {

		if (shopId == null) {
			throw new IllegalArgumentException("Shop ID không được null");
		}

		return couponRepository.findByShopIdOrderByStartAtDesc(shopId);
	}

	// =========================
	// GET ACTIVE
	// =========================

	@Transactional(readOnly = true)
	public List<Coupon> getActiveCoupons() {

		return couponRepository.findByActiveTrueOrderByStartAtDesc();
	}

	// =========================
	// VALIDATE COUPON
	// =========================

	public void validateCoupon(Coupon coupon, BigDecimal orderAmount) {

		if (coupon == null) {
			throw new RuntimeException("Coupon không tồn tại");
		}

		if (!coupon.isActive()) {
			throw new RuntimeException("Coupon đã bị vô hiệu hóa");
		}

		LocalDateTime now = LocalDateTime.now();

		if (coupon.getStartAt() == null || coupon.getEndAt() == null) {

			throw new RuntimeException("Coupon chưa được cấu hình thời gian");
		}

		if (now.isBefore(coupon.getStartAt())) {
			throw new RuntimeException("Coupon chưa bắt đầu");
		}

		if (now.isAfter(coupon.getEndAt())) {
			throw new RuntimeException("Coupon đã hết hạn");
		}

		Integer usageLimit = coupon.getUsageLimit();

		Integer usedCount = coupon.getUsedCount();

		if (usedCount == null) {
			usedCount = 0;
		}

		if (usageLimit != null && usageLimit > 0 && usedCount >= usageLimit) {

			throw new RuntimeException("Coupon đã hết lượt sử dụng");
		}

		if (orderAmount == null) {
			throw new RuntimeException("Giá trị đơn hàng không được null");
		}

		BigDecimal minOrderAmount = coupon.getMinOrderAmount();

		if (minOrderAmount == null) {
			minOrderAmount = BigDecimal.ZERO;
		}

		if (orderAmount.compareTo(minOrderAmount) < 0) {

			throw new RuntimeException("Đơn hàng chưa đạt giá trị tối thiểu để sử dụng coupon");
		}
	}

	// =========================
	// CALCULATE DISCOUNT
	// =========================

	public BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {

		validateCoupon(coupon, orderAmount);

		if (orderAmount.compareTo(BigDecimal.ZERO) <= 0) {

			return BigDecimal.ZERO;
		}

		BigDecimal discount;

		if (coupon.getType() == CouponType.PERCENT) {

			discount = orderAmount.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100));

		} else {

			discount = coupon.getDiscountValue();
		}

		// Không giảm quá giá trị đơn hàng
		if (discount.compareTo(orderAmount) > 0) {
			discount = orderAmount;
		}

		// Giới hạn giảm tối đa
		if (coupon.getMaxDiscountAmount() != null && coupon.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0
				&& discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {

			discount = coupon.getMaxDiscountAmount();
		}

		// Sau khi giới hạn max vẫn không được > order
		if (discount.compareTo(orderAmount) > 0) {
			discount = orderAmount;
		}

		return discount;
	}

	// =========================
	// INCREASE USED COUNT
	// =========================

	@Transactional
	public void increaseUsedCount(Coupon coupon) {

		if (coupon == null) {
			return;
		}

		Integer usedCount = coupon.getUsedCount();

		if (usedCount == null) {
			usedCount = 0;
		}

		coupon.setUsedCount(usedCount + 1);

		couponRepository.save(coupon);
	}

	// =========================
	// DECREASE USED COUNT
	// =========================

	@Transactional
	public void decreaseUsedCount(Coupon coupon) {

		if (coupon == null) {
			return;
		}

		Integer usedCount = coupon.getUsedCount();

		if (usedCount == null || usedCount <= 0) {

			return;
		}

		coupon.setUsedCount(usedCount - 1);

		couponRepository.save(coupon);
	}

	// =========================
	// VALIDATE DATA
	// =========================

	private void validateCouponData(Coupon coupon) {

		if (coupon.getType() == null) {
			throw new IllegalArgumentException("Loại coupon không được để trống");
		}

		if (coupon.getDiscountValue() == null || coupon.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {

			throw new IllegalArgumentException("Giá trị giảm phải lớn hơn 0");
		}

		if (coupon.getMinOrderAmount() == null) {
			coupon.setMinOrderAmount(BigDecimal.ZERO);
		}

		if (coupon.getMinOrderAmount().compareTo(BigDecimal.ZERO) < 0) {

			throw new IllegalArgumentException("Giá trị đơn hàng tối thiểu không được âm");
		}

		if (coupon.getMaxDiscountAmount() != null && coupon.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) < 0) {

			throw new IllegalArgumentException("Mức giảm tối đa không được âm");
		}

		if (coupon.getUsageLimit() == null) {
			coupon.setUsageLimit(0);
		}

		if (coupon.getUsageLimit() < 0) {
			throw new IllegalArgumentException("Giới hạn sử dụng không được âm");
		}

		if (coupon.getUsedCount() == null) {
			coupon.setUsedCount(0);
		}

		if (coupon.getUsedCount() < 0) {
			throw new IllegalArgumentException("Số lượt đã sử dụng không được âm");
		}

		if (coupon.getType() == CouponType.PERCENT
				&& coupon.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {

			throw new IllegalArgumentException("Coupon phần trăm không được vượt quá 100%");
		}

		if (coupon.getStartAt() == null || coupon.getEndAt() == null) {

			throw new IllegalArgumentException("Thời gian coupon không được để trống");
		}

		if (!coupon.getEndAt().isAfter(coupon.getStartAt())) {

			throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu");
		}
	}

	// =========================
	// SHOP OWNERSHIP
	// =========================

	private void checkShopAccess(Long shopId, User currentUser) {

		if (currentUser == null) {
			throw new IllegalArgumentException("Chưa đăng nhập");
		}

		if (currentUser.getRole() == null) {
			throw new IllegalArgumentException("Tài khoản chưa có role");
		}

		String role = currentUser.getRole().getName();

		// ADMIN / MANAGER được quản lý Shop
		if ("ADMIN".equalsIgnoreCase(role) || "MANAGER".equalsIgnoreCase(role)) {

			return;
		}

		// Chỉ VENDOR mới được quản lý coupon của Shop
		if (!"VENDOR".equalsIgnoreCase(role)) {

			throw new IllegalArgumentException("Bạn không có quyền quản lý Coupon");
		}

		if (shopId == null) {
			throw new IllegalArgumentException("Shop ID không được null");
		}

		boolean owner = shopRepository.findByIdAndVendorId(shopId, currentUser.getId()).isPresent();

		if (!owner) {
			throw new IllegalArgumentException("Shop không thuộc Vendor hiện tại");
		}
	}

	// =========================
	// COUPON OWNERSHIP
	// =========================

	private void checkCouponBelongsToShop(Coupon coupon, Long shopId) {

		if (coupon.getShop() == null) {
			throw new IllegalArgumentException("Coupon không thuộc Shop");
		}

		if (shopId == null || !coupon.getShop().getId().equals(shopId)) {

			throw new IllegalArgumentException("Coupon không thuộc Shop này");
		}
	}
}