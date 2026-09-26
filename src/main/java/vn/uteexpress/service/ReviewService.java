package vn.uteexpress.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.OrderStatus;
import vn.uteexpress.entity.Product;
import vn.uteexpress.entity.Review;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.OrderRepository;
import vn.uteexpress.repository.ProductRepository;
import vn.uteexpress.repository.ReviewRepository;
import vn.uteexpress.repository.UserRepository;

@Service
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final UserRepository userRepository;
	private final ProductRepository productRepository;
	private final OrderRepository orderRepository;

	public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository,
			ProductRepository productRepository, OrderRepository orderRepository) {

		this.reviewRepository = reviewRepository;
		this.userRepository = userRepository;
		this.productRepository = productRepository;
		this.orderRepository = orderRepository;
	}

	/**
	 * Tạo review mới.
	 */
	@Transactional
	public Review createReview(Long userId, Long productId, int rating, String content) {

		// =========================
		// VALIDATE RATING
		// =========================

		if (rating < 1 || rating > 5) {
			throw new IllegalArgumentException("Rating phải từ 1 đến 5 sao");
		}

		// =========================
		// VALIDATE CONTENT
		// =========================

		if (content == null || content.trim().isEmpty()) {
			throw new IllegalArgumentException("Nội dung review không được để trống");
		}

		content = content.trim();

		// =========================
		// USER
		// =========================

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

		// =========================
		// PRODUCT
		// =========================

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

		// =========================
		// CHECK ĐÃ REVIEW CHƯA
		// =========================

		if (reviewRepository.existsByUserIdAndProductId(userId, productId)) {

			throw new IllegalArgumentException("Bạn đã đánh giá sản phẩm này");
		}

		// =========================
		// CHECK ĐÃ MUA + ĐÃ GIAO
		// =========================

		boolean purchased = orderRepository.existsDeliveredOrderForProduct(userId, productId, OrderStatus.DELIVERED);

		if (!purchased) {
			throw new IllegalArgumentException("Bạn chỉ có thể đánh giá sản phẩm sau khi đơn hàng đã được giao");
		}

		// =========================
		// CREATE REVIEW
		// =========================

		Review review = new Review();

		review.setUser(user);
		review.setProduct(product);
		review.setRating(rating);
		review.setContent(content);

		return reviewRepository.save(review);
	}

	/**
	 * Cập nhật review.
	 */
	@Transactional
	public Review updateReview(Long reviewId, Long userId, int rating, String content) {

		if (rating < 1 || rating > 5) {
			throw new IllegalArgumentException("Rating phải từ 1 đến 5 sao");
		}

		if (content == null || content.trim().isEmpty()) {
			throw new IllegalArgumentException("Nội dung review không được để trống");
		}

		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy review"));

		// Chỉ chủ review mới được sửa
		if (!review.getUser().getId().equals(userId)) {
			throw new IllegalArgumentException("Bạn không có quyền sửa review này");
		}

		review.setRating(rating);
		review.setContent(content.trim());
		review.setUpdatedAt(LocalDateTime.now());

		return reviewRepository.save(review);
	}

	/**
	 * Xóa review.
	 */
	@Transactional
	public void deleteReview(Long reviewId, Long userId) {

		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy review"));

		// Chỉ chủ review mới được xóa
		if (!review.getUser().getId().equals(userId)) {
			throw new IllegalArgumentException("Bạn không có quyền xóa review này");
		}

		reviewRepository.delete(review);
	}

	/**
	 * Lấy review của một sản phẩm.
	 */
	public List<Review> getReviewsByProduct(Long productId) {

		return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
	}

	/**
	 * Lấy toàn bộ review của user.
	 */
	public List<Review> getReviewsByUser(Long userId) {

		return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
	}

	/**
	 * Lấy review của user đối với một sản phẩm.
	 */
	public Review getUserReviewForProduct(Long userId, Long productId) {

		return reviewRepository.findByUserIdAndProductId(userId, productId).orElse(null);
	}
}