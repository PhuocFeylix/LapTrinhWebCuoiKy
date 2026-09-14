package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.Category;
import vn.uteexpress.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

	private final CategoryService categoryService;

	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	// GET /api/categories
	@GetMapping
	public ResponseEntity<List<Category>> getAll() {
		return ResponseEntity.ok(categoryService.findAll());
	}

	// GET /api/categories/1
	@GetMapping("/{id}")
	public ResponseEntity<Category> getById(@PathVariable Long id) {

		return ResponseEntity.ok(categoryService.findById(id));
	}

	// GET /api/categories/search?keyword=phone
	@GetMapping("/search")
	public ResponseEntity<List<Category>> search(@RequestParam(required = false) String keyword) {

		return ResponseEntity.ok(categoryService.search(keyword));
	}

	// POST /api/categories
	@PostMapping
	public ResponseEntity<Category> create(@RequestBody Category category) {

		return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(category));
	}

	// PUT /api/categories/1
	@PutMapping("/{id}")
	public ResponseEntity<Category> update(@PathVariable Long id, @RequestBody Category category) {

		return ResponseEntity.ok(categoryService.update(id, category));
	}

	// DELETE /api/categories/1
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {

		categoryService.delete(id);

		return ResponseEntity.noContent().build();
	}
}