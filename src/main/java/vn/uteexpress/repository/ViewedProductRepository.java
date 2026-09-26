package vn.uteexpress.repository;

import vn.uteexpress.entity.ViewedProduct;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ViewedProductRepository
        extends JpaRepository<ViewedProduct, Long> {

    Optional<ViewedProduct> findByUserIdAndProductId(
            Long userId,
            Long productId);

    List<ViewedProduct> findByUserIdOrderByViewedAtDesc(
            Long userId);

    void deleteByUserIdAndProductId(
            Long userId,
            Long productId);

    void deleteByUserId(Long userId);
}