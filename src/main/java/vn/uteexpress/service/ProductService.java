package vn.uteexpress.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import vn.uteexpress.entity.Category;
import vn.uteexpress.entity.Product;
import vn.uteexpress.entity.Shop;
import vn.uteexpress.repository.CategoryRepository;
import vn.uteexpress.repository.ProductRepository;
import vn.uteexpress.repository.ShopRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
            ShopRepository shopRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.shopRepository = shopRepository;
    }

    public Page<Product> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAll(pageable);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
    }

    public Product create(Product product) {
        validateProduct(product);

        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy category"));

        Shop shop = shopRepository.findById(product.getShop().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy shop"));

        product.setCategory(category);
        product.setShop(shop);
        product.setName(product.getName().trim());
        product.setActive(true);

        return productRepository.save(product);
    }

    public Product update(Long id, Product product) {
        Product existing = findById(id);

        validateProduct(product);

        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy category"));

        Shop shop = shopRepository.findById(product.getShop().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy shop"));

        existing.setName(product.getName().trim());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setStock(product.getStock());
        existing.setImage(product.getImage());
        existing.setActive(product.isActive());
        existing.setCategory(category);
        existing.setShop(shop);

        return productRepository.save(existing);
    }

    public void delete(Long id) {
        Product product = findById(id);
        productRepository.delete(product);
    }

    public Page<Product> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAll(pageable);
        }

        return productRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
    }

    public Page<Product> findByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    public Page<Product> findByShop(Long shopId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByShopId(shopId, pageable);
    }

    private void validateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Sản phẩm không được null");
        }

        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống");
        }

        if (product.getPrice() < 0) {
            throw new IllegalArgumentException("Giá sản phẩm không được âm");
        }

        if (product.getStock() < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được âm");
        }

        if (product.getCategory() == null || product.getCategory().getId() == null) {
            throw new IllegalArgumentException("Sản phẩm phải thuộc một category");
        }

        if (product.getShop() == null || product.getShop().getId() == null) {
            throw new IllegalArgumentException("Sản phẩm phải thuộc một shop");
        }
    }
}