package vn.uteexpress.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.Product;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

	private final ProductService productService;
	private final UserRepository userRepository;

	public ProductController(ProductService productService, UserRepository userRepository) {

		this.productService = productService;
		this.userRepository = userRepository;
	}

	// =========================
	// GET ALL
	// =========================

	@GetMapping
	public ResponseEntity<Page<Product>> getAll(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		return ResponseEntity.ok(productService.findAll(page, size));
	}

	// =========================
	// GET BY ID
	// =========================

	@GetMapping("/{id}")
	public ResponseEntity<Product> getById(@PathVariable Long id) {

		return ResponseEntity.ok(productService.findById(id));
	}

	// =========================
	// SEARCH
	// =========================

	@GetMapping("/search")
	public ResponseEntity<Page<Product>> search(@RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

		return ResponseEntity.ok(productService.search(keyword, page, size));
	}

	// =========================
	// GET BY CATEGORY
	// =========================

	@GetMapping("/category/{categoryId}")
	public ResponseEntity<Page<Product>> getByCategory(@PathVariable Long categoryId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

		return ResponseEntity.ok(productService.findByCategory(categoryId, page, size));
	}

	// =========================
	// GET BY SHOP
	// =========================

	@GetMapping("/shop/{shopId}")
	public ResponseEntity<Page<Product>> getByShop(@PathVariable Long shopId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

		return ResponseEntity.ok(productService.findByShop(shopId, page, size));
	}

	// =========================
	// CREATE
	// =========================

	@PostMapping
	public ResponseEntity<Product> create(@RequestBody Product product, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		// ADMIN / MANAGER có thể tạo Product
		// cho bất kỳ Shop hợp lệ nào
		if (isAdminOrManager(currentUser)) {

			return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(product));
		}

		// VENDOR chỉ được tạo Product
		// cho Shop của chính mình
		checkCanManageProduct(product, currentUser);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(productService.createByVendor(currentUser.getId(), product));
	}

	// =========================
	// UPDATE
	// =========================

	@PutMapping("/{id}")
	public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product product,
			Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		// ADMIN / MANAGER được cập nhật Product
		if (isAdminOrManager(currentUser)) {

			return ResponseEntity.ok(productService.update(id, product));
		}

		// VENDOR chỉ được cập nhật Product
		// thuộc Shop của mình
		Product existingProduct = productService.findById(id);

		checkProductOwner(existingProduct, currentUser);

		// Không cho Vendor chuyển Product
		// sang Shop khác
		if (product.getShop() != null && product.getShop().getId() != null && existingProduct.getShop() != null
				&& !product.getShop().getId().equals(existingProduct.getShop().getId())) {

			throw new IllegalArgumentException("Vendor không được chuyển sản phẩm sang Shop khác");
		}

		return ResponseEntity.ok(productService.updateByVendor(id, currentUser.getId(), product));
	}

	// =========================
	// DELETE
	// =========================

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		// ADMIN / MANAGER được xóa Product
		if (isAdminOrManager(currentUser)) {

			productService.delete(id);

			return ResponseEntity.noContent().build();
		}

		// VENDOR chỉ được xóa Product
		// thuộc Shop của mình
		Product existingProduct = productService.findById(id);

		checkProductOwner(existingProduct, currentUser);

		productService.deleteByVendor(id, currentUser.getId());

		return ResponseEntity.noContent().build();
	}

	// =========================
	// GET CURRENT USER
	// =========================

	private User getCurrentUser(Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated()) {

			throw new IllegalArgumentException("Chưa đăng nhập");
		}

		return userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
	}

	// =========================
	// CHECK CREATE PRODUCT
	// =========================

	private void checkCanManageProduct(Product product, User currentUser) {

		if (isAdminOrManager(currentUser)) {
			return;
		}

		if (product == null || product.getShop() == null || product.getShop().getVendor() == null) {

			throw new IllegalArgumentException("Sản phẩm phải thuộc một Shop hợp lệ");
		}

		Long vendorId = product.getShop().getVendor().getId();

		if (!currentUser.getId().equals(vendorId)) {

			throw new IllegalArgumentException("Không được tạo sản phẩm cho Shop của Vendor khác");
		}
	}

	// =========================
	// CHECK PRODUCT OWNER
	// =========================

	private void checkProductOwner(Product product, User currentUser) {

		if (isAdminOrManager(currentUser)) {
			return;
		}

		if (product == null || product.getShop() == null || product.getShop().getVendor() == null) {

			throw new IllegalArgumentException("Sản phẩm không thuộc Shop hợp lệ");
		}

		Long vendorId = product.getShop().getVendor().getId();

		if (!currentUser.getId().equals(vendorId)) {

			throw new IllegalArgumentException("Không được thao tác sản phẩm của Vendor khác");
		}
	}

	// =========================
	// CHECK ADMIN / MANAGER
	// =========================

	private boolean isAdminOrManager(User user) {

		if (user == null || user.getRole() == null) {

			return false;
		}

		String roleName = user.getRole().getName();

		return "ADMIN".equalsIgnoreCase(roleName) || "MANAGER".equalsIgnoreCase(roleName);
	}
}