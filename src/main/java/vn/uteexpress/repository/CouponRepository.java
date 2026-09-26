package vn.uteexpress.repository;

import vn.uteexpress.entity.Coupon;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<Coupon> findByShopIdOrderByStartAtDesc(Long shopId);

    List<Coupon> findByActiveTrueOrderByStartAtDesc();
}