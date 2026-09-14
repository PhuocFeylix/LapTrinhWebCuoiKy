package vn.uteexpress.service;

import java.util.List;

import org.springframework.stereotype.Service;

import vn.uteexpress.entity.Category;
import vn.uteexpress.repository.CategoryRepository;

@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;

	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	public List<Category> findAll() {
		return categoryRepository.findAll();
	}

	public List<Category> findActiveCategories() {
		return categoryRepository.findByActiveTrue();
	}

	public Category findById(Long id) {
		return categoryRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy category với ID: " + id));
	}

	public Category create(Category category) {

		if (category.getName() == null || category.getName().trim().isEmpty()) {

			throw new IllegalArgumentException("Tên category không được để trống");
		}

		if (categoryRepository.existsByName(category.getName().trim())) {

			throw new IllegalArgumentException("Category đã tồn tại");
		}

		category.setName(category.getName().trim());
		category.setActive(true);

		return categoryRepository.save(category);
	}

	public Category update(Long id, Category category) {

		Category existing = findById(id);

		if (category.getName() == null || category.getName().trim().isEmpty()) {

			throw new IllegalArgumentException("Tên category không được để trống");
		}

		String newName = category.getName().trim();

		if (!existing.getName().equalsIgnoreCase(newName) && categoryRepository.existsByName(newName)) {

			throw new IllegalArgumentException("Category đã tồn tại");
		}

		existing.setName(newName);
		existing.setDescription(category.getDescription());
		existing.setActive(category.isActive());

		return categoryRepository.save(existing);
	}

	public void delete(Long id) {
		Category category = findById(id);
		categoryRepository.delete(category);
	}

	public List<Category> search(String keyword) {

		if (keyword == null || keyword.trim().isEmpty()) {
			return findAll();
		}

		return categoryRepository.findByNameContainingIgnoreCase(keyword.trim());
	}
}