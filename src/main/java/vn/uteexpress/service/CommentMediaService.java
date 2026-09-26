package vn.uteexpress.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import vn.uteexpress.entity.Comment;
import vn.uteexpress.entity.CommentMedia;
import vn.uteexpress.entity.MediaType;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.CommentMediaRepository;
import vn.uteexpress.repository.CommentRepository;

@Service
public class CommentMediaService {

	private final CommentMediaRepository commentMediaRepository;
	private final CommentRepository commentRepository;

	private final Path uploadDirectory = Paths.get("uploads", "comments");

	public CommentMediaService(CommentMediaRepository commentMediaRepository, CommentRepository commentRepository) {

		this.commentMediaRepository = commentMediaRepository;

		this.commentRepository = commentRepository;

		try {
			Files.createDirectories(uploadDirectory);
		} catch (IOException e) {
			throw new RuntimeException("Không thể tạo thư mục upload", e);
		}
	}

	// =========================
	// UPLOAD MEDIA
	// =========================

	@Transactional
	public CommentMedia uploadMedia(Long commentId, MultipartFile file, User currentUser) {

		checkUser(currentUser);

		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("File không được để trống");
		}

		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy comment"));

		checkCommentAccess(comment, currentUser);

		MediaType mediaType = detectMediaType(file.getContentType());

		validateFile(file, mediaType);

		String originalFilename = file.getOriginalFilename();

		String extension = getExtension(originalFilename);

		String storedFilename = UUID.randomUUID() + extension;

		Path targetPath = uploadDirectory.resolve(storedFilename).normalize();

		/*
		 * Đảm bảo file không thể thoát khỏi thư mục uploads/comments.
		 */
		if (!targetPath.startsWith(uploadDirectory.toAbsolutePath().normalize())) {

			throw new IllegalArgumentException("Đường dẫn file không hợp lệ");
		}

		try {

			Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

		} catch (IOException e) {

			throw new RuntimeException("Không thể lưu file", e);
		}

		CommentMedia media = new CommentMedia();

		media.setComment(comment);
		media.setMediaType(mediaType);

		media.setMediaUrl("/uploads/comments/" + storedFilename);

		return commentMediaRepository.save(media);
	}

	// =========================
	// GET MEDIA
	// =========================

	@Transactional(readOnly = true)
	public List<CommentMedia> getMediaByComment(Long commentId) {

		if (commentId == null) {
			throw new IllegalArgumentException("Comment ID không được null");
		}

		return commentMediaRepository.findByCommentId(commentId);
	}

	// =========================
	// DELETE MEDIA
	// =========================

	@Transactional
	public void deleteMedia(Long mediaId, User currentUser) {

		checkUser(currentUser);

		CommentMedia media = commentMediaRepository.findById(mediaId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy media"));

		Comment comment = media.getComment();

		if (comment == null) {
			throw new IllegalArgumentException("Media không thuộc Comment hợp lệ");
		}

		checkCommentAccess(comment, currentUser);

		String mediaUrl = media.getMediaUrl();

		if (mediaUrl != null && mediaUrl.startsWith("/uploads/comments/")) {

			String filename = mediaUrl.substring("/uploads/comments/".length());

			Path filePath = uploadDirectory.resolve(filename).normalize();

			/*
			 * Không cho phép mediaUrl chứa đường dẫn thoát khỏi thư mục upload.
			 */
			if (!filePath.startsWith(uploadDirectory.toAbsolutePath().normalize())) {

				throw new IllegalArgumentException("Đường dẫn file không hợp lệ");
			}

			try {

				Files.deleteIfExists(filePath);

			} catch (IOException e) {

				throw new RuntimeException("Không thể xóa file", e);
			}
		}

		commentMediaRepository.delete(media);
	}

	// =========================
	// CHECK USER
	// =========================

	private void checkUser(User currentUser) {

		if (currentUser == null) {
			throw new IllegalArgumentException("Chưa đăng nhập");
		}
	}

	// =========================
	// CHECK COMMENT OWNERSHIP
	// =========================

	private void checkCommentAccess(Comment comment, User currentUser) {

		if (comment.getUser() == null) {
			throw new IllegalArgumentException("Comment không có người dùng");
		}

		boolean isAdmin = currentUser.getRole() != null && "ADMIN".equalsIgnoreCase(currentUser.getRole().getName());

		if (isAdmin) {
			return;
		}

		if (!comment.getUser().getId().equals(currentUser.getId())) {

			throw new IllegalArgumentException("Bạn không có quyền thao tác media " + "của comment này");
		}
	}

	// =========================
	// DETECT MEDIA TYPE
	// =========================

	private MediaType detectMediaType(String contentType) {

		if (contentType == null) {
			throw new IllegalArgumentException("Không xác định được loại file");
		}

		if (contentType.startsWith("image/")) {
			return MediaType.IMAGE;
		}

		if (contentType.startsWith("video/")) {
			return MediaType.VIDEO;
		}

		throw new IllegalArgumentException("Chỉ hỗ trợ file hình ảnh hoặc video");
	}

	// =========================
	// VALIDATE FILE
	// =========================

	private void validateFile(MultipartFile file, MediaType mediaType) {

		long size = file.getSize();

		if (mediaType == MediaType.IMAGE) {

			// 5 MB
			long maxSize = 5 * 1024 * 1024;

			if (size > maxSize) {
				throw new IllegalArgumentException("Ảnh không được vượt quá 5MB");
			}

		} else {

			// 50 MB
			long maxSize = 50 * 1024 * 1024;

			if (size > maxSize) {
				throw new IllegalArgumentException("Video không được vượt quá 50MB");
			}
		}
	}

	// =========================
	// GET EXTENSION
	// =========================

	private String getExtension(String filename) {

		if (filename == null || !filename.contains(".")) {

			return "";
		}

		String extension = filename.substring(filename.lastIndexOf("."));

		/*
		 * Không cho extension quá dài.
		 */
		if (extension.length() > 10) {
			throw new IllegalArgumentException("Phần mở rộng file không hợp lệ");
		}

		return extension;
	}
}