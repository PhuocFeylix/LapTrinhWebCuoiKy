package vn.uteexpress.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.Comment;
import vn.uteexpress.entity.Product;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.CommentRepository;
import vn.uteexpress.repository.ProductRepository;
import vn.uteexpress.repository.UserRepository;

@Service
public class CommentService {

	private static final int MIN_CONTENT_LENGTH = 50;

	private final CommentRepository commentRepository;
	private final UserRepository userRepository;
	private final ProductRepository productRepository;

	public CommentService(CommentRepository commentRepository, UserRepository userRepository,
			ProductRepository productRepository) {

		this.commentRepository = commentRepository;
		this.userRepository = userRepository;
		this.productRepository = productRepository;
	}

	// =========================
	// CREATE COMMENT
	// =========================

	@Transactional
	public Comment createComment(Long userId, Long productId, String content) {

		validateContent(content);

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

		Comment comment = new Comment();

		comment.setUser(user);
		comment.setProduct(product);
		comment.setContent(content.trim());

		return commentRepository.save(comment);
	}

	// =========================
	// UPDATE COMMENT
	// =========================

	@Transactional
	public Comment updateComment(Long commentId, Long userId, String content) {

		validateContent(content);

		Comment comment = commentRepository.findByIdAndUserId(commentId, userId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy comment hoặc bạn không có quyền chỉnh sửa"));

		comment.setContent(content.trim());
		comment.setUpdatedAt(LocalDateTime.now());

		return commentRepository.save(comment);
	}

	// =========================
	// DELETE COMMENT
	// =========================

	@Transactional
	public void deleteComment(Long commentId, Long userId) {

		Comment comment = commentRepository.findByIdAndUserId(commentId, userId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy comment hoặc bạn không có quyền xóa"));

		commentRepository.delete(comment);
	}

	// =========================
	// GET BY PRODUCT
	// =========================

	public List<Comment> getCommentsByProduct(Long productId) {

		return commentRepository.findByProductIdOrderByCreatedAtDesc(productId);
	}

	// =========================
	// GET BY USER
	// =========================

	public List<Comment> getCommentsByUser(Long userId) {

		return commentRepository.findByUserIdOrderByCreatedAtDesc(userId);
	}

	// =========================
	// VALIDATE
	// =========================

	private void validateContent(String content) {

		if (content == null || content.trim().isEmpty()) {
			throw new IllegalArgumentException("Nội dung comment không được để trống");
		}

		if (content.trim().length() < MIN_CONTENT_LENGTH) {
			throw new IllegalArgumentException("Nội dung comment phải có ít nhất " + MIN_CONTENT_LENGTH + " ký tự");
		}
	}
}