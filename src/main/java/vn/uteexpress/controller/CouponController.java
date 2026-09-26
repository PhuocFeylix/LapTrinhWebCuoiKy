package vn.uteexpress.controller;

import vn.uteexpress.entity.Coupon;
import vn.uteexpress.service.CouponService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    // =========================
    // CREATE
    // =========================

    @PostMapping("/shop/{shopId}")
    public ResponseEntity<Coupon> createCoupon(
            @PathVariable Long shopId,
            @RequestBody Coupon coupon) {

        return ResponseEntity.ok(
                couponService.createCoupon(
                        coupon,
                        shopId));
    }

    // =========================
    // UPDATE
    // =========================

    @PutMapping("/{couponId}/shop/{shopId}")
    public ResponseEntity<Coupon> updateCoupon(
            @PathVariable Long couponId,
            @PathVariable Long shopId,
            @RequestBody Coupon coupon) {

        return ResponseEntity.ok(
                couponService.updateCoupon(
                        couponId,
                        coupon,
                        shopId));
    }

    // =========================
    // DELETE
    // =========================

    @DeleteMapping("/{couponId}/shop/{shopId}")
    public ResponseEntity<Void> deleteCoupon(
            @PathVariable Long couponId,
            @PathVariable Long shopId) {

        couponService.deleteCoupon(
                couponId,
                shopId);

        return ResponseEntity.noContent().build();
    }

    // =========================
    // GET
    // =========================

    @GetMapping("/{couponId}")
    public ResponseEntity<Coupon> getCoupon(
            @PathVariable Long couponId) {

        return ResponseEntity.ok(
                couponService.getCoupon(couponId));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Coupon> getCouponByCode(
            @PathVariable String code) {

        Coupon coupon =
                couponService.getCouponByCode(code);

        if (coupon == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(coupon);
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<Coupon>> getCouponsByShop(
            @PathVariable Long shopId) {

        return ResponseEntity.ok(
                couponService.getCouponsByShop(shopId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<Coupon>> getActiveCoupons() {

        return ResponseEntity.ok(
                couponService.getActiveCoupons());
    }

    // =========================
    // CALCULATE COUPON
    // =========================

    @GetMapping("/calculate")
    public ResponseEntity<BigDecimal> calculateCoupon(
            @RequestParam String code,
            @RequestParam BigDecimal orderAmount) {

        Coupon coupon =
                couponService.getCouponByCode(code);

        if (coupon == null) {
            return ResponseEntity.notFound().build();
        }

        BigDecimal discount =
                couponService.calculateDiscount(
                        coupon,
                        orderAmount);

        return ResponseEntity.ok(discount);
    }
}