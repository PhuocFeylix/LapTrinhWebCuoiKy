package vn.uteexpress.service;

import vn.uteexpress.entity.Coupon;
import vn.uteexpress.entity.CouponType;
import vn.uteexpress.entity.Shop;
import vn.uteexpress.repository.CouponRepository;
import vn.uteexpress.repository.ShopRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

	public Coupon createCoupon(Coupon coupon, Long shopId) {

		if (coupon.getCode() == null || coupon.getCode().trim().isEmpty()) {

			throw new RuntimeException("Mã coupon không được để trống");
		}

		String code = coupon.getCode().trim().toUpperCase();

		if (couponRepository.existsByCodeIgnoreCase(code)) {
			throw new RuntimeException("Mã coupon đã tồn tại");
		}

		coupon.setCode(code);

		if (coupon.getDiscountValue() == null || coupon.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {

			throw new RuntimeException("Giá trị giảm phải lớn hơn 0");
		}

		if (coupon.getMinOrderAmount() == null) {
			coupon.setMinOrderAmount(BigDecimal.ZERO);
		}

		if (coupon.getUsageLimit() == null) {
			coupon.setUsageLimit(0);
		}

		if (coupon.getUsedCount() == null) {
			coupon.setUsedCount(0);
		}

		if (coupon.getStartAt() == null || coupon.getEndAt() == null) {

			throw new RuntimeException("Phải nhập thời gian bắt đầu và kết thúc");
		}

		if (!coupon.getEndAt().isAfter(coupon.getStartAt())) {
			throw new RuntimeException("Thời gian kết thúc phải sau thời gian bắt đầu");
		}

		if (coupon.getType() == CouponType.PERCENT
				&& coupon.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {

			throw new RuntimeException("Coupon phần trăm không được vượt quá 100%");
		}

		// Nếu có shopId thì coupon thuộc Shop
		if (shopId != null) {

			Shop shop = shopRepository.findById(shopId).orElseThrow(() -> new RuntimeException("Không tìm thấy Shop"));

			coupon.setShop(shop);
		}

		return couponRepository.save(coupon);
	}

	// =========================
	// UPDATE
	// =========================

	public Coupon updateCoupon(Long couponId, Coupon newData, Long shopId) {

		Coupon coupon = getCoupon(couponId);

		if (shopId != null) {

			if (coupon.getShop() == null || !coupon.getShop().getId().equals(shopId)) {

				throw new RuntimeException("Coupon không thuộc Shop này");
			}
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

	public void deleteCoupon(Long couponId, Long shopId) {

		Coupon coupon = getCoupon(couponId);

		if (shopId != null) {

			if (coupon.getShop() == null || !coupon.getShop().getId().equals(shopId)) {

				throw new RuntimeException("Coupon không thuộc Shop này");
			}
		}

		couponRepository.delete(coupon);
	}

	// =========================
	// GET
	// =========================

	public Coupon getCoupon(Long couponId) {

		return couponRepository.findById(couponId).orElseThrow(() -> new RuntimeException("Không tìm thấy Coupon"));
	}

	public Coupon getCouponByCode(String code) {

		if (code == null || code.trim().isEmpty()) {

			return null;
		}

		return couponRepository.findByCodeIgnoreCase(code.trim()).orElse(null);
	}

	public List<Coupon> getCouponsByShop(Long shopId) {

		return couponRepository.findByShopIdOrderByStartAtDesc(shopId);
	}

	public List<Coupon> getActiveCoupons() {

		return couponRepository.findByActiveTrueOrderByStartAtDesc();
	}

	// =========================
	// VALIDATE
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

		if (coupon.getUsageLimit() != null && coupon.getUsageLimit() > 0
				&& coupon.getUsedCount() >= coupon.getUsageLimit()) {

			throw new RuntimeException("Coupon đã hết lượt sử dụng");
		}

		if (orderAmount == null || orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {

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

		// Giảm không được vượt quá giá trị đơn
		if (discount.compareTo(orderAmount) > 0) {
			discount = orderAmount;
		}

		// Coupon % có thể có mức giảm tối đa
		if (coupon.getMaxDiscountAmount() != null && coupon.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0
				&& discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {

			discount = coupon.getMaxDiscountAmount();
		}

		return discount;
	}

	// =========================
	// INCREASE USED COUNT
	// =========================

	@Transactional
	public void increaseUsedCount(Coupon coupon) {

		coupon.setUsedCount(coupon.getUsedCount() + 1);

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

		if (coupon.getDiscountValue() == null || coupon.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {

			throw new RuntimeException("Giá trị giảm phải lớn hơn 0");
		}

		if (coupon.getMinOrderAmount() == null) {
			coupon.setMinOrderAmount(BigDecimal.ZERO);
		}

		if (coupon.getUsageLimit() == null) {
			coupon.setUsageLimit(0);
		}

		if (coupon.getType() == CouponType.PERCENT
				&& coupon.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {

			throw new RuntimeException("Coupon phần trăm không được vượt quá 100%");
		}

		if (coupon.getStartAt() == null || coupon.getEndAt() == null) {

			throw new RuntimeException("Thời gian coupon không được để trống");
		}

		if (!coupon.getEndAt().isAfter(coupon.getStartAt())) {

			throw new RuntimeException("Thời gian kết thúc phải sau thời gian bắt đầu");
		}
	}
}