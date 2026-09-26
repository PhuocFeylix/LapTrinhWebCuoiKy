package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.Review;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.service.ReviewService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

	private final ReviewService reviewService;
	private final UserRepository userRepository;

	public ReviewController(ReviewService reviewService, UserRepository userRepository) {

		this.reviewService = reviewService;
		this.userRepository = userRepository;
	}

	// =========================
	// CREATE REVIEW
	// =========================

	@PostMapping("/user/{userId}/product/{productId}")
	public ResponseEntity<Review> createReview(@PathVariable Long userId, @PathVariable Long productId,
			@RequestParam int rating, @RequestParam String content, Authentication authentication) {

		checkUserAccess(userId, authentication);

		return ResponseEntity.ok(reviewService.createReview(userId, productId, rating, content));
	}

	// =========================
	// UPDATE REVIEW
	// =========================

	@PutMapping("/{reviewId}/user/{userId}")
	public ResponseEntity<Review> updateReview(@PathVariable Long reviewId, @PathVariable Long userId,
			@RequestParam int rating, @RequestParam String content, Authentication authentication) {

		checkUserAccess(userId, authentication);

		return ResponseEntity.ok(reviewService.updateReview(reviewId, userId, rating, content));
	}

	// =========================
	// DELETE REVIEW
	// =========================

	@DeleteMapping("/{reviewId}/user/{userId}")
	public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId, @PathVariable Long userId,
			Authentication authentication) {

		checkUserAccess(userId, authentication);

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
	public ResponseEntity<List<Review>> getReviewsByUser(@PathVariable Long userId, Authentication authentication) {

		checkUserAccess(userId, authentication);

		return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
	}

	// =========================
	// GET USER REVIEW FOR PRODUCT
	// =========================

	@GetMapping("/user/{userId}/product/{productId}")
	public ResponseEntity<Review> getUserReviewForProduct(@PathVariable Long userId, @PathVariable Long productId,
			Authentication authentication) {

		checkUserAccess(userId, authentication);

		Review review = reviewService.getUserReviewForProduct(userId, productId);

		if (review == null) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok(review);
	}

	// =========================
	// CHECK USER ACCESS
	// =========================

	private void checkUserAccess(Long userId, Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated()) {

			throw new IllegalArgumentException("Chưa đăng nhập");
		}

		User currentUser = userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

		boolean isAdmin = currentUser.getRole() != null && "ADMIN".equalsIgnoreCase(currentUser.getRole().getName());

		if (!isAdmin && !currentUser.getId().equals(userId)) {

			throw new IllegalArgumentException("Không được truy cập review của người dùng khác");
		}
	}
}