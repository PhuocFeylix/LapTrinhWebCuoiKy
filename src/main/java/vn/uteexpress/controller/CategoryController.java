package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.Category;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

	private final CategoryService categoryService;
	private final UserRepository userRepository;

	public CategoryController(CategoryService categoryService, UserRepository userRepository) {

		this.categoryService = categoryService;
		this.userRepository = userRepository;
	}

	// =========================
	// GET ALL
	// Public
	// =========================

	@GetMapping
	public ResponseEntity<List<Category>> getAll() {

		return ResponseEntity.ok(categoryService.findActiveCategories());
	}

	// =========================
	// GET BY ID
	// Public
	// =========================

	@GetMapping("/{id}")
	public ResponseEntity<Category> getById(@PathVariable Long id) {

		return ResponseEntity.ok(categoryService.findById(id));
	}

	// =========================
	// SEARCH
	// Public
	// =========================

	@GetMapping("/search")
	public ResponseEntity<List<Category>> search(@RequestParam(required = false) String keyword) {

		return ResponseEntity.ok(categoryService.search(keyword));
	}

	// =========================
	// CREATE
	// MANAGER / ADMIN
	// =========================

	@PostMapping
	public ResponseEntity<Category> create(@RequestBody Category category, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		checkManagerOrAdmin(currentUser);

		return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(category));
	}

	// =========================
	// UPDATE
	// MANAGER / ADMIN
	// =========================

	@PutMapping("/{id}")
	public ResponseEntity<Category> update(@PathVariable Long id, @RequestBody Category category,
			Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		checkManagerOrAdmin(currentUser);

		return ResponseEntity.ok(categoryService.update(id, category));
	}

	// =========================
	// DELETE
	// MANAGER / ADMIN
	// =========================

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		checkManagerOrAdmin(currentUser);

		categoryService.delete(id);

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
	// CHECK MANAGER / ADMIN
	// =========================

	private void checkManagerOrAdmin(User user) {

		if (user == null || user.getRole() == null) {

			throw new IllegalArgumentException("Không có quyền thực hiện thao tác này");
		}

		String roleName = user.getRole().getName();

		boolean allowed = "ADMIN".equalsIgnoreCase(roleName) || "MANAGER".equalsIgnoreCase(roleName);

		if (!allowed) {

			throw new IllegalArgumentException("Chỉ MANAGER hoặc ADMIN được quản lý Category");
		}
	}
}