package vn.uteexpress.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.uteexpress.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // =========================
    // SEARCH
    // =========================

    List<Product> findByNameContainingIgnoreCase(String keyword);

    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(
            String keyword, Pageable pageable);

    // =========================
    // CATEGORY
    // =========================

    List<Product> findByCategoryId(Long categoryId);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Product> findByCategoryIdAndActiveTrue(
            Long categoryId, Pageable pageable);

    // =========================
    // ACTIVE PRODUCTS
    // =========================

    List<Product> findByActiveTrue();

    Page<Product> findByActiveTrue(Pageable pageable);

    // =========================
    // SHOP
    // =========================

    Page<Product> findByShopId(Long shopId, Pageable pageable);

    Page<Product> findByShopIdAndActiveTrue(
            Long shopId, Pageable pageable);

    List<Product> findByShopId(Long shopId);
}
