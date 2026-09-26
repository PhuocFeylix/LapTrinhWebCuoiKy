package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.Review;
import vn.uteexpress.service.ReviewService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

	private final ReviewService reviewService;

	public ReviewController(ReviewService reviewService) {
		this.reviewService = reviewService;
	}

	// =========================
	// CREATE REVIEW
	// =========================

	@PostMapping("/user/{userId}/product/{productId}")
	public ResponseEntity<Review> createReview(@PathVariable Long userId, @PathVariable Long productId,
			@RequestParam int rating, @RequestParam String content) {

		return ResponseEntity.ok(reviewService.createReview(userId, productId, rating, content));
	}

	// =========================
	// UPDATE REVIEW
	// =========================

	@PutMapping("/{reviewId}/user/{userId}")
	public ResponseEntity<Review> updateReview(@PathVariable Long reviewId, @PathVariable Long userId,
			@RequestParam int rating, @RequestParam String content) {

		return ResponseEntity.ok(reviewService.updateReview(reviewId, userId, rating, content));
	}

	// =========================
	// DELETE REVIEW
	// =========================

	@DeleteMapping("/{reviewId}/user/{userId}")
	public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId, @PathVariable Long userId) {

		reviewService.deleteReview(reviewId, userId);

		return ResponseEntity.noContent().build();
	}

	// =========================
	// GET REVIEWS BY PRODUCT
	// =========================

	@GetMapping("/product/{productId}")
	public ResponseEntity<List<Review>> getReviewsByProduct(@PathVariable Long productId) {

		return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
	}

	// =========================
	// GET REVIEWS BY USER
	// =========================

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<Review>> getReviewsByUser(@PathVariable Long userId) {

		return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
	}

	// =========================
	// GET USER REVIEW FOR PRODUCT
	// =========================

	@GetMapping("/user/{userId}/product/{productId}")
	public ResponseEntity<Review> getUserReviewForProduct(@PathVariable Long userId, @PathVariable Long productId) {

		Review review = reviewService.getUserReviewForProduct(userId, productId);

		if (review == null) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok(review);
	}
}