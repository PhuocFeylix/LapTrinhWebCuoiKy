package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import vn.uteexpress.entity.CommentMedia;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.service.CommentMediaService;

@RestController
@RequestMapping("/api/comment-media")
public class CommentMediaController {

	private final CommentMediaService commentMediaService;
	private final UserRepository userRepository;

	public CommentMediaController(CommentMediaService commentMediaService, UserRepository userRepository) {

		this.commentMediaService = commentMediaService;
		this.userRepository = userRepository;
	}

	// =========================
	// UPLOAD MEDIA
	// =========================

	@PostMapping(value = "/comment/{commentId}/upload", consumes = "multipart/form-data")
	public ResponseEntity<CommentMedia> uploadMedia(@PathVariable Long commentId,
			@RequestParam("file") MultipartFile file, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		return ResponseEntity.ok(commentMediaService.uploadMedia(commentId, file, currentUser));
	}

	// =========================
	// GET MEDIA
	// =========================

	@GetMapping("/comment/{commentId}")
	public ResponseEntity<List<CommentMedia>> getMedia(@PathVariable Long commentId) {

		return ResponseEntity.ok(commentMediaService.getMediaByComment(commentId));
	}

	// =========================
	// DELETE MEDIA
	// =========================

	@DeleteMapping("/{mediaId}")
	public ResponseEntity<Void> deleteMedia(@PathVariable Long mediaId, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		commentMediaService.deleteMedia(mediaId, currentUser);

		return ResponseEntity.noContent().build();
	}

	// =========================
	// CURRENT USER
	// =========================

	private User getCurrentUser(Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated()) {

			throw new IllegalArgumentException("Chưa đăng nhập");
		}

		return userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
	}
}