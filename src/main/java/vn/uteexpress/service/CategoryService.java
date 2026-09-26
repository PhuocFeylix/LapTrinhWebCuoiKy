package vn.uteexpress.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.Category;
import vn.uteexpress.repository.CategoryRepository;

@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;

	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	// =========================
	// FIND ALL
	// =========================

	@Transactional(readOnly = true)
	public List<Category> findAll() {
		return categoryRepository.findAll();
	}

	// =========================
	// FIND ACTIVE CATEGORIES
	// =========================

	@Transactional(readOnly = true)
	public List<Category> findActiveCategories() {
		return categoryRepository.findByActiveTrue();
	}

	// =========================
	// FIND BY ID
	// =========================

	@Transactional(readOnly = true)
	public Category findById(Long id) {

		if (id == null) {
			throw new IllegalArgumentException("Category ID không được null");
		}

		return categoryRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy category với ID: " + id));
	}

	// =========================
	// CREATE
	// =========================

	@Transactional
	public Category create(Category category) {

		validateCategory(category);

		String name = category.getName().trim();

		// Không cho tạo Category trùng tên
		if (categoryRepository.existsByName(name)) {

			throw new IllegalArgumentException("Category đã tồn tại");
		}

		category.setName(name);

		// Category mới mặc định active
		category.setActive(true);

		return categoryRepository.save(category);
	}

	// =========================
	// UPDATE
	// =========================

	@Transactional
	public Category update(Long id, Category category) {

		Category existing = findById(id);

		validateCategory(category);

		String newName = category.getName().trim();

		// Nếu đổi tên thì kiểm tra trùng
		if (!existing.getName().equalsIgnoreCase(newName) && categoryRepository.existsByName(newName)) {

			throw new IllegalArgumentException("Category đã tồn tại");
		}

		existing.setName(newName);
		existing.setDescription(category.getDescription());
		existing.setActive(category.isActive());

		return categoryRepository.save(existing);
	}

	// =========================
	// DELETE
	// =========================

	@Transactional
	public void delete(Long id) {

		Category category = findById(id);

		if (!category.isActive()) {
			throw new IllegalArgumentException("Category đã được ngừng hoạt động");
		}

		category.setActive(false);

		categoryRepository.save(category);
	}

	// =========================
	// SEARCH
	// =========================

	@Transactional(readOnly = true)
	public List<Category> search(String keyword) {

		if (keyword == null || keyword.trim().isEmpty()) {
			return findActiveCategories();
		}

		return categoryRepository.findByNameContainingIgnoreCaseAndActiveTrue(keyword.trim());
	}

	// =========================
	// VALIDATE CATEGORY
	// =========================

	private void validateCategory(Category category) {

		if (category == null) {

			throw new IllegalArgumentException("Category không được null");
		}

		if (category.getName() == null || category.getName().trim().isEmpty()) {

			throw new IllegalArgumentException("Tên category không được để trống");
		}
	}
}