package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import vn.uteexpress.entity.CommentMedia;
import vn.uteexpress.service.CommentMediaService;

@RestController
@RequestMapping("/api/comment-media")
public class CommentMediaController {

	private final CommentMediaService commentMediaService;

	public CommentMediaController(CommentMediaService commentMediaService) {

		this.commentMediaService = commentMediaService;
	}

	// =========================
	// UPLOAD MEDIA
	// =========================

	@PostMapping(value = "/comment/{commentId}/upload", consumes = "multipart/form-data")
	public ResponseEntity<CommentMedia> uploadMedia(@PathVariable Long commentId,
			@RequestParam("file") MultipartFile file) {

		return ResponseEntity.ok(commentMediaService.uploadMedia(commentId, file));
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
	public ResponseEntity<Void> deleteMedia(@PathVariable Long mediaId) {

		commentMediaService.deleteMedia(mediaId);

		return ResponseEntity.noContent().build();
	}
}