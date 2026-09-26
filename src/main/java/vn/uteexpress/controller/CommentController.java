package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.Comment;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.service.CommentService;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

	private final CommentService commentService;
	private final UserRepository userRepository;

	public CommentController(CommentService commentService, UserRepository userRepository) {

		this.commentService = commentService;
		this.userRepository = userRepository;
	}

	// =========================
	// CREATE
	// =========================

	@PostMapping("/user/{userId}/product/{productId}")
	public ResponseEntity<Comment> createComment(@PathVariable Long userId, @PathVariable Long productId,
			@RequestParam String content, Authentication authentication) {

		checkUserAccess(userId, authentication);

		return ResponseEntity.ok(commentService.createComment(userId, productId, content));
	}

	// =========================
	// UPDATE
	// =========================

	@PutMapping("/{commentId}/user/{userId}")
	public ResponseEntity<Comment> updateComment(@PathVariable Long commentId, @PathVariable Long userId,
			@RequestParam String content, Authentication authentication) {

		checkUserAccess(userId, authentication);

		return ResponseEntity.ok(commentService.updateComment(commentId, userId, content));
	}

	// =========================
	// DELETE
	// =========================

	@DeleteMapping("/{commentId}/user/{userId}")
	public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, @PathVariable Long userId,
			Authentication authentication) {

		checkUserAccess(userId, authentication);

		commentService.deleteComment(commentId, userId);

		return ResponseEntity.noContent().build();
	}

	// =========================
	// GET COMMENTS BY PRODUCT
	// =========================

	@GetMapping("/product/{productId}")
	public ResponseEntity<List<Comment>> getCommentsByProduct(@PathVariable Long productId) {

		return ResponseEntity.ok(commentService.getCommentsByProduct(productId));
	}

	// =========================
	// GET COMMENTS BY USER
	// =========================

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<Comment>> getCommentsByUser(@PathVariable Long userId, Authentication authentication) {

		checkUserAccess(userId, authentication);

		return ResponseEntity.ok(commentService.getCommentsByUser(userId));
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

			throw new IllegalArgumentException("Không được truy cập comment của người dùng khác");
		}
	}
}