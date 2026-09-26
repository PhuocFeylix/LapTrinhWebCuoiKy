package vn.uteexpress.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.Coupon;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.service.CouponService;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

	private final CouponService couponService;
	private final UserRepository userRepository;

	public CouponController(CouponService couponService, UserRepository userRepository) {

		this.couponService = couponService;
		this.userRepository = userRepository;
	}

	// =========================
	// CREATE
	// =========================

	@PostMapping("/shop/{shopId}")
	public ResponseEntity<Coupon> createCoupon(@PathVariable Long shopId, @RequestBody Coupon coupon,
			Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		return ResponseEntity.ok(couponService.createCoupon(coupon, shopId, currentUser));
	}

	// =========================
	// UPDATE
	// =========================

	@PutMapping("/{couponId}/shop/{shopId}")
	public ResponseEntity<Coupon> updateCoupon(@PathVariable Long couponId, @PathVariable Long shopId,
			@RequestBody Coupon coupon, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		return ResponseEntity.ok(couponService.updateCoupon(couponId, coupon, shopId, currentUser));
	}

	// =========================
	// DELETE
	// =========================

	@DeleteMapping("/{couponId}/shop/{shopId}")
	public ResponseEntity<Void> deleteCoupon(@PathVariable Long couponId, @PathVariable Long shopId,
			Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		couponService.deleteCoupon(couponId, shopId, currentUser);

		return ResponseEntity.noContent().build();
	}

	// =========================
	// GET COUPON
	// =========================

	@GetMapping("/{couponId}")
	public ResponseEntity<Coupon> getCoupon(@PathVariable Long couponId) {

		return ResponseEntity.ok(couponService.getCoupon(couponId));
	}

	// =========================
	// GET BY CODE
	// =========================

	@GetMapping("/code/{code}")
	public ResponseEntity<Coupon> getCouponByCode(@PathVariable String code) {

		Coupon coupon = couponService.getCouponByCode(code);

		if (coupon == null) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok(coupon);
	}

	// =========================
	// GET BY SHOP
	// =========================

	@GetMapping("/shop/{shopId}")
	public ResponseEntity<List<Coupon>> getCouponsByShop(@PathVariable Long shopId) {

		return ResponseEntity.ok(couponService.getCouponsByShop(shopId));
	}

	// =========================
	// GET ACTIVE
	// =========================

	@GetMapping("/active")
	public ResponseEntity<List<Coupon>> getActiveCoupons() {

		return ResponseEntity.ok(couponService.getActiveCoupons());
	}

	// =========================
	// CALCULATE
	// =========================

	@GetMapping("/calculate")
	public ResponseEntity<BigDecimal> calculateCoupon(@RequestParam String code, @RequestParam BigDecimal orderAmount) {

		Coupon coupon = couponService.getCouponByCode(code);

		if (coupon == null) {
			return ResponseEntity.notFound().build();
		}

		BigDecimal discount = couponService.calculateDiscount(coupon, orderAmount);

		return ResponseEntity.ok(discount);
	}

	// =========================
	// CURRENT USER
	// =========================

	private User getCurrentUser(Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated()) {

			throw new IllegalArgumentException("Chưa đăng nhập");
		}

		return userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
	}
}