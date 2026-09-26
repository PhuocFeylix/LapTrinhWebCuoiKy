package vn.uteexpress.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.uteexpress.entity.PromotionProduct;

public interface PromotionProductRepository
        extends JpaRepository<PromotionProduct, Long> {

    List<PromotionProduct> findByPromotionId(Long promotionId);

    List<PromotionProduct> findByProductId(Long productId);

    Optional<PromotionProduct> findByPromotionIdAndProductId(
            Long promotionId,
            Long productId);

    void deleteByPromotionIdAndProductId(
            Long promotionId,
            Long productId);

    void deleteByPromotionId(Long promotionId);
}