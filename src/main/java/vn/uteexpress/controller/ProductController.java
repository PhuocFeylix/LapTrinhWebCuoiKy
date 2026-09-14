package vn.uteexpress.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;

import vn.uteexpress.entity.Product;
import vn.uteexpress.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping
	public ResponseEntity<Page<Product>> getAll(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		return ResponseEntity.ok(productService.findAll(page, size));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Product> getById(@PathVariable Long id) {

		return ResponseEntity.ok(productService.findById(id));
	}

	@GetMapping("/search")
	public ResponseEntity<Page<Product>> search(@RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

		return ResponseEntity.ok(productService.search(keyword, page, size));
	}

	@GetMapping("/category/{categoryId}")
	public ResponseEntity<Page<Product>> getByCategory(@PathVariable Long categoryId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

		return ResponseEntity.ok(productService.findByCategory(categoryId, page, size));
	}

	@PostMapping
	public ResponseEntity<Product> create(@RequestBody Product product) {

		return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(product));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product product) {

		return ResponseEntity.ok(productService.update(id, product));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {

		productService.delete(id);

		return ResponseEntity.noContent().build();
	}
}