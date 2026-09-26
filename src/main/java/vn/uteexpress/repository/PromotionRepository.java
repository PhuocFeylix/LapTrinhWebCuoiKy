package vn.uteexpress.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.uteexpress.entity.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

	List<Promotion> findByShopIdOrderByStartAtDesc(Long shopId);

	List<Promotion> findByShopIdAndActiveTrueOrderByStartAtDesc(Long shopId);
}