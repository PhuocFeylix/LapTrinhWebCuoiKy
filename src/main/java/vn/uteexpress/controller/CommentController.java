package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.Comment;
import vn.uteexpress.service.CommentService;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

	private final CommentService commentService;

	public CommentController(CommentService commentService) {
		this.commentService = commentService;
	}

	// =========================
	// CREATE
	// =========================

	@PostMapping("/user/{userId}/product/{productId}")
	public ResponseEntity<Comment> createComment(@PathVariable Long userId, @PathVariable Long productId,
			@RequestParam String content) {

		return ResponseEntity.ok(commentService.createComment(userId, productId, content));
	}

	// =========================
	// UPDATE
	// =========================

	@PutMapping("/{commentId}/user/{userId}")
	public ResponseEntity<Comment> updateComment(@PathVariable Long commentId, @PathVariable Long userId,
			@RequestParam String content) {

		return ResponseEntity.ok(commentService.updateComment(commentId, userId, content));
	}

	// =========================
	// DELETE
	// =========================

	@DeleteMapping("/{commentId}/user/{userId}")
	public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, @PathVariable Long userId) {

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
	public ResponseEntity<List<Comment>> getCommentsByUser(@PathVariable Long userId) {

		return ResponseEntity.ok(commentService.getCommentsByUser(userId));
	}
}